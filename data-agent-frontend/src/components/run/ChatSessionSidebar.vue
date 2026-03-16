<!--
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
-->

<template>
  <el-aside
    :width="collapsed ? '64px' : '300px'"
    class="chat-session-sidebar"
    :class="{ collapsed }"
  >
    <!-- 收起时只显示展开按钮 -->
    <div v-if="collapsed" class="sidebar-collapsed">
      <el-tooltip content="展开会话列表" placement="right">
        <el-button type="primary" circle class="expand-btn" @click="collapsed = false">
          <el-icon><DArrowRight /></el-icon>
        </el-button>
      </el-tooltip>
      <div class="collapsed-actions">
        <el-tooltip content="新建会话" placement="right">
          <el-button type="success" circle @click="createNewSession">
            <el-icon><Plus /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>

    <!-- 展开时显示完整内容 -->
    <template v-else>
      <!-- 顶部用户信息区 -->
      <div class="sidebar-header">
        <div class="user-info-card">
          <el-avatar :src="agent.avatar" :size="48" class="agent-avatar">
            {{ agent.name ? agent.name.charAt(0) : 'A' }}
          </el-avatar>
          <div class="agent-details">
            <h3 class="agent-name">{{ agent.name || 'Agent' }}</h3>
            <span class="agent-status">
              <span class="status-dot"></span>
              在线
            </span>
          </div>
          <el-button class="collapse-btn" text circle @click="collapsed = true">
            <el-icon><DArrowLeft /></el-icon>
          </el-button>
        </div>

        <!-- 操作按钮区 -->
        <div class="action-buttons">
          <el-button type="primary" class="new-session-btn" @click="createNewSession">
            <el-icon class="btn-icon"><Plus /></el-icon>
            <span>新会话</span>
          </el-button>
          <el-tooltip content="清空所有会话" placement="bottom">
            <el-button type="danger" plain class="clear-btn" @click="clearAllSessions">
              <el-icon><Delete /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </div>

      <!-- 会话列表 -->
      <div class="session-list-container custom-scrollbar">
        <div v-if="sessions.length === 0" class="empty-sessions">
          <el-empty description="暂无历史会话" :image-size="80" />
        </div>

        <transition-group name="list" tag="div" class="session-list">
          <div
            v-for="session in sessions"
            :key="session.id"
            :class="[
              'session-item',
              {
                active: handleGetCurrentSession()?.id === session.id,
                pinned: session.isPinned,
              },
            ]"
            @click="handleSelectSession(session)"
          >
            <!-- 装饰性左侧边框条 -->
            <div class="active-indicator"></div>

            <div class="session-content">
              <div class="session-main">
                <div class="title-row">
                  <el-icon v-if="session.isPinned" class="pin-icon-marker"><StarFilled /></el-icon>

                  <span
                    class="session-title"
                    @dblclick="startEditSessionTitle(session)"
                    v-if="!session.editing"
                    :title="session.title"
                  >
                    {{ session.title || '新会话' }}
                  </span>
                  <el-input
                    v-else
                    v-model="session.editingTitle"
                    size="small"
                    class="edit-input"
                    @blur="saveSessionTitle(session)"
                    @keyup.enter="saveSessionTitle(session)"
                    @keyup.esc="cancelEditSessionTitle(session)"
                    ref="sessionTitleInputRef"
                  />
                </div>
                <div class="session-meta">
                  <span class="time">
                    {{ formatTime(session.updateTime || session.createTime) }}
                  </span>
                </div>
              </div>

              <!-- 悬浮操作栏 -->
              <div class="session-actions-overlay">
                <el-tooltip content="重命名" placement="top" :show-after="500">
                  <el-button
                    link
                    class="action-btn edit"
                    @click.stop="startEditSessionTitle(session)"
                  >
                    <el-icon><Edit /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip
                  :content="session.isPinned ? '取消置顶' : '置顶'"
                  placement="top"
                  :show-after="500"
                >
                  <el-button
                    link
                    class="action-btn pin"
                    :class="{ 'is-pinned': session.isPinned }"
                    @click.stop="togglePinSession(session)"
                  >
                    <el-icon>
                      <StarFilled v-if="session.isPinned" />
                      <Star v-else />
                    </el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="删除" placement="top" :show-after="500">
                  <el-button link class="action-btn delete" @click.stop="deleteSession(session)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </div>
          </div>
        </transition-group>
      </div>

      <!-- 底部信息 -->
      <div class="sidebar-footer">
        <span class="session-count">{{ sessions.length }} 个会话</span>
      </div>
    </template>
  </el-aside>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import ChatService from '../../services/chat';
import {
  Plus,
  Delete,
  Star,
  StarFilled,
  Edit,
  DArrowLeft,
  DArrowRight,
} from '@element-plus/icons-vue';
import { type Agent } from '../../services/agent';
import { type ChatSession } from '../../services/chat';

// 扩展ChatSession接口以包含编辑相关属性
interface ExtendedChatSession extends ChatSession {
  editing?: boolean;
  editingTitle?: string;
}

interface SessionUpdateEvent {
  type: string;
  sessionId: string;
  title: string;
}

export default defineComponent({
  name: 'ChatSessionSidebar',
  components: {
    Plus,
    Delete,
    Star,
    StarFilled,
    Edit,
    DArrowLeft,
    DArrowRight,
  },
  props: {
    agent: {
      type: Object as PropType<Agent>,
      required: true,
    },
    handleSetCurrentSession: {
      type: Function as PropType<(session: ChatSession | null) => Promise<void>>,
      required: true,
    },
    handleGetCurrentSession: {
      type: Function as PropType<() => ChatSession | null>,
      required: true,
    },
    handleSelectSession: {
      type: Function as PropType<(session: ChatSession) => Promise<void>>,
      required: true,
    },
    handleDeleteSessionState: {
      type: Function as PropType<(sessionId: string) => void>,
      required: true,
    },
  },
  setup(props) {
    const sessions = ref<ExtendedChatSession[]>([]);
    const collapsed = ref(false);
    const sessionEventSource = ref<EventSource | null>(null);
    let reconnectTimer: number | null = null;
    let isComponentActive = true;

    const router = useRouter();
    const route = useRoute();

    const formatTime = (time: Date | string | undefined) => {
      if (!time) return '';
      const date = new Date(time);
      return date.toLocaleString('zh-CN');
    };

    const clearReconnectTimer = () => {
      if (reconnectTimer) {
        window.clearTimeout(reconnectTimer);
        reconnectTimer = null;
      }
    };

    const handleTitleUpdate = (eventData: SessionUpdateEvent) => {
      if (!eventData?.sessionId) {
        return;
      }
      const target = sessions.value.find(session => session.id === eventData.sessionId);
      if (target) {
        target.title = eventData.title;
        target.editingTitle = eventData.title;
      }
      const current = props.handleGetCurrentSession();
      if (current && current.id === eventData.sessionId) {
        current.title = eventData.title;
      }
    };

    const connectSessionStream = () => {
      clearReconnectTimer();
      const currentAgentId = agentId.value;
      if (!currentAgentId) {
        return;
      }
      if (sessionEventSource.value) {
        sessionEventSource.value.close();
      }
      const source = new EventSource(`/api/agent/${currentAgentId}/sessions/stream`);
      source.addEventListener('title-updated', event => {
        try {
          const data = JSON.parse((event as MessageEvent<string>).data) as SessionUpdateEvent;
          handleTitleUpdate(data);
        } catch (error) {
          console.error('解析会话标题更新失败', error);
        }
      });
      source.onerror = error => {
        console.error('会话推送连接异常:', error);
        source.close();
        sessionEventSource.value = null;
        if (isComponentActive) {
          reconnectTimer = window.setTimeout(() => connectSessionStream(), 3000);
        }
      };
      sessionEventSource.value = source;
    };

    // 开始编辑会话标题
    const startEditSessionTitle = (session: ExtendedChatSession) => {
      session.editing = true;
      session.editingTitle = session.title || '新会话';
      nextTick(() => {
        const input = document.querySelector('.el-input__inner') as HTMLInputElement;
        if (input) {
          input.focus();
          input.select();
        }
      });
    };

    // 保存会话标题
    const saveSessionTitle = async (session: ExtendedChatSession) => {
      if (!session.editingTitle || session.editingTitle.trim() === '') {
        ElMessage.warning('会话标题不能为空');
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
        ElMessage.success('会话标题已更新');
      } catch (error) {
        ElMessage.error('更新会话标题失败');
        console.error('更新会话标题失败:', error);
      }
    };

    // 取消编辑会话标题
    const cancelEditSessionTitle = (session: ExtendedChatSession) => {
      session.editing = false;
    };

    // 计算属性
    const agentId = computed(() => route.params.id as string);

    // 方法
    const goBack = () => {
      router.push(`/agent/${agentId.value}`);
    };

    const loadSessions = async () => {
      try {
        sessions.value = await ChatService.getAgentSessions(parseInt(agentId.value));
        // 默认选择第一个会话或创建新会话
        if (sessions.value.length > 0) {
          await props.handleSelectSession(sessions.value[0]);
        } else {
          await createNewSession();
        }
      } catch (error) {
        ElMessage.error('加载会话列表失败');
        console.error('加载会话列表失败:', error);
      }
    };

    const createNewSession = async () => {
      try {
        const newSession = await ChatService.createSession(parseInt(agentId.value), '新会话');
        sessions.value.unshift(newSession);
        await props.handleSelectSession(newSession);
        ElMessage.success('新会话创建成功');
      } catch (error) {
        ElMessage.error('创建会话失败');
        console.error('创建会话失败:', error);
      }
    };

    const togglePinSession = async (session: ChatSession) => {
      try {
        await ChatService.pinSession(session.id, !session.isPinned);
        session.isPinned = !session.isPinned;
        ElMessage.success(session.isPinned ? '会话已置顶' : '会话已取消置顶');
      } catch (error) {
        ElMessage.error('操作失败');
        console.error('置顶会话失败:', error);
      }
    };

    const deleteSession = async (session: ChatSession) => {
      try {
        await ElMessageBox.confirm('确定要删除这个会话吗？', '确认删除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        });
        await ChatService.deleteSession(session.id);
        props.handleDeleteSessionState(session.id);
        sessions.value = sessions.value.filter((s: ChatSession) => s.id !== session.id);
        if (props.handleGetCurrentSession() == session) {
          await props.handleSetCurrentSession(null);
        }
        ElMessage.success('会话删除成功');
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除会话失败');
          console.error('删除会话失败:', error);
        }
      }
    };

    const clearAllSessions = async () => {
      try {
        await ElMessageBox.confirm('确定要清空所有会话吗？此操作不可恢复。', '确认清空', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        });
        await ChatService.clearAgentSessions(parseInt(agentId.value));
        sessions.value.forEach((session: ChatSession) => {
          props.handleDeleteSessionState(session.id);
        });
        sessions.value = [];
        await props.handleSetCurrentSession(null);
        ElMessage.success('所有会话已清空');
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('清空会话失败');
          console.error('清空会话失败:', error);
        }
      }
    };

    // 生命周期
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

    return {
      sessions,
      collapsed,
      formatTime,
      goBack,
      createNewSession,
      togglePinSession,
      deleteSession,
      clearAllSessions,
      startEditSessionTitle,
      saveSessionTitle,
      cancelEditSessionTitle,
    };
  },
});
</script>

<style scoped>
.chat-session-sidebar {
  background-color: #f8fafc;
  border-right: 1px solid #e2e8f0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
  z-index: 10;
}

.chat-session-sidebar.collapsed {
  width: 64px !important;
}

/* Collapsed State */
.sidebar-collapsed {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  gap: 24px;
  height: 100%;
}

.collapsed-actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Header Section */
.sidebar-header {
  padding: 24px 20px 16px;
  background-color: #f8fafc;
  flex-shrink: 0;
}

.user-info-card {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding: 4px;
}

.agent-avatar {
  border: 2px solid white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  flex-shrink: 0;
  background: #e0e7ff;
  color: #4f46e5;
  font-weight: 600;
  font-size: 18px;
}

.agent-details {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.agent-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.agent-status {
  font-size: 12px;
  color: #10b981;
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-dot {
  width: 6px;
  height: 6px;
  background-color: #10b981;
  border-radius: 50%;
  display: inline-block;
}

.collapse-btn {
  color: #94a3b8;
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: #475569;
  background-color: #f1f5f9;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

.new-session-btn {
  flex: 1;
  height: 40px;
  border-radius: 10px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  box-shadow: 0 4px 6px -1px rgba(79, 70, 229, 0.1), 0 2px 4px -1px rgba(79, 70, 229, 0.06);
  transition: all 0.2s ease;
}

.new-session-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 8px -1px rgba(79, 70, 229, 0.15), 0 3px 6px -1px rgba(79, 70, 229, 0.1);
}

.clear-btn {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  padding: 0;
}

/* Session List */
.session-list-container {
  flex: 1;
  overflow-y: auto;
  padding: 0 12px;
  margin-top: 8px;
}

.empty-sessions {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #94a3b8;
}

.session-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-bottom: 20px;
}

.session-item {
  position: relative;
  padding: 12px 16px;
  border-radius: 12px;
  background: white;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  overflow: hidden;
  display: flex;
  align-items: center;
}

.session-item:hover {
  background: white;
  border-color: #e2e8f0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  transform: translateY(-1px);
}

.session-item.active {
  background: #eff6ff;
  border-color: #bfdbfe;
}

.session-item.active .session-title {
  color: #1d4ed8;
  font-weight: 600;
}

.session-item.pinned {
  background: #fffbeb;
  border-color: #fef3c7;
}

.session-item.pinned.active {
  background: #eff6ff; /* Active takes precedence visually but keep pin indicator */
  border-color: #bfdbfe;
}

/* Active Indicator */
.active-indicator {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%) scaleY(0);
  width: 3px;
  height: 24px;
  background-color: #3b82f6;
  border-top-right-radius: 3px;
  border-bottom-right-radius: 3px;
  transition: transform 0.2s ease;
}

.session-item.active .active-indicator {
  transform: translateY(-50%) scaleY(1);
}

.session-content {
  flex: 1;
  min-width: 0;
  position: relative;
}

.session-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.pin-icon-marker {
  font-size: 12px;
  color: #f59e0b;
  flex-shrink: 0;
}

.session-title {
  font-size: 14px;
  color: #334155;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  line-height: 1.5;
}

.edit-input {
  height: 24px;
  font-size: 13px;
}

.session-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time {
  font-size: 11px;
  color: #94a3b8;
}

/* Hover Actions Overlay */
.session-actions-overlay {
  position: absolute;
  right: -8px;
  top: 50%;
  transform: translateY(-50%);
  background: linear-gradient(90deg, rgba(255, 255, 255, 0) 0%, rgba(255, 255, 255, 1) 20%);
  padding-left: 20px;
  display: flex;
  gap: 2px;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s ease;
}

.session-item.active .session-actions-overlay {
  background: linear-gradient(90deg, rgba(239, 246, 255, 0) 0%, rgba(239, 246, 255, 1) 20%);
}

.session-item.pinned:not(.active) .session-actions-overlay {
  background: linear-gradient(90deg, rgba(255, 251, 235, 0) 0%, rgba(255, 251, 235, 1) 20%);
}

.session-item:hover .session-actions-overlay {
  opacity: 1;
  pointer-events: auto;
}

.action-btn {
  padding: 4px;
  height: 24px;
  width: 24px;
  color: #64748b;
  transition: all 0.2s;
}

.action-btn:hover {
  color: #3b82f6;
  background-color: rgba(59, 130, 246, 0.1);
  border-radius: 4px;
}

.action-btn.delete:hover {
  color: #ef4444;
  background-color: rgba(239, 68, 68, 0.1);
}

.action-btn.pin.is-pinned {
  color: #f59e0b;
}

/* Footer */
.sidebar-footer {
  padding: 12px 20px;
  border-top: 1px solid #f1f5f9;
  text-align: center;
  background-color: #f8fafc;
}

.session-count {
  font-size: 12px;
  color: #94a3b8;
}

/* Custom Scrollbar */
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 2px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

/* Transitions */
.list-move,
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

.list-leave-active {
  position: absolute;
  width: 100%;
}
</style>
