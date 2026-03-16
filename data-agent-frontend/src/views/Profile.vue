<template>
  <BaseLayout>
    <div class="profile-container">
      <div class="page-header">
        <h1 class="page-title">个人中心</h1>
        <p class="page-subtitle">管理您的个人信息和账户设置</p>
      </div>

      <el-row :gutter="20">
        <!-- 左侧：个人信息卡片 -->
        <el-col :span="8">
          <el-card shadow="never" class="profile-card">
            <div class="profile-avatar-section">
              <el-avatar :size="100" :src="userStore.userAvatar" v-if="userStore.userAvatar" />
              <el-avatar :size="100" icon="UserFilled" v-else />
              <el-button type="primary" size="small" class="mt-4">更换头像</el-button>
            </div>
            <div class="profile-info">
              <h3>{{ userStore.username }}</h3>
              <p class="user-role">{{ userStore.userRole }}</p>
              <p class="user-email">{{ userStore.email }}</p>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：设置表单 -->
        <el-col :span="16">
          <el-card shadow="never" class="settings-card">
            <template #header>
              <div class="card-header">
                <span>账户设置</span>
              </div>
            </template>

            <el-tabs v-model="activeTab">
              <!-- 基本信息 -->
              <el-tab-pane label="基本信息" name="basic">
                <el-form :model="profileForm" label-width="100px" class="profile-form">
                  <el-form-item label="用户名">
                    <el-input v-model="profileForm.username" placeholder="请输入用户名" />
                  </el-form-item>
                  <el-form-item label="邮箱">
                    <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
                  </el-form-item>
                  <el-form-item label="手机号">
                    <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="updateProfile">保存更改</el-button>
                  </el-form-item>
                </el-form>
              </el-tab-pane>

              <!-- 修改密码 -->
              <el-tab-pane label="修改密码" name="password">
                <el-form :model="passwordForm" label-width="100px" class="profile-form">
                  <el-form-item label="当前密码">
                    <el-input
                      v-model="passwordForm.oldPassword"
                      type="password"
                      placeholder="请输入当前密码"
                      show-password
                    />
                  </el-form-item>
                  <el-form-item label="新密码">
                    <el-input
                      v-model="passwordForm.newPassword"
                      type="password"
                      placeholder="请输入新密码"
                      show-password
                    />
                  </el-form-item>
                  <el-form-item label="确认密码">
                    <el-input
                      v-model="passwordForm.confirmPassword"
                      type="password"
                      placeholder="请再次输入新密码"
                      show-password
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="changePassword">修改密码</el-button>
                  </el-form-item>
                </el-form>
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </BaseLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import BaseLayout from '@/layouts/BaseLayout.vue';
import { useUserStore } from '@/stores/user';
import AuthService from '@/services/auth';

const userStore = useUserStore();
const activeTab = ref('basic');

const profileForm = reactive({
  username: '',
  email: '',
  phone: '',
});

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const loadProfile = () => {
  profileForm.username = userStore.username;
  profileForm.email = userStore.email || '';
  profileForm.phone = userStore.phone || '';
};

const updateProfile = async () => {
  try {
    await AuthService.updateProfile({
      username: profileForm.username,
      email: profileForm.email,
      phone: profileForm.phone,
    });
    ElMessage.success('个人信息更新成功');
    userStore.updateUserInfo(profileForm);
  } catch (error) {
    ElMessage.error('更新失败，请重试');
  }
};

const changePassword = async () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致');
    return;
  }

  try {
    await AuthService.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    });
    ElMessage.success('密码修改成功');
    passwordForm.oldPassword = '';
    passwordForm.newPassword = '';
    passwordForm.confirmPassword = '';
  } catch (error) {
    ElMessage.error('密码修改失败，请检查当前密码是否正确');
  }
};

onMounted(() => {
  loadProfile();
});
</script>

<style scoped>
.profile-container {
  padding: 24px;
  background: var(--bg-color);
  min-height: calc(100vh - 64px);
  border-radius: 8px;
}

.page-header {
  margin-bottom: 32px;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 16px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  background: linear-gradient(90deg, var(--primary-color), var(--accent-color));
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
}

.page-subtitle {
  color: var(--text-secondary);
  margin-top: 8px;
  font-size: 15px;
}

.profile-card,
.settings-card {
  border-radius: 16px;
  border: none;
  box-shadow: var(--shadow-sm);
}

.profile-avatar-section {
  text-align: center;
  padding: 20px 0;
  border-bottom: 1px solid var(--border-color);
}

.profile-info {
  padding: 20px 0;
  text-align: center;
}

.profile-info h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.user-role {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 4px 0;
}

.user-email {
  color: var(--text-tertiary);
  font-size: 13px;
  margin: 4px 0;
}

.mt-4 {
  margin-top: 16px;
}

.profile-form {
  max-width: 500px;
  padding: 20px 0;
}
</style>
