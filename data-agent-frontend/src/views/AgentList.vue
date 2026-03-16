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
  <BaseLayout>
    <div class="agent-list-page">
      <!-- 主内容区域 -->
      <main class="main-content">
        <!-- 内容头部 -->
        <div class="content-header">
          <div class="header-info">
            <h1 class="content-title">智能体管理中心</h1>
            <p class="content-subtitle">创建和管理您的AI智能体，让数据分析更智能</p>
          </div>
          <div class="header-actions">
            <el-button
              type="primary"
              :icon="Plus"
              size="large"
              @click="goToCreateAgent"
              class="create-btn"
            >
              新建智能体
            </el-button>
          </div>
        </div>

        <!-- 统计卡片区域 -->
        <div class="stats-section">
          <el-row :gutter="24">
            <el-col :xs="12" :sm="6" :md="6" :lg="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-content">
                  <div class="stat-icon bg-blue-100 text-blue-600">
                    <el-icon><Grid /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">总数量</div>
                    <div class="stat-number">{{ agents.length }}</div>
                  </div>
                </div>
                <!-- 装饰性背景图标 -->
                <div class="stat-decoration text-blue-600">
                  <el-icon><Grid /></el-icon>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="12" :sm="6" :md="6" :lg="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-content">
                  <div class="stat-icon bg-green-100 text-green-600">
                    <el-icon><Check /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">已发布</div>
                    <div class="stat-number">{{ publishedCount }}</div>
                  </div>
                </div>
                <div class="stat-decoration text-green-600">
                  <el-icon><Check /></el-icon>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="12" :sm="6" :md="6" :lg="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-content">
                  <div class="stat-icon bg-orange-100 text-orange-600">
                    <el-icon><Edit /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">草稿</div>
                    <div class="stat-number">{{ draftCount }}</div>
                  </div>
                </div>
                <div class="stat-decoration text-orange-600">
                  <el-icon><Edit /></el-icon>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="12" :sm="6" :md="6" :lg="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-content">
                  <div class="stat-icon bg-gray-100 text-gray-600">
                    <el-icon><VideoPause /></el-icon>
                  </div>
                  <div class="stat-info">
                    <div class="stat-label">已下线</div>
                    <div class="stat-number">{{ offlineCount }}</div>
                  </div>
                </div>
                <div class="stat-decoration text-gray-600">
                  <el-icon><VideoPause /></el-icon>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- Dashboard Grid Layout -->
        <div class="dashboard-grid">
          <!-- Left Column: Filter & Agent List -->
          <div class="left-panel">
            <!-- 过滤和搜索区域 -->
            <div class="filter-section">
              <div class="filter-container">
                <div class="filter-left">
                  <el-radio-group v-model="activeFilter" size="large" class="custom-radio-group">
                    <el-radio-button value="all">全部</el-radio-button>
                    <el-radio-button value="published">已发布</el-radio-button>
                    <el-radio-button value="draft">草稿</el-radio-button>
                    <el-radio-button value="offline">已下线</el-radio-button>
                  </el-radio-group>
                </div>

                <div class="filter-right">
                  <el-input
                    v-model="searchKeyword"
                    placeholder="搜索智能体..."
                    size="large"
                    :prefix-icon="Search"
                    clearable
                    class="search-input"
                  />
                  <el-button
                    :icon="Refresh"
                    circle
                    size="large"
                    @click="loadAgents"
                    class="icon-btn"
                  />
                </div>
              </div>
            </div>

            <!-- 智能体网格 -->
            <div class="agents-grid" v-if="!loading">
              <el-row :gutter="24">
                <el-col
                  v-for="agent in filteredAgents"
                  :key="agent.id"
                  :xs="24"
                  :sm="12"
                  :md="12"
                  :lg="8"
                  :xl="8"
                >
                  <div class="agent-card-wrapper" @click="enterAgent(agent.id)">
                    <div class="agent-card">
                      <!-- 装饰性背景元素 -->
                      <div class="card-bg-decoration top-right"></div>
                      <div class="card-bg-decoration bottom-left"></div>

                      <!-- 卡片头部：头像+名称+状态 -->
                      <div class="card-header-row">
                        <div class="header-left">
                          <el-avatar
                            :size="48"
                            :src="agent.avatar"
                            shape="square"
                            class="agent-avatar"
                          >
                            {{ agent.name.substring(0, 1) }}
                          </el-avatar>
                          <div class="header-text">
                            <h3 class="agent-name" :title="agent.name">{{ agent.name }}</h3>
                            <div class="agent-id-row">
                              <span class="agent-id-badge">ID: {{ agent.id }}</span>
                            </div>
                          </div>
                        </div>
                        <div class="header-right">
                          <el-tag
                            :type="getStatusTagType(agent.status)"
                            size="small"
                            effect="light"
                            round
                            class="status-tag"
                          >
                            {{ getStatusText(agent.status) }}
                          </el-tag>
                        </div>
                      </div>

                      <!-- 内容区域：描述 -->
                      <div class="card-body">
                        <p class="agent-desc" :title="agent.description">
                          {{ agent.description || '暂无描述' }}
                        </p>
                      </div>

                      <!-- 底部信息 -->
                      <div class="card-footer">
                        <div class="footer-left">
                          <el-icon class="time-icon"><Clock /></el-icon>
                          <span class="update-time">
                            {{ formatTime(agent.updateTime).split(' ')[0] }}
                          </span>
                        </div>
                        <div class="card-actions">
                          <el-tooltip content="删除" placement="top">
                            <el-button
                              type="danger"
                              link
                              class="action-btn delete-btn"
                              @click.stop="handleDeleteAgent(agent)"
                            >
                              <el-icon><Delete /></el-icon>
                            </el-button>
                          </el-tooltip>
                        </div>
                      </div>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>

            <!-- 加载状态 -->
            <div v-if="loading" class="loading-state">
              <el-skeleton :rows="6" animated />
            </div>

            <!-- 空状态 -->
            <div v-if="!loading && filteredAgents.length === 0" class="empty-state">
              <el-empty description="暂无智能体">
                <template #image>
                  <el-icon size="60"><Grid /></el-icon>
                </template>
                <el-button type="primary" :icon="Plus" @click="goToCreateAgent">
                  创建智能体
                </el-button>
              </el-empty>
            </div>
          </div>

          <!-- Right Column: Dashboard Widgets -->
          <aside class="right-panel">
            <!-- Widget 1: Quick Actions -->
            <div class="dashboard-widget quick-actions-widget">
              <div class="widget-header">
                <h3>快捷操作</h3>
              </div>
              <div class="widget-content">
                <div class="quick-action-grid">
                  <div class="action-item" @click="goToCreateAgent">
                    <div class="action-icon bg-blue-50 text-blue-600">
                      <el-icon><Plus /></el-icon>
                    </div>
                    <span>新建智能体</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Widget 2: Recent Activity -->
            <div class="dashboard-widget activity-widget">
              <div class="widget-header">
                <h3>最近活动</h3>
              </div>
              <div class="widget-content scrollable">
                <el-timeline>
                  <el-timeline-item
                    v-for="(activity, index) in recentActivities"
                    :key="index"
                    :type="activity.type"
                    :color="activity.color"
                    :timestamp="activity.timestamp"
                    hide-timestamp
                  >
                    <div class="activity-content">
                      <div class="activity-title">{{ activity.content }}</div>
                      <div class="activity-time">{{ activity.timestamp }}</div>
                    </div>
                  </el-timeline-item>
                </el-timeline>
              </div>
            </div>
          </aside>
        </div>
      </main>
    </div>
  </BaseLayout>
</template>

<script lang="ts">
import { defineComponent, ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Grid,
  Check,
  Edit,
  VideoPause,
  Delete,
  Search,
  Refresh,
  Plus,
  Clock,
  Download,
  Files,
  DocumentCopy,
} from '@element-plus/icons-vue';
import BaseLayout from '@/layouts/BaseLayout.vue';
import agentService from '@/services/agent';
import type { Agent } from '@/services/agent';

export default defineComponent({
  name: 'AgentList',
  components: {
    BaseLayout,
    Grid,
    Check,
    Edit,
    VideoPause,
    Delete,
    Plus,
    Download,
    Files,
    DocumentCopy,
  },
  setup() {
    const router = useRouter();
    const loading = ref(true);
    const activeFilter = ref('all');
    const searchKeyword = ref('');
    const agents = ref<Agent[]>([]);

    // Mock Data for Right Sidebar
    const customColors = [
      { color: '#f56c6c', percentage: 20 },
      { color: '#e6a23c', percentage: 40 },
      { color: '#5cb87a', percentage: 60 },
      { color: '#1989fa', percentage: 80 },
      { color: '#6f7ad3', percentage: 100 },
    ];

    const recentActivities = ref<
      {
        content: string;
        timestamp: string;
        type: 'primary' | 'success' | 'warning' | 'info' | 'danger';
        color: string;
      }[]
    >([]);

    const updateRecentActivities = () => {
      if (!agents.value || agents.value.length === 0) {
        recentActivities.value = [
          {
            content: '暂无活动记录',
            timestamp: '',
            type: 'info',
            color: '#909399',
          },
        ];
        return;
      }

      // 按更新时间降序排序
      const sortedAgents = [...agents.value].sort((a, b) => {
        const timeA = new Date(a.updateTime).getTime();
        const timeB = new Date(b.updateTime).getTime();
        return timeB - timeA;
      });

      // 取前5条记录
      recentActivities.value = sortedAgents.slice(0, 5).map((agent, index) => {
        const types: ('primary' | 'success' | 'warning' | 'info')[] = [
          'primary',
          'success',
          'warning',
          'info',
        ];
        return {
          content: `更新了智能体 "${agent.name}"`,
          timestamp: formatTime(agent.updateTime),
          type: types[index % types.length],
          color: index === 0 ? '#3b82f6' : '', // 第一条高亮
        };
      });
    };

    // 计算属性
    const publishedCount = computed(
      () => agents.value.filter((a: Agent) => a.status === 'published').length,
    );
    const draftCount = computed(
      () => agents.value.filter((a: Agent) => a.status === 'draft').length,
    );
    const offlineCount = computed(
      () => agents.value.filter((a: Agent) => a.status === 'offline').length,
    );

    const filteredAgents = computed(() => {
      let filtered = agents.value;

      // 按状态过滤
      if (activeFilter.value !== 'all') {
        filtered = filtered.filter((agent: Agent) => agent.status === activeFilter.value);
      }

      // 按关键词搜索
      if (searchKeyword.value.trim()) {
        const keyword = searchKeyword.value.toLowerCase();
        filtered = filtered.filter(
          (agent: Agent) =>
            agent.name.toLowerCase().includes(keyword) ||
            agent.description.toLowerCase().includes(keyword) ||
            agent.id.toString().includes(keyword),
        );
      }

      return filtered;
    });

    const setFilter = (filter: string) => {
      activeFilter.value = filter;
    };

    const loadAgents = async () => {
      loading.value = true;
      try {
        const response = await agentService.list();
        agents.value = response || [];
      } catch (error) {
        ElMessage.error('获取智能体列表失败，请检查网络！');
        agents.value = [];
      } finally {
        loading.value = false;
        // 更新最近活动
        updateRecentActivities();
      }
    };

    const enterAgent = (agentId: string) => {
      router.push(`/agent/${agentId}`);
    };

    const getStatusText = (status: string) => {
      const statusMap: Record<string, string> = {
        published: '已发布',
        draft: '草稿',
        offline: '已下线',
      };
      return statusMap[status] || status;
    };

    const getStatusTagType = (status: string) => {
      const typeMap: Record<string, 'success' | 'warning' | 'info'> = {
        published: 'success',
        draft: 'warning',
        offline: 'info',
      };
      return typeMap[status] || 'info';
    };

    const formatTime = (time: string) => {
      if (!time) return '';
      return time.replace(/\//g, '/');
    };

    const goToCreateAgent = () => {
      router.push('/agent/create');
    };

    // 删除智能体
    const handleDeleteAgent = async (agent: Agent) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除智能体 "${agent.name}" 吗？此操作不可恢复。`,
          '删除确认',
          {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'warning',
          },
        );

        const success = await agentService.delete(agent.id!);
        if (success) {
          ElMessage.success('智能体删除成功');
          // 从列表中移除已删除的智能体
          agents.value = agents.value.filter((a: Agent) => a.id !== agent.id);
        } else {
          ElMessage.error('智能体删除失败');
        }
      } catch (error) {
        // 用户取消了删除操作
        console.log('删除操作已取消');
      }
    };

    // 生命周期
    onMounted(() => {
      loadAgents();
    });

    return {
      loading,
      activeFilter,
      searchKeyword,
      agents,
      filteredAgents,
      publishedCount,
      draftCount,
      offlineCount,
      setFilter,
      loadAgents,
      enterAgent,
      getStatusText,
      getStatusTagType,
      formatTime,
      goToCreateAgent,
      handleDeleteAgent,
      Search,
      Refresh,
      Plus,
      customColors,
      recentActivities,
    };
  },
});
</script>

<style scoped>
.agent-list-page {
  min-height: 100vh;
  /* Updated Background: Cleaner, smoother, deeper */
  background-color: var(--bg-color);
  background-image: radial-gradient(at 0% 0%, hsla(253, 16%, 7%, 0) 0, transparent 50%),
    radial-gradient(at 50% 0%, hsla(225, 39%, 30%, 0.05) 0, transparent 50%),
    radial-gradient(at 100% 0%, hsla(339, 49%, 30%, 0) 0, transparent 50%);
  padding-bottom: 2rem;
  position: relative;
}

html.dark .agent-list-page {
  background-image: none;
}

/* Subtle decorative gradient blobs */
.agent-list-page::before {
  content: '';
  position: absolute;
  top: -10%;
  right: -5%;
  width: 40%;
  height: 40%;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.08) 0%, rgba(255, 255, 255, 0) 70%);
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;
}

html.dark .agent-list-page::before,
html.dark .agent-list-page::after {
  opacity: 0.3;
}

.agent-list-page::after {
  content: '';
  position: absolute;
  bottom: 10%;
  left: -5%;
  width: 30%;
  height: 30%;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.08) 0%, rgba(255, 255, 255, 0) 70%);
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;
}

/* 主内容区域 */
.main-content {
  width: 100%;
  margin: 0 auto;
  padding: 1.5rem 2rem;
  max-width: 1600px;
  position: relative;
  z-index: 1;
}

/* Layout Grid */
.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  align-items: start;
}

@media (max-width: 1280px) {
  .dashboard-grid {
    grid-template-columns: 1fr; /* Stack on smaller screens */
  }
  .right-panel {
    display: none; /* Hide sidebar on small screens or move to bottom */
  }
}

.left-panel {
  min-width: 0; /* Prevent flex overflow issues */
}

/* 内容头部 */
.content-header {
  margin-bottom: 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info {
  position: relative;
  padding-left: 1rem;
}

.header-info::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 4px;
  background: linear-gradient(to bottom, #3b82f6, #8b5cf6);
  border-radius: 2px;
}

.content-title {
  font-size: 2rem;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
  letter-spacing: -0.03em;
  background: linear-gradient(135deg, var(--text-primary) 0%, #3b82f6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.content-subtitle {
  color: var(--text-secondary);
  margin: 0;
  font-size: 1rem;
}

/* 统计卡片区域 */
.stats-section {
  margin-bottom: 2rem;
}

.stat-card {
  border: 1px solid var(--border-color);
  border-radius: 16px;
  height: 100%;
  transition: all 0.3s ease;
  background: var(--card-bg-color);
  backdrop-filter: blur(12px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02);
  position: relative;
  overflow: hidden;
}

html.dark .stat-card {
  background: rgba(30, 30, 30, 0.6);
  border-color: rgba(255, 255, 255, 0.1);
}

.stat-card:hover {
  transform: translateY(-2px);
  background: var(--card-bg-color);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05);
}

html.dark .stat-card:hover {
  background: rgba(30, 30, 30, 0.8);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 1.25rem;
  position: relative;
  z-index: 2;
}

.stat-decoration {
  position: absolute;
  right: -10px;
  bottom: -20px;
  font-size: 6rem;
  opacity: 0.05;
  transform: rotate(-15deg);
  pointer-events: none;
  z-index: 0;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.75rem;
}

.bg-blue-100 {
  background: #eff6ff;
  color: #3b82f6;
}
html.dark .bg-blue-100 {
  background: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
}

.bg-green-100 {
  background: #f0fdf4;
  color: #22c55e;
}
html.dark .bg-green-100 {
  background: rgba(34, 197, 94, 0.2);
  color: #4ade80;
}

.bg-orange-100 {
  background: #fff7ed;
  color: #f97316;
}
html.dark .bg-orange-100 {
  background: rgba(249, 115, 22, 0.2);
  color: #fb923c;
}

.bg-gray-100 {
  background: #f8fafc;
  color: #64748b;
}
html.dark .bg-gray-100 {
  background: rgba(148, 163, 184, 0.2);
  color: #94a3b8;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  font-weight: 600;
  text-transform: uppercase;
}

.stat-number {
  font-size: 1.875rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

/* 过滤和搜索区域 */
.filter-section {
  margin-bottom: 1.5rem;
}

.filter-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
  padding: 0.5rem;
  background: var(--card-bg-color);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.custom-radio-group :deep(.el-radio-button__inner) {
  border: none;
  background: transparent;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  color: var(--text-secondary);
  box-shadow: none !important;
}

.custom-radio-group :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: var(--menu-active-bg);
  color: #3b82f6;
  font-weight: 600;
}

html.dark
  .custom-radio-group
  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  color: #60a5fa;
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding-right: 0.5rem;
}

.search-input {
  width: 280px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  background: var(--bg-color);
  box-shadow: none;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  background: var(--card-bg-color);
  box-shadow: 0 0 0 1px #3b82f6 inset;
}

.icon-btn {
  border: none;
  background: var(--bg-color);
  color: var(--text-secondary);
}

.create-btn {
  border-radius: 8px;
  padding-left: 1.5rem;
  padding-right: 1.5rem;
  background: #3b82f6;
  border: none;
}

/* 智能体卡片 */
.agents-grid {
  margin-bottom: 2rem;
}

.agent-card-wrapper {
  height: 100%;
  margin-bottom: 1.5rem;
}

.agent-card {
  background: var(--card-bg-color);
  border-radius: 16px;
  border: 1px solid var(--border-color);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.agent-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 4px 6px -2px rgba(0, 0, 0, 0.025);
  border-color: #cbd5e1;
}

html.dark .agent-card:hover {
  border-color: #475569;
}

/* 装饰性背景元素 */
.card-bg-decoration {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  z-index: -1;
  opacity: 0;
  transition: all 0.5s ease;
}

.card-bg-decoration.top-right {
  top: -20%;
  right: -20%;
  width: 150px;
  height: 150px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.2) 0%, rgba(255, 255, 255, 0) 70%);
}

.card-bg-decoration.bottom-left {
  bottom: -20%;
  left: -20%;
  width: 150px;
  height: 150px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.2) 0%, rgba(255, 255, 255, 0) 70%);
}

.agent-card:hover .card-bg-decoration {
  opacity: 1;
  transform: scale(1.2);
}

.card-header-row {
  padding: 1.25rem;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid var(--border-color);
  background: var(--card-bg-color);
  backdrop-filter: blur(8px);
  position: relative;
  z-index: 2;
}

.header-left {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.agent-avatar {
  background: #3b82f6;
  font-size: 1.25rem;
  font-weight: 700;
  flex-shrink: 0;
}

.header-text {
  flex: 1;
  min-width: 0;
}

.agent-name {
  margin: 0 0 0.25rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.agent-id-row {
  display: flex;
  align-items: center;
}

.agent-id-badge {
  font-size: 0.75rem;
  color: var(--text-secondary);
  font-family: monospace;
}

.card-body {
  padding: 1.25rem;
  flex: 1;
}

.agent-desc {
  margin: 0;
  font-size: 0.875rem;
  color: var(--text-secondary);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 2.625rem; /* fixed height for 2 lines */
}

.card-footer {
  padding: 0.75rem 1.25rem;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--card-bg-color);
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.75rem;
}

.action-btn {
  padding: 4px;
  height: auto;
  color: var(--text-secondary);
}

.action-btn:hover {
  color: #ef4444;
  background: #fee2e2;
  border-radius: 4px;
}

html.dark .action-btn:hover {
  background: rgba(239, 68, 68, 0.2);
}

/* Right Sidebar Widgets */
.right-panel {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.dashboard-widget {
  background: var(--card-bg-color);
  border-radius: 16px;
  border: 1px solid var(--border-color);
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.widget-header {
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--bg-color);
}

.widget-header h3 {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--text-primary);
}

.widget-content {
  padding: 1.25rem;
}

/* Quick Actions Widget */
.quick-action-grid {
  display: flex;
  gap: 1rem;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  padding: 0.75rem 1rem;
  border-radius: 12px;
  background-color: var(--bg-color);
  transition: all 0.2s ease;
  flex: 1;
}

.action-item:hover {
  background-color: var(--menu-active-bg);
  transform: translateY(-2px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.action-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
}

.action-item span {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.action-item:hover span {
  color: #2563eb;
}

html.dark .action-item:hover span {
  color: #60a5fa;
}

.bg-blue-50 {
  background: #eff6ff;
}
html.dark .bg-blue-50 {
  background: rgba(59, 130, 246, 0.1);
}

.bg-purple-50 {
  background: #f3e8ff;
}
html.dark .bg-purple-50 {
  background: rgba(168, 85, 247, 0.1);
}

.bg-green-50 {
  background: #f0fdf4;
}
html.dark .bg-green-50 {
  background: rgba(34, 197, 94, 0.1);
}

.bg-orange-50 {
  background: #fff7ed;
}
html.dark .bg-orange-50 {
  background: rgba(249, 115, 22, 0.1);
}

/* System Status Widget */
.status-item {
  margin-bottom: 1rem;
}

.status-item:last-child {
  margin-bottom: 0;
}

.status-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.25rem;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.status-value {
  font-weight: 600;
  color: var(--text-primary);
}

/* Recent Activity Widget */
.activity-widget .widget-content {
  padding: 1rem;
  max-height: 300px;
  overflow-y: auto;
}

.activity-content {
  display: flex;
  flex-direction: column;
}

.activity-title {
  font-size: 0.85rem;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.activity-time {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

/* Scrollbar for widgets */
.scrollable::-webkit-scrollbar {
  width: 4px;
}

.scrollable::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 2px;
}
</style>
