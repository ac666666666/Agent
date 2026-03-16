<template>
  <div class="nav-tabs-container" v-if="visitedViews.length > 0">
    <div class="tabs-wrapper">
      <el-scrollbar>
        <div class="tabs-content">
          <div
            v-for="tag in visitedViews"
            :key="tag.path"
            :class="['tab-item', { 'is-active': isActive(tag) }]"
            @click="handleTabClick(tag)"
            @contextmenu.prevent="openContextMenu($event, tag)"
          >
            <i v-if="tag.icon" :class="tag.icon"></i>
            <span class="tab-title">{{ tag.title }}</span>
            <i 
              v-if="!isAffix(tag)"
              class="bi bi-x tab-close" 
              @click.stop="closeTab(tag)"
            ></i>
          </div>
        </div>
      </el-scrollbar>
    </div>
    
    <div class="tabs-actions">
      <el-dropdown @command="handleCommand">
        <div class="action-trigger">
          <i class="bi bi-chevron-down"></i>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="closeOthers">
              <i class="bi bi-x-circle"></i>
              关闭其他
            </el-dropdown-item>
            <el-dropdown-item command="closeAll">
              <i class="bi bi-x-square"></i>
              关闭所有
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const visitedViews = ref([]);
const affixTags = ref([]);

// 固定的标签页（不可关闭）
const isAffix = (tag) => {
  return tag.meta && tag.meta.affix;
};

const isActive = (tag) => {
  return tag.path === route.path;
};

const addView = () => {
  const { name, path, meta } = route;
  if (name && !visitedViews.value.some(v => v.path === path)) {
    visitedViews.value.push({
      name,
      path,
      title: meta.title || 'No Title',
      icon: meta.icon,
      meta,
    });
  }
};

const closeTab = (tag) => {
  const index = visitedViews.value.findIndex(v => v.path === tag.path);
  if (index > -1) {
    visitedViews.value.splice(index, 1);
    
    // 如果关闭的是当前标签，跳转到最后一个标签
    if (isActive(tag) && visitedViews.value.length > 0) {
      const lastView = visitedViews.value[visitedViews.value.length - 1];
      router.push(lastView.path);
    }
  }
};

const handleTabClick = (tag) => {
  if (route.path !== tag.path) {
    router.push(tag.path);
  }
};

const handleCommand = (command) => {
  if (command === 'closeOthers') {
    visitedViews.value = visitedViews.value.filter(
      v => isActive(v) || isAffix(v)
    );
  } else if (command === 'closeAll') {
    visitedViews.value = visitedViews.value.filter(v => isAffix(v));
    if (visitedViews.value.length > 0) {
      router.push(visitedViews.value[0].path);
    } else {
      router.push('/');
    }
  }
};

watch(route, () => {
  addView();
});

onMounted(() => {
  addView();
});
</script>

<style scoped>
.nav-tabs-container {
  display: flex;
  align-items: center;
  background: var(--header-bg-color);
  border-bottom: 1px solid var(--border-color);
  height: 40px;
  padding: 0 12px;
  gap: 8px;
}

.tabs-wrapper {
  flex: 1;
  overflow: hidden;
}

.tabs-content {
  display: flex;
  gap: 6px;
  padding: 4px 0;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: var(--bg-color);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  font-size: 13px;
  color: var(--text-secondary);
}

.tab-item:hover {
  background: var(--card-hover-bg);
  color: var(--text-primary);
}

.tab-item.is-active {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

.tab-title {
  font-weight: 500;
}

.tab-close {
  font-size: 14px;
  margin-left: 4px;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.tab-close:hover {
  opacity: 1;
}

.tabs-actions {
  flex-shrink: 0;
}

.action-trigger {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--text-secondary);
}

.action-trigger:hover {
  background: var(--menu-hover-bg);
  color: var(--primary-color);
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
