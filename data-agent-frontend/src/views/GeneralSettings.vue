<template>
  <BaseLayout>
    <div class="general-settings-page">
      <div class="page-header">
        <div class="header-content">
          <div>
            <h1 class="page-title">通用设置</h1>
            <p class="page-subtitle">配置系统基础参数、界面显示和用户偏好</p>
          </div>
        </div>
      </div>

      <div class="settings-container">
        <el-tabs v-model="activeTab" class="settings-tabs">
          <!-- 系统设置 -->
          <el-tab-pane label="系统设置" name="system">
            <el-card shadow="never" class="setting-card">
              <template #header>
                <div class="card-header">
                  <span>基本信息</span>
                </div>
              </template>
              <el-form :model="systemForm" label-width="120px" class="setting-form">
                <el-form-item label="系统名称">
                  <el-input v-model="systemForm.systemName" placeholder="Data Agent" />
                </el-form-item>
                <el-form-item label="系统描述">
                  <el-input
                    v-model="systemForm.description"
                    type="textarea"
                    :rows="3"
                    placeholder="企业级数据智能体平台"
                  />
                </el-form-item>
                <el-form-item label="默认语言">
                  <el-select v-model="systemForm.language" placeholder="请选择语言">
                    <el-option label="简体中文" value="zh-CN" />
                    <el-option label="English" value="en-US" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveSystemSettings">保存更改</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>

          <!-- 界面偏好 -->
          <el-tab-pane label="界面偏好" name="appearance">
            <el-card shadow="never" class="setting-card">
              <template #header>
                <div class="card-header">
                  <span>主题设置</span>
                </div>
              </template>
              <div class="theme-selector">
                <div
                  class="theme-item"
                  :class="{ active: appearanceForm.theme === 'light' }"
                  @click="appearanceForm.theme = 'light'"
                >
                  <div class="theme-preview light"></div>
                  <div class="theme-name">浅色模式</div>
                </div>
                <div
                  class="theme-item"
                  :class="{ active: appearanceForm.theme === 'dark' }"
                  @click="appearanceForm.theme = 'dark'"
                >
                  <div class="theme-preview dark"></div>
                  <div class="theme-name">深色模式</div>
                </div>
                <div
                  class="theme-item"
                  :class="{ active: appearanceForm.theme === 'auto' }"
                  @click="appearanceForm.theme = 'auto'"
                >
                  <div class="theme-preview auto"></div>
                  <div class="theme-name">跟随系统</div>
                </div>
              </div>
              <div class="mt-6 text-center">
                <el-button type="primary" @click="saveAppearanceSettings">应用设置</el-button>
              </div>
            </el-card>
          </el-tab-pane>

          <!-- 关于 -->
          <el-tab-pane label="关于" name="about">
            <el-card shadow="never" class="setting-card about-card">
              <div class="about-content">
                <div class="app-logo">
                  <i class="bi bi-robot"></i>
                </div>
                <h2 class="app-name">Data Agent</h2>
                <p class="app-version">Version 1.0.0 (Beta)</p>
                <p class="app-desc">
                  Data Agent
                  是一个基于大语言模型的智能数据分析平台，致力于让数据交互更简单、更智能。
                </p>
                <div class="app-links">
                  <el-link type="primary" href="#" target="_blank">使用文档</el-link>
                  <el-divider direction="vertical" />
                  <el-link type="primary" href="#" target="_blank">GitHub</el-link>
                  <el-divider direction="vertical" />
                  <el-link type="primary" href="#" target="_blank">反馈问题</el-link>
                </div>
              </div>
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </BaseLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import BaseLayout from '@/layouts/BaseLayout.vue';
import SystemConfigService from '@/services/systemConfig';
import { useSystemStore } from '@/stores/system';

const activeTab = ref('system');
const systemStore = useSystemStore();

// 系统设置表单
const systemForm = reactive({
  systemName: '',
  description: '',
  language: '',
});

// 界面偏好表单
const appearanceForm = reactive({
  theme: '',
});

// 加载设置
const loadSettings = async () => {
  // Sync from store first
  systemForm.systemName = systemStore.systemName;
  systemForm.description = systemStore.description;
  systemForm.language = systemStore.language;
  appearanceForm.theme = systemStore.theme;
};

// 保存系统设置
const saveSystemSettings = async () => {
  const configs = {
    'system.name': systemForm.systemName,
    'system.description': systemForm.description,
    'system.language': systemForm.language,
  };

  const success = await SystemConfigService.updateConfigs(configs);
  if (success) {
    ElMessage.success('系统设置已保存');
    systemStore.updateSystemSettings(
      systemForm.systemName,
      systemForm.description,
      systemForm.language,
    );
  } else {
    ElMessage.error('保存失败');
  }
};

// 保存界面设置
const saveAppearanceSettings = async () => {
  const configs = {
    'appearance.theme': appearanceForm.theme,
  };

  const success = await SystemConfigService.updateConfigs(configs);
  if (success) {
    ElMessage.success('界面设置已应用');
    systemStore.updateTheme(appearanceForm.theme);
  } else {
    ElMessage.error('保存失败');
  }
};

onMounted(() => {
  loadSettings();
});
</script>

<style scoped>
.general-settings-page {
  padding: 24px;
  background: var(--bg-color);
  min-height: calc(100vh - 64px);
  border-radius: 8px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.page-subtitle {
  color: var(--text-secondary);
  margin-top: 8px;
  font-size: 14px;
}

.settings-container {
  background: var(--card-bg-color);
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.05);
  min-height: 500px;
}

.settings-tabs :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 20px;
  border-bottom: 1px solid var(--border-color);
}

.settings-tabs :deep(.el-tabs__content) {
  padding: 24px;
}

.setting-card {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  font-weight: 600;
  color: var(--text-primary);
}

.setting-form {
  max-width: 600px;
}

/* 主题选择器样式 */
.theme-selector {
  display: flex;
  justify-content: center;
  gap: 32px;
  padding: 20px 0;
}

.theme-item {
  cursor: pointer;
  text-align: center;
}

.theme-preview {
  width: 120px;
  height: 80px;
  border-radius: 8px;
  border: 2px solid var(--border-color);
  margin-bottom: 12px;
  transition: all 0.3s ease;
  position: relative;
}

.theme-item.active .theme-preview {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.theme-item.active .theme-name {
  color: #3b82f6;
  font-weight: 600;
}

.theme-preview.light {
  background: #ffffff;
}

.theme-preview.dark {
  background: #1e293b;
}

.theme-preview.auto {
  background: linear-gradient(90deg, #ffffff 50%, #1e293b 50%);
}

/* 关于页面样式 */
.about-card {
  border: none;
  box-shadow: none !important;
}

.about-content {
  text-align: center;
  padding: 40px 0;
}

.app-logo {
  font-size: 64px;
  color: #3b82f6;
  margin-bottom: 16px;
}

.app-name {
  font-size: 28px;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.app-version {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 24px;
}

.app-desc {
  color: var(--text-secondary);
  max-width: 500px;
  margin: 0 auto 32px auto;
  line-height: 1.6;
}

.app-links .el-link {
  font-size: 14px;
}
</style>
