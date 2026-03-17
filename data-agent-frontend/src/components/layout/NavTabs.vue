<template>
  <div class="nav-tabs-bar">
    <el-scrollbar class="tabs-scrollbar">
      <div class="tabs-content">
        <div
          v-for="tab in tabsStore.visitedTabs"
          :key="tab.path"
          :class="['tab-item', { 'is-active': isActive(tab), 'is-affix': tab.affix }]"
          @click="handleTabClick(tab)"
        >
          <i v-if="tab.icon" :class="[tab.icon, 'tab-icon']"></i>
          <span class="tab-title">{{ tab.title }}</span>
          <span
            v-if="!tab.affix"
            class="tab-close"
            @click.stop="handleClose(tab)"
          >
            <i class="bi bi-x"></i>
          </span>
        </div>
      </div>
    </el-scrollbar>

    <el-dropdown @command="handleCommand" trigger="click">
      <div class="tabs-more">
        <i class="bi bi-chevron-down"></i>
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="closeOthers">
            <i class="bi bi-x-circle me-1"></i> 关闭其他
          </el-dropdown-item>
          <el-dropdown-item command="closeAll">
            <i class="bi bi-x-square me-1"></i> 关闭所有
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useTabsStore } from '@/stores/tabs';
import type { TabItem } from '@/stores/tabs';

const route = useRoute();
const router = useRouter();
const tabsStore = useTabsStore();

const isActive = (tab: TabItem) => tab.path === route.path;

const addCurrentTab = () => {
  if (route.name && route.meta?.title) {
    tabsStore.addTab({
      name: String(route.name),
      path: route.path,
      title: String(route.meta.title),
      icon: route.meta.icon as string | undefined,
    });
  }
};

const handleTabClick = (tab: TabItem) => {
  if (route.path !== tab.path) {
    router.push(tab.path);
  }
};

const handleClose = (tab: TabItem) => {
  const nextPath = tabsStore.closeTab(tab.path);
  if (isActive(tab)) {
    router.push(nextPath);
  }
};

const handleCommand = (command: string) => {
  if (command === 'closeOthers') {
    tabsStore.closeOtherTabs(route.path);
  } else if (command === 'closeAll') {
    const nextPath = tabsStore.closeAllTabs();
    router.push(nextPath);
  }
};

// 监听路由变化，自动添加标签
watch(() => route.path, () => {
  addCurrentTab();
}, { immediate: true });

onMounted(() => {
  tabsStore.initAffixTabs();
  addCurrentTab();
});
</script>

<style scoped>
.nav-tabs-bar {
  display: flex;
  align-items: center;
  background: var(--header-bg-color);
  border-bottom: 1px solid var(--border-color);
  height: 40px;
  padding: 0 8px 0 16px;
  gap: 8px;
  flex-shrink: 0;
}

.tabs-scrollbar {
  flex: 1;
  height: 40px;
}

:deep(.el-scrollbar__wrap) {
  display: flex;
  align-items: center;
}

.tabs-content {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 0;
  white-space: nowrap;
}

.tab-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  background: var(--bg-color);
  border: 1px solid var(--border-color);
  transition: all 0.2s ease;
  user-select: none;
  flex-shrink: 0;
}

.tab-item:hover {
  color: var(--primary-color);
  border-color: var(--primary-color);
  background: var(--primary-bg);
}

.tab-item.is-active {
  color: white;
  background: var(--primary-color);
  border-color: var(--primary-color);
}

.tab-icon {
  font-size: 12px;
}

.tab-title {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tab-close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  margin-left: 2px;
  transition: all 0.2s;
  opacity: 0.7;
}

.tab-close:hover {
  background: rgba(0, 0, 0, 0.15);
  opacity: 1;
}

.tab-item.is-active .tab-close:hover {
  background: rgba(255, 255, 255, 0.25);
}

.tab-close .bi-x {
  font-size: 13px;
  line-height: 1;
}

.tabs-more {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.2s;
  flex-shrink: 0;
}

.tabs-more:hover {
  background: var(--menu-hover-bg);
  color: var(--primary-color);
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}
</style>
