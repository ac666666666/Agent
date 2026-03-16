<template>
  <div class="agent-list-page pa-6">
    <!-- Header Section -->
    <div
      class="d-flex flex-column flex-md-row justify-space-between align-start align-md-center mb-6"
    >
      <div class="header-info">
        <h1 class="text-h4 font-weight-bold text-grey-darken-3 mb-1">
          智能体管理中心
        </h1>
        <p class="text-subtitle-1 text-grey-darken-1">
          创建和管理您的AI智能体，让数据分析更智能
        </p>
      </div>

      <div class="header-stats d-flex gap-4 mt-4 mt-md-0">
        <div class="stat-item text-center px-4">
          <div class="text-h4 font-weight-bold text-primary">
            {{ agents.length }}
          </div>
          <div class="text-caption text-grey">总数量</div>
        </div>
        <div class="stat-item text-center px-4">
          <div class="text-h4 font-weight-bold text-primary">
            {{ publishedCount }}
          </div>
          <div class="text-caption text-grey">已发布</div>
        </div>
        <div class="stat-item text-center px-4">
          <div class="text-h4 font-weight-bold text-primary">
            {{ draftCount }}
          </div>
          <div class="text-caption text-grey">草稿</div>
        </div>
        <div class="stat-item text-center px-4">
          <div class="text-h4 font-weight-bold text-primary">
            {{ offlineCount }}
          </div>
          <div class="text-caption text-grey">已下线</div>
        </div>
      </div>
    </div>

    <!-- Filter & Search Section -->
    <v-card class="mb-6 rounded-lg" elevation="1">
      <div class="d-flex flex-column flex-md-row align-center pa-4 gap-4">
        <!-- Filter Tabs -->
        <v-btn-toggle
          v-model="activeFilter"
          mandatory
          class="filter-tabs"
          color="primary"
          variant="outlined"
          divided
        >
          <v-btn value="all">
            <v-icon start icon="mdi-view-grid"></v-icon>
            全部智能体
            <v-chip
              size="x-small"
              class="ml-2"
              color="grey-lighten-2"
              variant="flat"
              >{{ agents.length }}</v-chip
            >
          </v-btn>
          <v-btn value="published">
            <v-icon start icon="mdi-check-circle"></v-icon>
            已发布
            <v-chip
              size="x-small"
              class="ml-2"
              color="grey-lighten-2"
              variant="flat"
              >{{ publishedCount }}</v-chip
            >
          </v-btn>
          <v-btn value="draft">
            <v-icon start icon="mdi-pencil"></v-icon>
            草稿
            <v-chip
              size="x-small"
              class="ml-2"
              color="grey-lighten-2"
              variant="flat"
              >{{ draftCount }}</v-chip
            >
          </v-btn>
          <v-btn value="offline">
            <v-icon start icon="mdi-pause-circle"></v-icon>
            已下线
            <v-chip
              size="x-small"
              class="ml-2"
              color="grey-lighten-2"
              variant="flat"
              >{{ offlineCount }}</v-chip
            >
          </v-btn>
        </v-btn-toggle>

        <v-spacer></v-spacer>

        <!-- Search & Actions -->
        <div class="d-flex gap-2 align-center w-100 w-md-auto">
          <v-text-field
            v-model="searchKeyword"
            placeholder="搜索智能体名称、ID或描述..."
            prepend-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            hide-details
            class="search-field"
            style="min-width: 300px"
            clearable
          ></v-text-field>

          <v-btn
            icon
            variant="text"
            color="grey-darken-1"
            @click="loadAgents"
            :loading="loading"
          >
            <v-icon>mdi-refresh</v-icon>
            <v-tooltip activator="parent" location="top">刷新列表</v-tooltip>
          </v-btn>

          <v-btn
            color="primary"
            prepend-icon="mdi-plus"
            @click="goToCreateAgent"
            height="40"
          >
            创建智能体
          </v-btn>
        </div>
      </div>
    </v-card>

    <!-- Loading State -->
    <v-row v-if="loading">
      <v-col v-for="n in 8" :key="n" cols="12" sm="6" md="4" lg="3">
        <v-skeleton-loader
          class="rounded-lg"
          type="card, article"
          elevation="1"
        ></v-skeleton-loader>
      </v-col>
    </v-row>

    <!-- Empty State -->
    <div
      v-else-if="filteredAgents.length === 0"
      class="d-flex flex-column align-center justify-center py-12"
    >
      <v-icon size="64" color="grey-lighten-1" class="mb-4"
        >mdi-clipboard-text-off-outline</v-icon
      >
      <h3 class="text-h6 text-grey-darken-1 mb-2">暂无智能体</h3>
      <p class="text-body-2 text-grey mb-6">没有找到符合条件的智能体</p>
      <v-btn
        color="primary"
        variant="tonal"
        prepend-icon="mdi-plus"
        @click="goToCreateAgent"
      >
        创建智能体
      </v-btn>
    </div>

    <!-- Agents Grid -->
    <v-row v-else>
      <v-col
        v-for="agent in filteredAgents"
        :key="agent.id"
        cols="12"
        sm="6"
        md="4"
        lg="3"
      >
        <v-hover v-slot="{ isHovering, props }">
          <v-card
            v-bind="props"
            :elevation="isHovering ? 4 : 1"
            class="agent-card rounded-lg h-100 transition-swing"
            @click="enterAgent(agent.id!)"
          >
            <div class="position-relative pa-5">
              <!-- Delete Button (Hover only) -->
              <v-fade-transition>
                <v-btn
                  v-if="isHovering"
                  icon="mdi-delete"
                  size="small"
                  color="error"
                  variant="flat"
                  class="delete-btn"
                  @click.stop="handleDeleteAgent(agent)"
                ></v-btn>
              </v-fade-transition>

              <!-- Status Chip -->
              <div class="d-flex justify-end mb-4">
                <v-chip
                  :color="getStatusColor(agent.status!)"
                  size="small"
                  label
                  class="font-weight-medium"
                >
                  {{ getStatusText(agent.status!) }}
                </v-chip>
              </div>

              <!-- Content -->
              <div class="d-flex flex-column align-center text-center mb-4">
                <v-avatar size="64" color="grey-lighten-4" class="mb-4">
                  <v-img
                    v-if="agent.avatar"
                    :src="agent.avatar"
                    :alt="agent.name"
                  ></v-img>
                  <span v-else class="text-h5 font-weight-bold text-primary">
                    {{ agent.name?.charAt(0).toUpperCase() }}
                  </span>
                </v-avatar>

                <h3 class="text-h6 font-weight-bold text-truncate w-100 mb-2">
                  {{ agent.name }}
                </h3>

                <p class="text-body-2 text-grey-darken-1 agent-desc mb-0">
                  {{ agent.description || "暂无描述" }}
                </p>
              </div>

              <!-- Footer -->
              <v-divider class="mb-3"></v-divider>
              <div
                class="d-flex justify-space-between align-center text-caption text-grey"
              >
                <span>ID: {{ agent.id }}</span>
                <span>{{ formatDate(agent.updateTime) }}</span>
              </div>
            </div>
          </v-card>
        </v-hover>
      </v-col>
    </v-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import agentService from "~/services/agent";
import type { Agent } from "~/services/agent";

const router = useRouter();
const loading = ref(true);
const activeFilter = ref("all");
const searchKeyword = ref("");
const agents = ref<Agent[]>([]);

// Computed Properties
const publishedCount = computed(
  () => agents.value.filter((a) => a.status === "published").length
);
const draftCount = computed(
  () => agents.value.filter((a) => a.status === "draft").length
);
const offlineCount = computed(
  () => agents.value.filter((a) => a.status === "offline").length
);

const filteredAgents = computed(() => {
  let filtered = agents.value;

  // Filter by status
  if (activeFilter.value !== "all") {
    filtered = filtered.filter((agent) => agent.status === activeFilter.value);
  }

  // Filter by keyword
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase();
    filtered = filtered.filter(
      (agent) =>
        (agent.name?.toLowerCase() || "").includes(keyword) ||
        (agent.description?.toLowerCase() || "").includes(keyword) ||
        (agent.id?.toString() || "").includes(keyword)
    );
  }

  return filtered;
});

// Methods
const loadAgents = async () => {
  loading.value = true;
  try {
    const response = await agentService.list();
    agents.value = response || [];
  } catch (error) {
    console.error("Failed to load agents:", error);
    // TODO: Add global toast/snackbar for errors
  } finally {
    loading.value = false;
  }
};

const enterAgent = (agentId: number) => {
  router.push(`/agent/${agentId}`);
};

const goToCreateAgent = () => {
  router.push("/agent/create");
};

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    published: "已发布",
    draft: "草稿",
    offline: "已下线",
  };
  return map[status] || status;
};

const getStatusColor = (status: string) => {
  const map: Record<string, string> = {
    published: "success",
    draft: "warning",
    offline: "grey",
  };
  return map[status] || "primary";
};

const formatDate = (date: Date | string | undefined) => {
  if (!date) return "";
  return new Date(date).toLocaleDateString();
};

const handleDeleteAgent = async (agent: Agent) => {
  const { showConfirm } = useConfirm();

  showConfirm({
    title: "删除确认",
    message: `确定要删除智能体 "${agent.name}" 吗？此操作不可恢复。`,
    icon: "mdi-alert",
    confirmText: "确定删除",
    onConfirm: async () => {
      try {
        const success = await agentService.delete(agent.id!);
        if (success) {
          agents.value = agents.value.filter((a) => a.id !== agent.id);
        }
      } catch (error) {
        console.error("Delete failed:", error);
      }
    },
  });
};

onMounted(() => {
  loadAgents();
});
</script>

<style scoped>
.agent-desc {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 40px; /* Ensure consistent height for 2 lines */
}

.delete-btn {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 2;
}

/* Custom scrollbar if needed */
.agent-list-page {
  background-color: #f8fafc;
  min-height: 100%;
}
</style>