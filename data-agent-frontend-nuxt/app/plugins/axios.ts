import axios from 'axios';

export default defineNuxtPlugin((nuxtApp) => {
  // 配置全局 Axios 默认值
  // 注意：服务端渲染时，BaseURL 可能需要是完整 URL（http://localhost:8065/api），
  // 但我们在 nuxt.config.ts 中配置了 proxy，客户端请求 '/api' 会被代理。
  // 为了简化，这里主要配置客户端行为。
  
  axios.defaults.baseURL = '/api'; 
  
  // 可以在这里添加请求/响应拦截器
  axios.interceptors.response.use(
    (response) => response,
    (error) => {
      // 统一错误处理
      console.error('API Request Error:', error);
      return Promise.reject(error);
    }
  );

  return {
    provide: {
      axios: axios
    }
  }
});
