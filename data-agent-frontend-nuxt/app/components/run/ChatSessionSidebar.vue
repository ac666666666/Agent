<template>
  <v-navigation-drawer
    v-model="drawer"
    :width="320"
    :rail="collapsed"
    permanent
    class="chat-session-sidebar"
  >
    <!-- Collapsed State: Only Expand Button -->
    <div v-if="collapsed" class="d-flex flex-column align-center py-4 gap-2">
      <v-tooltip text="展开会话列表" location="right">
        <template v-slot:activator="{ props }">
          <v-btn
            v-bind="props"
            icon="mdi-chevron-double-right"
            color="primary"
            variant="tonal"
            size="small"
            @click="collapsed = false"
          ></v-btn>
        </template>
      </v-tooltip>
    </div>

    <!-- Expanded State -->
    <template v-else>
      <!-- Header -->
      <div class="pa-4">
        <div class="d-flex justify-space-between align-center mb-4">
          <v-avatar size="48" class="mx-auto">
            <v-img
              v-if="agent.avatar"
              :src="agent.avatar"
              :alt="agent.name"
            ></v-img>
            <span v-else class="text-h6">{{ agent.name?.charAt(0) }}</span>
          </v-avatar>

          <v-tooltip text="收起会话列表" location="bottom">
            <template v-slot:activator="{ props }">
              <v-btn
                v-bind="props"
                icon="mdi-chevron-double-left"
                variant="text"
                size="small"
                @click="collapsed = true"
              ></v-btn>
            </template>
          </v-tooltip>
        </div>

        <div class="d-flex gap-2">
          <v-btn
            color="primary"
            prepend-icon="mdi-plus"
            class="flex-grow-1"
            @click="createNewSession"
          >
            新建会话
          </v-btn>
          <v-btn
            color="error"
            variant="tonal"
            icon="mdi-delete"
            @click="clearAllSessions"
          ></v-btn>
        </div>
      </div>

      <v-divider></v-divider>

      <!-- Session List -->
      <div class="session-list pa-4">
        <v-card
          v-for="session in sessions"
          :key="session.id"
          :color="
            handleGetCurrentSession()?.id === session.id
              ? 'primary-lighten-5'
              : undefined
          "
          :variant="
            handleGetCurrentSession()?.id === session.id ? 'flat' : 'outlined'
          "
          class="mb-3 cursor-pointer session-card"
          :class="{
            'border-primary': handleGetCurrentSession()?.id === session.id,
            'pinned-border': session.isPinned,
          }"
          @click="handleSelectSession(session)"
        >
          <div class="pa-3">
            <div class="d-flex justify-space-between align-start mb-1">
              <!-- Title or Edit Input -->
              <div class="flex-grow-1 mr-2 overflow-hidden">
                <v-text-field
                  v-if="session.editing"
                  v-model="session.editingTitle"
                  density="compact"
                  variant="outlined"
                  hide-details
                  autofocus
                  @blur="saveSessionTitle(session)"
                  @keydown.enter="saveSessionTitle(session)"
                  @keydown.esc="cancelEditSessionTitle(session)"
                  @click.stop
                ></v-text-field>
                <div
                  v-else
                  class="text-subtitle-2 font-weight-bold text-truncate"
                  @dblclick="startEditSessionTitle(session)"
                >
                  {{ session.title || "新会话" }}
                </div>
              </div>

              <!-- Actions -->
              <div class="d-flex gap-1 session-actions">
                <v-btn
                  icon="mdi-pencil"
                  size="x-small"
                  variant="text"
                  color="grey"
                  @click.stop="startEditSessionTitle(session)"
                ></v-btn>
                <v-btn
                  :icon="session.isPinned ? 'mdi-star' : 'mdi-star-outline'"
                  size="x-small"
                  variant="text"
                  :color="session.isPinned ? 'warning' : 'grey'"
                  @click.stop="togglePinSession(session)"
                ></v-btn>
                <v-btn
                  icon="mdi-delete"
                  size="x-small"
                  variant="text"
                  color="grey"
                  @click.stop="deleteSession(session)"
                ></v-btn>
              </div>
            </div>

            <div class="text-caption text-grey">
              {{ formatTime(session.updateTime || session.createTime) }}
            </div>
          </div>
        </v-card>
      </div>
    </template>
  </v-navigation-drawer>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick } from "vue";
import { useRouter, useRoute } from "vue-router";
import ChatService from "~/services/chat";
import type { ChatSession } from "~/services/chat";
import type { Agent } from "~/services/agent";
import { useConfirm } from "~/composables/useConfirm";

// Extended Interface
interface ExtendedChatSession extends ChatSession {
  editing?: boolean;
  editingTitle?: string;
}

interface SessionUpdateEvent {
  type: string;
  sessionId: string;
  title: string;
}

const props = defineProps<{
  agent: Agent;
  handleSetCurrentSession: (session: ChatSession | null) => Promise<void>;
  handleGetCurrentSession: () => ChatSession | null;
  handleSelectSession: (session: ChatSession) => Promise<void>;
  handleDeleteSessionState: (sessionId: number) => void;
}>();

const router = useRouter();
const route = useRoute();
const { showConfirm } = useConfirm();

const sessions = ref<ExtendedChatSession[]>([]);
const collapsed = ref(false);
const drawer = ref(true); // Control visibility if needed, or always true for sidebar
const sessionEventSource = ref<EventSource | null>(null);
let reconnectTimer: number | null = null;
let isComponentActive = true;

const agentId = computed(() => route.params.id as string);

// Methods
const formatTime = (time: Date | string | undefined) => {
  if (!time) return "";
  return new Date(time).toLocaleString("zh-CN");
};

const clearReconnectTimer = () => {
  if (reconnectTimer) {
    window.clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
};

const handleTitleUpdate = (eventData: SessionUpdateEvent) => {
  if (!eventData?.sessionId) return;

  // Convert sessionId to number if needed, assuming API returns string but interface uses number
  // The interface in old code used string for sessionId in event but number in ChatSession?
  // Let's assume ID types match or are compatible.
  const sid = parseInt(eventData.sessionId);

  const target = sessions.value.find((session) => session.id === sid);
  if (target) {
    target.title = eventData.title;
    target.editingTitle = eventData.title;
  }
  const current = props.handleGetCurrentSession();
  if (current && current.id === sid) {
    current.title = eventData.title;
  }
};

const connectSessionStream = () => {
  clearReconnectTimer();
  const currentAgentId = agentId.value;
  if (!currentAgentId) return;

  if (sessionEventSource.value) {
    sessionEventSource.value.close();
  }
  // Use absolute path or proxy will handle it
  const source = new EventSource(
    `/api/agent/${currentAgentId}/sessions/stream`
  );

  source.addEventListener("title-updated", (event) => {
    try {
      const data = JSON.parse(
        (event as MessageEvent).data
      ) as SessionUpdateEvent;
      handleTitleUpdate(data);
    } catch (error) {
      console.error("解析会话标题更新失败", error);
    }
  });

  source.onerror = (error) => {
    console.error("会话推送连接异常:", error);
    source.close();
    sessionEventSource.value = null;
    if (isComponentActive) {
      reconnectTimer = window.setTimeout(() => connectSessionStream(), 3000);
    }
  };

  sessionEventSource.value = source;
};

const startEditSessionTitle = (session: ExtendedChatSession) => {
  session.editing = true;
  session.editingTitle = session.title || "新会话";
};

const saveSessionTitle = async (session: ExtendedChatSession) => {
  if (!session.editingTitle || session.editingTitle.trim() === "") {
    // TODO: Toast warning '会话标题不能为空'
    return;
  }

  const newTitle = session.editingTitle.trim();
  if (newTitle === session.title) {
    session.editing = false;
    return;
  }

  try {
    await ChatService.renameSession(session.id, newTitle);
    session.title = newTitle;
    session.editing = false;
    // TODO: Toast success '会话标题已更新'
  } catch (error) {
    console.error("更新会话标题失败:", error);
    // TODO: Toast error
  }
};

const cancelEditSessionTitle = (session: ExtendedChatSession) => {
  session.editing = false;
};

const loadSessions = async () => {
  try {
    sessions.value = await ChatService.getAgentSessions(
      parseInt(agentId.value)
    );
    if (sessions.value.length > 0) {
      await props.handleSelectSession(sessions.value[0]);
    } else {
      await createNewSession();
    }
  } catch (error) {
    console.error("加载会话列表失败:", error);
    // TODO: Toast error
  }
};

const createNewSession = async () => {
  try {
    const newSession = await ChatService.createSession(
      parseInt(agentId.value),
      "新会话"
    );
    sessions.value.unshift(newSession);
    await props.handleSelectSession(newSession);
    // TODO: Toast success
  } catch (error) {
    console.error("创建会话失败:", error);
    // TODO: Toast error
  }
};

const togglePinSession = async (session: ExtendedChatSession) => {
  try {
    await ChatService.pinSession(session.id, !session.isPinned);
    session.isPinned = !session.isPinned;
    // TODO: Toast success
  } catch (error) {
    console.error("置顶会话失败:", error);
    // TODO: Toast error
  }
};

const deleteSession = async (session: ExtendedChatSession) => {
  showConfirm({
    title: "确认删除",
    message: "确定要删除这个会话吗？",
    confirmText: "确定",
    onConfirm: async () => {
      try {
        await ChatService.deleteSession(session.id);
        props.handleDeleteSessionState(session.id);
        sessions.value = sessions.value.filter((s) => s.id !== session.id);
        if (props.handleGetCurrentSession()?.id === session.id) {
          await props.handleSetCurrentSession(null);
        }
        // TODO: Toast success
      } catch (error) {
        console.error("删除会话失败:", error);
        // TODO: Toast error
      }
    },
  });
};

const clearAllSessions = async () => {
  showConfirm({
    title: "确认清空",
    message: "确定要清空所有会话吗？此操作不可恢复。",
    confirmText: "确定",
    onConfirm: async () => {
      try {
        await ChatService.clearAgentSessions(parseInt(agentId.value));
        sessions.value.forEach((session) => {
          props.handleDeleteSessionState(session.id);
        });
        sessions.value = [];
        await props.handleSetCurrentSession(null);
        // TODO: Toast success
      } catch (error) {
        console.error("清空会话失败:", error);
        // TODO: Toast error
      }
    },
  });
};

// Lifecycle
onMounted(async () => {
  connectSessionStream();
  await loadSessions();
});

onUnmounted(() => {
  isComponentActive = false;
  clearReconnectTimer();
  if (sessionEventSource.value) {
    sessionEventSource.value.close();
    sessionEventSource.value = null;
  }
});
</script>

<style scoped>
.session-list {
  overflow-y: auto;
  height: calc(100vh - 160px); /* Adjust based on header height */
}

.session-card {
  transition: all 0.2s ease;
}

.session-card:hover {
  border-color: rgb(var(--v-theme-primary));
  background-color: rgb(var(--v-theme-primary), 0.05);
}

.session-card.pinned-border {
  border-left: 4px solid rgb(var(--v-theme-warning)) !important;
}

.border-primary {
  border-color: rgb(var(--v-theme-primary)) !important;
}

.session-actions {
  opacity: 0;
  transition: opacity 0.2s ease;
}

.session-card:hover .session-actions {
  opacity: 1;
}
</style>