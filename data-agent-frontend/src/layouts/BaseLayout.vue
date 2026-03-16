<template>
  <el-container class="base-layout">
    <!-- 侧边栏 -->
    <Sidebar />

    <!-- 主内容区域 -->
    <el-container class="main-container">
      <el-header class="page-header">
        <div class="header-content">
          <!-- 这里可以放置面包屑、用户信息、设置等 -->
          <div class="breadcrumb-placeholder">
            <!-- 示例：当前页面标题 -->
            <span class="page-title">{{ currentPageTitle }}</span>
          </div>

          <div class="header-actions">
            <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link">
                <el-avatar :size="32" :src="userStore.userAvatar" v-if="userStore.userAvatar" />
                <el-avatar :size="32" icon="UserFilled" v-else />
                <span class="username">{{ userStore.username }}</span>
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <el-main class="page-content">
        <slot></slot>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { UserFilled, ArrowDown } from '@element-plus/icons-vue';
import Sidebar from '@/components/layout/Sidebar.vue';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const currentPageTitle = computed(() => {
  return route.meta.title || 'Data Agent';
});

const handleCommand = command => {
  if (command === 'logout') {
    userStore.logout();
    router.push('/login');
  } else if (command === 'profile') {
    router.push('/profile');
  }
};

onMounted(() => {
  userStore.checkAuth();
});
</script>

<style scoped>
.base-layout {
  min-height: 100vh;
  background-color: var(--bg-color);
}

.main-container {
  margin-left: 240px; /* Width of the sidebar */
  transition: margin-left 0.3s;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.page-header {
  background: var(--header-bg-color);
  border-bottom: 1px solid var(--border-color);
  height: 64px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 99;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.header-actions {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
}

.username {
  font-size: 14px;
  font-weight: 500;
}

.page-content {
  flex: 1;
  padding: 0;
  overflow-y: auto;
}
</style>
