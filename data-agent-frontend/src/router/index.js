/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { createRouter, createWebHistory } from 'vue-router';
import { ElMessage } from 'element-plus';
import routes from '@/router/routes';
import modelConfigService from '@/services/modelConfig';
import { useUserStore } from '@/stores/user';

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 路由切换时滚动到顶部
    if (savedPosition) {
      return savedPosition;
    } else {
      return { top: 0 };
    }
  },
});

let hasShownWarning = false;

// 全局路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} - Spring AI Alibaba Data Agent`;
  } else {
    document.title = 'Spring AI Alibaba Data Agent';
  }

  // 认证检查
  const userStore = useUserStore();
  const token = localStorage.getItem('auth_token');

  if (to.meta.public) {
    // 如果是公开页面（登录/注册），且已登录，则重定向到首页
    if (token) {
      next('/dashboard');
      return;
    }
    next();
    return;
  }

  if (!token) {
    next('/login');
    return;
  }

  // 对于所有需要认证的页面，直接允许访问
  // 不在路由守卫中强制检查模型配置，让用户自由导航
  console.log(`导航到: ${to.path} (${to.name})`);
  next();
});

router.afterEach((to, from) => {
  // 路由切换后的处理
  console.log(`导航完成: ${to.path} ${from.path}`);
});

export default router;
