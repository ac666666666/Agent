<template>
  <div class="auth-container">
    <div class="auth-box">
      <div class="auth-header">
        <h1 class="auth-title">Data Agent</h1>
        <p class="auth-subtitle">创建您的账号</p>
      </div>
      
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
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item label="昵称" prop="nickname">
          <el-input 
            v-model="registerForm.nickname" 
            placeholder="请输入昵称"
            prefix-icon="Postcard"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="registerForm.password" 
            type="password" 
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-button 
          type="primary" 
          class="auth-button" 
          :loading="loading" 
          @click="handleRegister"
        >
          注册
        </el-button>
        
        <div class="auth-footer">
          <span>已有账号？</span>
          <router-link to="/login" class="auth-link">立即登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Lock, Postcard } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();
const registerFormRef = ref(null);
const loading = ref(false);

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

const handleRegister = async () => {
  if (!registerFormRef.value) return;
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
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
        loading.value = false;
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
  background-color: var(--bg-color);
  background-image: radial-gradient(at 0% 0%, hsla(253, 16%, 7%, 0) 0, transparent 50%),
    radial-gradient(at 50% 0%, hsla(225, 39%, 30%, 0.05) 0, transparent 50%),
    radial-gradient(at 100% 0%, hsla(339, 49%, 30%, 0) 0, transparent 50%);
}

html.dark .auth-container {
  background-image: none;
}

.auth-box {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  background: var(--card-bg-color);
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  border: 1px solid var(--border-color);
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.auth-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.auth-subtitle {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
}

.auth-form {
  margin-bottom: 24px;
}

.auth-button {
  width: 100%;
  margin-top: 8px;
  height: 40px;
  font-size: 16px;
}

.auth-footer {
  text-align: center;
  font-size: 14px;
  color: var(--text-secondary);
}

.auth-link {
  color: #3b82f6;
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
}

.auth-link:hover {
  text-decoration: underline;
}
</style>
