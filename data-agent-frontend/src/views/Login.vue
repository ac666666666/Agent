<template>
  <div class="auth-container">
    <!-- 背景装饰 -->
    <div class="auth-background">
      <div class="robot-circuit"></div>
      <div class="floating-icons">
        <i class="bi bi-robot icon-float icon-1"></i>
        <i class="bi bi-cpu icon-float icon-2"></i>
        <i class="bi bi-diagram-3 icon-float icon-3"></i>
        <i class="bi bi-lightning-charge icon-float icon-4"></i>
        <i class="bi bi-gear icon-float icon-5"></i>
        <i class="bi bi-database icon-float icon-6"></i>
      </div>
    </div>

    <!-- 认证卡片 -->
    <div class="auth-box">
      <div class="auth-header">
        <div class="logo-section">
          <i class="bi bi-robot logo-icon"></i>
        </div>
        <h1 class="auth-title">Data Agent</h1>
        <p class="auth-subtitle">企业级数据智能体平台</p>
      </div>
      
      <el-tabs v-model="activeTab" class="auth-tabs">
        <!-- 登录表单 -->
        <el-tab-pane label="登录" name="login">
          <el-form 
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            label-position="top"
            class="auth-form"
            @keyup.enter="handleLogin"
          >
            <el-form-item label="用户名" prop="username">
              <el-input 
                v-model="loginForm.username" 
                placeholder="请输入用户名"
                size="large"
              >
                <template #prefix>
                  <i class="bi bi-person"></i>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="密码" prop="password">
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="请输入密码"
                size="large"
                show-password
              >
                <template #prefix>
                  <i class="bi bi-lock"></i>
                </template>
              </el-input>
            </el-form-item>
            
            <el-button 
              type="primary" 
              class="auth-button" 
              size="large"
              :loading="loading" 
              @click="handleLogin"
            >
              <i class="bi bi-box-arrow-in-right"></i>
              <span>登录</span>
            </el-button>
          </el-form>
        </el-tab-pane>

        <!-- 注册表单 -->
        <el-tab-pane label="注册" name="register">
          <el-form 
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            label-position="top"
            class="auth-form"
            @keyup.enter="handleRegister"
          >
            <el-form-item label="用户名" prop="username">
              <el-input 
                v-model="registerForm.username" 
                placeholder="请输入用户名"
                size="large"
              >
                <template #prefix>
                  <i class="bi bi-person"></i>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="昵称" prop="nickname">
              <el-input 
                v-model="registerForm.nickname" 
                placeholder="请输入昵称"
                size="large"
              >
                <template #prefix>
                  <i class="bi bi-person-badge"></i>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="密码" prop="password">
              <el-input 
                v-model="registerForm.password" 
                type="password" 
                placeholder="请输入密码（6-20位）"
                size="large"
                show-password
              >
                <template #prefix>
                  <i class="bi bi-lock"></i>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input 
                v-model="registerForm.confirmPassword" 
                type="password" 
                placeholder="请再次输入密码"
                size="large"
                show-password
              >
                <template #prefix>
                  <i class="bi bi-shield-check"></i>
                </template>
              </el-input>
            </el-form-item>
            
            <el-button 
              type="primary" 
              class="auth-button" 
              size="large"
              :loading="registerLoading" 
              @click="handleRegister"
            >
              <i class="bi bi-person-plus"></i>
              <span>注册</span>
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const loginFormRef = ref(null);
const registerFormRef = ref(null);
const loading = ref(false);
const registerLoading = ref(false);
const activeTab = ref('login');

const loginForm = reactive({
  username: '',
  password: ''
});

const registerForm = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
});

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'));
  } else {
    callback();
  }
};

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
};

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 20, message: '长度不能超过 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ]
};

const handleLogin = async () => {
  if (!loginFormRef.value) return;
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const success = await userStore.login(loginForm);
        if (success) {
          ElMessage.success('登录成功');
          router.push('/');
        } else {
          ElMessage.error('登录失败，请检查用户名或密码');
        }
      } catch (error) {
        ElMessage.error('登录发生错误，请稍后重试');
      } finally {
        loading.value = false;
      }
    }
  });
};

const handleRegister = async () => {
  if (!registerFormRef.value) return;
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      registerLoading.value = true;
      try {
        const { confirmPassword, ...registerData } = registerForm;
        const success = await userStore.register(registerData);
        if (success) {
          ElMessage.success('注册成功，已自动登录');
          router.push('/');
        } else {
          ElMessage.error('注册失败，用户名可能已存在');
        }
      } catch (error) {
        ElMessage.error('注册发生错误，请稍后重试');
      } finally {
        registerLoading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: url('@/assets/images/auth-bg.jpg') no-repeat center center;
  background-size: cover;
  position: relative;
  overflow: hidden;
}

.auth-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 0;
}

html.dark .auth-container::before {
  background: rgba(0, 0, 0, 0.5);
}

/* 机器人电路背景 */
.auth-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  opacity: 0.1;
  pointer-events: none;
}

.robot-circuit {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(90deg, rgba(255,255,255,0.1) 1px, transparent 1px),
    linear-gradient(rgba(255,255,255,0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: circuit-move 20s linear infinite;
}

@keyframes circuit-move {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 50px 50px;
  }
}

/* 浮动图标 */
.floating-icons {
  position: absolute;
  width: 100%;
  height: 100%;
}

.icon-float {
  position: absolute;
  font-size: 48px;
  color: rgba(255, 255, 255, 0.15);
  animation: float 6s ease-in-out infinite;
}

.icon-1 {
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.icon-2 {
  top: 20%;
  right: 15%;
  animation-delay: 1s;
}

.icon-3 {
  bottom: 20%;
  left: 15%;
  animation-delay: 2s;
}

.icon-4 {
  bottom: 15%;
  right: 20%;
  animation-delay: 3s;
}

.icon-5 {
  top: 50%;
  left: 5%;
  animation-delay: 4s;
}

.icon-6 {
  top: 60%;
  right: 10%;
  animation-delay: 5s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(10deg);
  }
}

/* 认证卡片 */
.auth-box {
  width: 100%;
  max-width: 450px;
  padding: 48px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
  z-index: 1;
}

html.dark .auth-box {
  background: rgba(30, 41, 59, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-section {
  margin-bottom: 16px;
}

.logo-icon {
  font-size: 64px;
  color: #ffffff;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.3));
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.auth-title {
  font-size: 32px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 8px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.auth-subtitle {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  margin: 0;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.auth-tabs {
  margin-top: 24px;
}

.auth-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
}

.auth-tabs :deep(.el-tabs__nav-wrap) {
  display: flex;
  justify-content: center;
}

.auth-tabs :deep(.el-tabs__nav) {
  float: none;
}

.auth-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: rgba(255, 255, 255, 0.3);
}

.auth-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.auth-tabs :deep(.el-tabs__item.is-active) {
  color: #ffffff;
}

.auth-tabs :deep(.el-tabs__active-bar) {
  background-color: #ffffff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.auth-form {
  margin-top: 24px;
}

.auth-form :deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.95);
  font-weight: 500;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.auth-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.3) inset;
}

html.dark .auth-form :deep(.el-input__wrapper) {
  background: rgba(30, 41, 59, 0.8);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.2) inset;
}

.auth-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--primary-color) inset;
}

.auth-form :deep(.bi) {
  font-size: 16px;
  color: var(--text-secondary);
}

.auth-button {
  width: 100%;
  margin-top: 16px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.auth-button i {
  font-size: 18px;
}
</style>
