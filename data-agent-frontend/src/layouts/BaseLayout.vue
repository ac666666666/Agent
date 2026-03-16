<template>
  <el-container class="base-layout">
    <!-- 侧边栏 -->
    <Sidebar />

    <!-- 主内容区域 -->
    <el-container class="main-container">
      <!-- 顶部导航栏 -->
      <el-header class="page-header">
        <div class="header-content">
          <!-- 面包屑导航 -->
          <Breadcrumb />

          <!-- 右侧操作区 -->
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

      <!-- 标签页导航 -->
      <NavTabs />

      <!-- 主内容 -->
      <el-main class="page-content">
        <slot></slot>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { UserFilled, ArrowDown } from '@element-plus/icons-vue';
import Sidebar from '@/components/layout/Sidebar.vue';
import Breadcrumb from '@/components/layout/Breadcrumb.vue';
import NavTabs from '@/components/layout/NavTabs.vue';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();

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
  height: 56px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 99;
  box-shadow: var(--shadow-sm);
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 16px;
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
