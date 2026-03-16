<template>
  <div class="breadcrumb-container">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">
        <i class="bi bi-house-door"></i>
        <span>首页</span>
      </el-breadcrumb-item>
      <el-breadcrumb-item 
        v-for="(item, index) in breadcrumbList" 
        :key="index"
        :to="item.path ? { path: item.path } : undefined"
      >
        <i v-if="item.icon" :class="item.icon"></i>
        <span>{{ item.title }}</span>
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

const breadcrumbList = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title);
  const breadcrumbs = [];

  matched.forEach((item, index) => {
    // 跳过根路径
    if (item.path === '/') return;

    breadcrumbs.push({
      title: item.meta.title,
      icon: item.meta.icon,
      path: index === matched.length - 1 ? null : item.path, // 最后一项不可点击
    });
  });

  return breadcrumbs;
});
</script>

<style scoped>
.breadcrumb-container {
  display: flex;
  align-items: center;
}

:deep(.el-breadcrumb) {
  font-size: 14px;
}

:deep(.el-breadcrumb__item) {
  display: flex;
  align-items: center;
}

:deep(.el-breadcrumb__inner) {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
  font-weight: 500;
}

:deep(.el-breadcrumb__inner:hover) {
  color: var(--primary-color);
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--text-primary);
  font-weight: 600;
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner:hover) {
  color: var(--text-primary);
  cursor: default;
}

.breadcrumb-container i {
  font-size: 14px;
}
</style>
