import { defineStore } from 'pinia';
import { ref } from 'vue';

export interface TabItem {
  name: string;
  path: string;
  title: string;
  icon?: string;
  affix?: boolean;
}

export const useTabsStore = defineStore('tabs', () => {
  const visitedTabs = ref<TabItem[]>([]);

  // 初始化固定标签
  const initAffixTabs = () => {
    if (!visitedTabs.value.some(t => t.path === '/dashboard')) {
      visitedTabs.value.unshift({
        name: 'Dashboard',
        path: '/dashboard',
        title: '仪表盘',
        icon: 'bi bi-speedometer2',
        affix: true,
      });
    }
  };

  const addTab = (tab: TabItem) => {
    // 不记录登录、注册、404 页面
    if (['/login', '/register'].includes(tab.path)) return;
    if (tab.path.includes('/:pathMatch')) return;

    if (!visitedTabs.value.some(t => t.path === tab.path)) {
      visitedTabs.value.push({ ...tab });
    }
  };

  const closeTab = (path: string) => {
    const index = visitedTabs.value.findIndex(t => t.path === path);
    if (index > -1 && !visitedTabs.value[index].affix) {
      visitedTabs.value.splice(index, 1);
    }
    // 返回关闭后应该跳转的路径
    if (visitedTabs.value.length > 0) {
      return visitedTabs.value[Math.min(index, visitedTabs.value.length - 1)].path;
    }
    return '/dashboard';
  };

  const closeOtherTabs = (path: string) => {
    visitedTabs.value = visitedTabs.value.filter(t => t.path === path || t.affix);
  };

  const closeAllTabs = () => {
    visitedTabs.value = visitedTabs.value.filter(t => t.affix);
    return visitedTabs.value.length > 0 ? visitedTabs.value[0].path : '/dashboard';
  };

  return {
    visitedTabs,
    initAffixTabs,
    addTab,
    closeTab,
    closeOtherTabs,
    closeAllTabs,
  };
});
