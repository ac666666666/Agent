<template>
  <el-aside width="240px" class="sidebar">
    <div class="logo-container">
      <i class="bi bi-robot logo-icon"></i>
      <span class="logo-text">{{ systemStore.systemName || 'Data Agent' }}</span>
    </div>

    <el-menu :default-active="activeMenu" class="sidebar-menu" :router="true">
      <div class="menu-group-title">概览</div>
      <el-menu-item index="/dashboard">
        <el-icon><i class="bi bi-speedometer2"></i></el-icon>
        <span>仪表盘</span>
      </el-menu-item>

      <div class="menu-group-title">智能体管理</div>
      <el-menu-item index="/agents">
        <el-icon><i class="bi bi-robot"></i></el-icon>
        <span>我的智能体</span>
      </el-menu-item>
      <el-menu-item index="/agent-market" disabled>
        <el-icon><i class="bi bi-shop"></i></el-icon>
        <span>智能体市场</span>
        <el-tag size="small" type="info" class="menu-tag">Dev</el-tag>
      </el-menu-item>

      <div class="menu-group-title">知识库</div>
      <el-menu-item index="/knowledge">
        <el-icon><i class="bi bi-book"></i></el-icon>
        <span>知识库管理</span>
      </el-menu-item>
      <el-menu-item index="/datasources">
        <el-icon><i class="bi bi-database"></i></el-icon>
        <span>数据源连接</span>
      </el-menu-item>

      <div class="menu-group-title">系统设置</div>
      <el-menu-item index="/model-config">
        <el-icon><i class="bi bi-sliders"></i></el-icon>
        <span>模型配置</span>
      </el-menu-item>
      <el-menu-item index="/settings">
        <el-icon><i class="bi bi-gear"></i></el-icon>
        <span>通用设置</span>
      </el-menu-item>
      <el-menu-item index="/profile">
        <el-icon><i class="bi bi-person-circle"></i></el-icon>
        <span>个人中心</span>
      </el-menu-item>
    </el-menu>

    <!-- 底部用户信息 -->
    <div class="sidebar-footer">
      <div class="user-profile">
        <el-avatar
          :size="32"
          :src="userStore.userAvatar"
          v-if="userStore.userAvatar"
          class="user-avatar"
        />
        <el-avatar :size="32" icon="UserFilled" v-else class="user-avatar" />
        <div class="user-info">
          <div class="user-name">{{ userStore.username }}</div>
          <div class="user-role">{{ userStore.userRole }}</div>
        </div>
        <el-icon class="logout-icon" @click.stop="handleLogout">
          <i class="bi bi-box-arrow-right"></i>
        </el-icon>
      </div>
    </div>
  </el-aside>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useSystemStore } from '@/stores/system';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const systemStore = useSystemStore();
const userStore = useUserStore();

const activeMenu = computed(() => {
  // Map specific routes to their parent menu item if needed
  if (route.path.startsWith('/agent/')) {
    return '/agents';
  }
  return route.path;
});

const handleLogout = () => {
  userStore.logout();
  router.push('/login');
};
</script>

<style scoped>
.sidebar {
  background-color: var(--header-bg-color);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 1000;
  transition: width 0.3s;
}

.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid var(--border-color);
  gap: 12px;
}

.logo-icon {
  font-size: 24px;
  color: #1e40af; /* Primary color from design system */
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
}

.sidebar-menu {
  border-right: none;
  background-color: transparent;
  flex: 1;
  padding-top: 10px;
}

:deep(.el-menu) {
  background-color: transparent;
  border-right: none;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 4px 12px;
  border-radius: 8px;
  color: var(--text-secondary);
}

:deep(.el-menu-item.is-active) {
  background-color: var(--menu-active-bg);
  color: #1e40af;
  font-weight: 600;
}

html.dark :deep(.el-menu-item.is-active) {
  color: #60a5fa;
}

:deep(.el-menu-item:hover) {
  background-color: var(--menu-hover-bg);
}

:deep(.el-icon) {
  font-size: 18px;
  margin-right: 12px;
}

.menu-group-title {
  padding: 12px 24px 8px;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.menu-tag {
  margin-left: auto;
  transform: scale(0.8);
  border: none;
  background: var(--bg-color);
  color: var(--text-secondary);
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid var(--border-color);
  background: var(--bg-color);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.user-profile:hover {
  background: var(--header-bg-color);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.user-avatar {
  background: #3b82f6;
  color: white;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
}

.user-role {
  font-size: 0.75rem;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.logout-icon {
  color: var(--text-secondary);
  font-size: 1.25rem;
  cursor: pointer;
  transition: color 0.2s;
}

.logout-icon:hover {
  color: #ef4444;
}
</style>
