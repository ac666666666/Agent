<template>
  <el-aside :width="isCollapsed ? '80px' : '240px'" class="sidebar" :class="{ 'is-collapsed': isCollapsed }">
    <div class="logo-container">
      <i class="bi bi-robot logo-icon"></i>
      <transition name="fade">
        <span v-show="!isCollapsed" class="logo-text">{{ systemStore.systemName || 'Data Agent' }}</span>
      </transition>
      <!-- 收缩按钮 -->
      <div class="collapse-trigger" @click="toggleCollapse">
        <i class="bi" :class="isCollapsed ? 'bi-chevron-right' : 'bi-chevron-left'"></i>
      </div>
    </div>

    <el-menu 
      :default-active="activeMenu" 
      class="sidebar-menu" 
      :router="true"
      :collapse="isCollapsed"
      :collapse-transition="false"
    >
      <div v-show="!isCollapsed" class="menu-group-title">概览</div>
      <el-menu-item index="/dashboard">
        <el-icon><i class="bi bi-speedometer2"></i></el-icon>
        <template #title><span>仪表盘</span></template>
      </el-menu-item>

      <div v-show="!isCollapsed" class="menu-group-title">智能体管理</div>
      <el-menu-item index="/agents">
        <el-icon><i class="bi bi-robot"></i></el-icon>
        <template #title><span>我的智能体</span></template>
      </el-menu-item>
      <el-menu-item index="/agent-market" disabled>
        <el-icon><i class="bi bi-shop"></i></el-icon>
        <template #title>
          <span>智能体市场</span>
          <el-tag size="small" type="info" class="menu-tag">Dev</el-tag>
        </template>
      </el-menu-item>

      <div v-show="!isCollapsed" class="menu-group-title">知识库</div>
      <el-menu-item index="/knowledge">
        <el-icon><i class="bi bi-book"></i></el-icon>
        <template #title><span>知识库管理</span></template>
      </el-menu-item>
      <el-menu-item index="/datasources">
        <el-icon><i class="bi bi-database"></i></el-icon>
        <template #title><span>数据源连接</span></template>
      </el-menu-item>

      <div v-show="!isCollapsed" class="menu-group-title">系统设置</div>
      <el-menu-item index="/model-config">
        <el-icon><i class="bi bi-sliders"></i></el-icon>
        <template #title><span>模型配置</span></template>
      </el-menu-item>
      <el-menu-item index="/settings">
        <el-icon><i class="bi bi-gear"></i></el-icon>
        <template #title><span>通用设置</span></template>
      </el-menu-item>
      <el-menu-item index="/profile">
        <el-icon><i class="bi bi-person-circle"></i></el-icon>
        <template #title><span>个人中心</span></template>
      </el-menu-item>
    </el-menu>

    <!-- 底部用户信息 -->
    <div class="sidebar-footer">
      <div class="user-profile" :class="{ 'is-collapsed': isCollapsed }">
        <el-avatar
          :size="32"
          :src="userStore.userAvatar"
          v-if="userStore.userAvatar"
          class="user-avatar"
        />
        <el-avatar :size="32" icon="UserFilled" v-else class="user-avatar" />
        <transition name="fade">
          <div v-show="!isCollapsed" class="user-info">
            <div class="user-name">{{ userStore.username }}</div>
            <div class="user-role">{{ userStore.userRole }}</div>
          </div>
        </transition>
        <transition name="fade">
          <el-icon v-show="!isCollapsed" class="logout-icon" @click.stop="handleLogout">
            <i class="bi bi-box-arrow-right"></i>
          </el-icon>
        </transition>
      </div>
    </div>
  </el-aside>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useSystemStore } from '@/stores/system';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const systemStore = useSystemStore();
const userStore = useUserStore();
const isCollapsed = ref(false);

const activeMenu = computed(() => {
  // Map specific routes to their parent menu item if needed
  if (route.path.startsWith('/agent/')) {
    return '/agents';
  }
  return route.path;
});

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value;
};

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
  transition: width 0.3s ease;
}

/* 收缩按钮 */
.collapse-trigger {
  width: 32px;
  height: 32px;
  background: transparent;
  border: 2px solid var(--border-color);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
  margin-left: auto;
}

.sidebar.is-collapsed .collapse-trigger {
  margin-left: 0;
  border-radius: 50%;
}

.collapse-trigger:hover {
  background: var(--primary-color);
  border-color: var(--primary-color);
  transform: scale(1.05);
}

.collapse-trigger:hover i {
  color: white;
}

.collapse-trigger i {
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: bold;
  transition: color 0.3s ease;
}

.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid var(--border-color);
  gap: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.sidebar.is-collapsed .logo-container {
  justify-content: center;
  padding: 0 20px;
  gap: 0;
}

.logo-icon {
  font-size: 24px;
  color: var(--primary-color);
  flex-shrink: 0;
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
  overflow-x: hidden;
}

:deep(.el-menu) {
  background-color: transparent;
  border-right: none;
}

:deep(.el-menu--collapse) {
  width: 80px;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 4px 12px;
  border-radius: 8px;
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

.sidebar.is-collapsed :deep(.el-menu-item) {
  margin: 4px 12px;
  padding: 0 !important;
  justify-content: center;
}

:deep(.el-menu-item.is-active) {
  background: var(--menu-active-bg);
  color: var(--menu-active-text);
  font-weight: 600;
}

.sidebar.is-collapsed :deep(.el-menu-item.is-active) {
  border-left: none;
  border-radius: 8px;
}

.sidebar:not(.is-collapsed) :deep(.el-menu-item.is-active) {
  border-left: 3px solid var(--menu-active-border);
}

html.dark :deep(.el-menu-item.is-active) {
  color: var(--menu-active-text);
}

:deep(.el-menu-item:hover) {
  background-color: var(--menu-hover-bg);
  transform: translateX(2px);
}

.sidebar.is-collapsed :deep(.el-menu-item:hover) {
  transform: scale(1.05);
}

:deep(.el-icon) {
  font-size: 18px;
  margin-right: 12px;
}

.sidebar.is-collapsed :deep(.el-icon) {
  margin-right: 0;
}

.menu-group-title {
  padding: 12px 24px 8px;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  transition: opacity 0.3s;
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

.sidebar.is-collapsed .sidebar-footer {
  padding: 16px 12px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-profile.is-collapsed {
  justify-content: center;
  padding: 8px;
  gap: 0;
}

.user-profile:hover {
  background: var(--header-bg-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.user-avatar {
  background: var(--primary-color);
  color: white;
  flex-shrink: 0;
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
  flex-shrink: 0;
}

.logout-icon:hover {
  color: var(--error-color);
}

/* 淡入淡出动画 */
.fade-enter-active {
  transition: opacity 0.3s ease 0.1s;
}

.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 图标动画 */
:deep(.el-icon) {
  font-size: 18px;
  margin-right: 12px;
  transition: all 0.3s ease;
}

.sidebar.is-collapsed :deep(.el-icon) {
  margin-right: 0;
  font-size: 20px;
}
</style>
