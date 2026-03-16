/*
 * Copyright 2024-2025 the original author or authors.
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
 */

import axios from 'axios';
import { useUserStore } from '@/stores/user';

// 全局配置axios拦截器
export function setupAxiosInterceptors() {
  // 请求拦截器 - 添加认证token
  axios.interceptors.request.use(
    (config) => {
      const token = localStorage.getItem('auth_token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );

  // 响应拦截器 - 处理401错误
  axios.interceptors.response.use(
    (response) => {
      return response;
    },
    (error) => {
      if (error.response?.status === 401) {
        console.log('检测到401错误，清理认证状态');
        // Token过期或无效，清理认证状态
        localStorage.removeItem('auth_token');
        localStorage.removeItem('user_id');
        
        // 清理用户store状态
        const userStore = useUserStore();
        userStore.logout();
        
        // 重定向到登录页（避免无限循环）
        if (window.location.pathname !== '/login') {
          console.log('重定向到登录页');
          window.location.href = '/login';
        }
      }
      return Promise.reject(error);
    }
  );
}