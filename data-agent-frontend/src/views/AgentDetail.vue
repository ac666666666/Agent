<!--
 * Copyright 2025 the original author or authors.
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
-->

<template>
  <BaseLayout>
    <div class="agent-detail-page">
      <el-container class="main-container">
        <!-- Header -->
        <el-header class="glass-header">
          <div class="header-content">
            <div class="header-left">
              <el-button class="back-btn" :icon="ArrowLeft" @click="goBack" circle plain />
              <div
                class="avatar-wrapper"
                @mouseenter="showHeaderAvatarButton = true"
                @mouseleave="showHeaderAvatarButton = false"
              >
                <el-avatar :src="agent.avatar" :size="48" class="header-avatar" shape="square">
                  {{ agent.name?.substring(0, 1) }}
                </el-avatar>
                <div v-if="showHeaderAvatarButton" class="avatar-overlay-header">
                  <el-button
                    type="primary"
                    size="small"
                    @click="triggerHeaderFileUpload"
                    :loading="headerUploading"
                    link
                    class="upload-btn"
                  >
                    <el-icon><Upload /></el-icon>
                  </el-button>
                </div>
              </div>
              <div class="title-section">
                <h2 class="agent-title">{{ agent.name }}</h2>
                <el-tag
                  size="small"
                  :type="agent.status === 'published' ? 'success' : 'warning'"
                  effect="light"
                  round
                >
                  {{ agent.status === 'published' ? '已发布' : '草稿' }}
                </el-tag>
              </div>
              <input
                ref="headerFileInput"
                type="file"
                accept="image/*"
                style="display: none"
                @change="handleHeaderFileUpload"
              />
            </div>
            <div class="header-right">
              <!-- Placeholder for future actions -->
            </div>
          </div>
        </el-header>

        <el-container class="content-container">
          <!-- Sidebar Menu -->
          <el-aside width="260px" class="glass-sidebar">
            <el-menu
              :default-active="activeMenuIndex"
              class="custom-menu"
              @select="handleMenuSelect"
            >
              <div class="menu-group-title">配置</div>
              <el-menu-item index="basic">
                <el-icon><InfoFilled /></el-icon>
                <span>基本信息</span>
              </el-menu-item>
              <el-menu-item index="datasource">
                <el-icon><Coin /></el-icon>
                <span>数据源配置</span>
              </el-menu-item>
              <el-menu-item index="prompt">
                <el-icon><ChatLineSquare /></el-icon>
                <span>PROMPT配置</span>
              </el-menu-item>

              <div class="menu-group-title">知识库</div>
              <el-menu-item index="agent-knowledge">
                <el-icon><Document /></el-icon>
                <span>智能体知识</span>
              </el-menu-item>
              <el-menu-item index="business-knowledge">
                <el-icon><User /></el-icon>
                <span>业务知识</span>
              </el-menu-item>
              <el-menu-item index="semantic-model">
                <el-icon><Suitcase /></el-icon>
                <span>语义模型</span>
              </el-menu-item>
              <el-menu-item index="preset-questions">
                <el-icon><Setting /></el-icon>
                <span>预设问题</span>
              </el-menu-item>

              <div class="menu-group-title">操作</div>
              <el-menu-item index="go-run" class="action-menu-item run-item">
                <el-icon><VideoPlay /></el-icon>
                <span>前往运行</span>
              </el-menu-item>
              <el-menu-item index="access-api" class="action-menu-item api-item">
                <el-icon><Connection /></el-icon>
                <span>访问 API</span>
              </el-menu-item>
            </el-menu>
          </el-aside>

          <!-- Main Content -->
          <el-main class="glass-main">
            <div class="main-content-wrapper">
              <AgentBaseSetting
                v-if="activeMenuIndex === 'basic'"
                :agent="agent"
              ></AgentBaseSetting>
              <AgentDataSourceConfig
                v-else-if="activeMenuIndex === 'datasource'"
                :agent-id="agent.id"
              ></AgentDataSourceConfig>
              <AgentPromptConfig
                v-else-if="activeMenuIndex === 'prompt'"
                :agent-id="agent.id"
                :agent-prompt="agent.prompt"
              ></AgentPromptConfig>
              <BusinessKnowledgeConfig
                v-else-if="activeMenuIndex === 'business-knowledge'"
                :agent-id="agent.id"
              ></BusinessKnowledgeConfig>
              <AgentSemanticsConfig
                v-else-if="activeMenuIndex === 'semantic-model'"
                :agent-id="agent.id"
              ></AgentSemanticsConfig>
              <AgentPresetsConfig
                v-else-if="activeMenuIndex === 'preset-questions'"
                :agent-id="agent.id"
              ></AgentPresetsConfig>
              <AgentAccessApi
                v-else-if="activeMenuIndex === 'access-api'"
                :agent-id="agent.id"
              ></AgentAccessApi>
              <AgentKnowledgeConfig
                v-else-if="activeMenuIndex === 'agent-knowledge'"
                :agent-id="agent.id"
              ></AgentKnowledgeConfig>
              <NotFound v-else></NotFound>
            </div>
          </el-main>
        </el-container>
      </el-container>
    </div>
  </BaseLayout>
</template>

<script lang="ts">
import { ref, defineComponent, Ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import AgentService from '@/services/agent';
import {
  ArrowLeft,
  InfoFilled,
  Coin,
  ChatLineSquare,
  User,
  Suitcase,
  Setting,
  VideoPlay,
  Connection,
  Document,
  Upload,
} from '@element-plus/icons-vue';
import BaseLayout from '@/layouts/BaseLayout.vue';
import AgentBaseSetting from '@/components/agent/BaseSetting.vue';
import AgentPromptConfig from '@/components/agent/PromptConfig.vue';
import BusinessKnowledgeConfig from '@/components/agent/BusinessKnowledgeConfig.vue';
import AgentSemanticsConfig from '@/components/agent/SemanticsConfig.vue';
import AgentPresetsConfig from '@/components/agent/PresetsConfig.vue';
import AgentAccessApi from '@/components/agent/AccessApi.vue';
import AgentDataSourceConfig from '@/components/agent/DataSourceConfig.vue';
import AgentKnowledgeConfig from '@/components/agent/AgentKnowledgeConfig.vue';
import NotFound from '@/views/NotFound.vue';
import { Agent } from '@/services/agent';
import { fileUploadApi } from '@/services/fileUpload';

export default defineComponent({
  name: 'AgentDetail',
  components: {
    BaseLayout,
    AgentBaseSetting,
    AgentPromptConfig,
    BusinessKnowledgeConfig,
    AgentSemanticsConfig,
    AgentPresetsConfig,
    AgentAccessApi,
    AgentDataSourceConfig,
    AgentKnowledgeConfig,
    NotFound,
    InfoFilled,
    Coin,
    ChatLineSquare,
    User,
    Suitcase,
    Setting,
    VideoPlay,
    Connection,
    Document,
    Upload,
  },
  setup() {
    const router = useRouter();

    // 响应式数据
    const activeMenuIndex: Ref<string> = ref('basic');
    const agent: Ref<Agent> = ref({
      id: '',
      name: 'Loading...',
      description: '',
      status: 'draft',
      createdAt: '',
      updatedAt: '',
      avatar: '',
      prompt: '',
      category: '',
      adminId: '',
      tags: '',
      humanReviewEnabled: false,
    } as Agent);

    const headerFileInput = ref<HTMLInputElement | null>(null);
    const headerUploading = ref(false);
    const showHeaderAvatarButton = ref(false);
    const originalHeaderAvatar = ref<string>('');

    const triggerHeaderFileUpload = () => {
      if (headerFileInput.value) {
        headerFileInput.value.click();
      }
    };

    const handleHeaderFileUpload = async (event: Event) => {
      const target = event.target as HTMLInputElement;
      const file = target.files?.[0];
      if (!file) return;

      // 验证文件类型
      if (!file.type.startsWith('image/')) {
        ElMessage.error('请选择图片文件');
        return;
      }

      if (file.size > 5 * 1024 * 1024) {
        ElMessage.error('图片大小不能超过5MB');
        return;
      }

      try {
        headerUploading.value = true;

        originalHeaderAvatar.value = agent.value.avatar;

        const reader = new FileReader();
        reader.onload = e => {
          agent.value.avatar = e.target?.result as string;
        };
        reader.readAsDataURL(file);

        const response = await fileUploadApi.uploadAvatar(file);

        if (response.success) {
          agent.value.avatar = response.url;
          ElMessage.success('头像上传成功');
        } else {
          throw new Error(response.message || '上传失败');
        }
      } catch (error) {
        ElMessage.error('头像上传失败: ' + (error instanceof Error ? error.message : '未知错误'));
        agent.value.avatar = originalHeaderAvatar.value;
      } finally {
        headerUploading.value = false;
        if (headerFileInput.value) {
          headerFileInput.value.value = '';
        }
      }
    };

    const handleMenuSelect = (index: string) => {
      const id = router.currentRoute.value.params.id;
      activeMenuIndex.value = index;
      if (index === 'go-run') {
        router.push(`/agent/${id}/run`);
      }
    };

    const goBack = () => {
      router.push('/agents');
    };

    const loadAgent = async () => {
      try {
        const id = router.currentRoute.value.params.id;
        const loadAgent = await AgentService.get(id);
        if (loadAgent) {
          agent.value = loadAgent;
        } else {
          throw new Error('Agent 不存在');
        }
      } catch (error) {
        ElMessage.error('加载失败');
        console.error('加载失败:', error);
      }
    };

    onMounted(async () => {
      await loadAgent();
    });

    return {
      ArrowLeft,
      agent,
      activeMenuIndex,
      handleMenuSelect,
      goBack,
      headerFileInput,
      headerUploading,
      showHeaderAvatarButton,
      triggerHeaderFileUpload,
      handleHeaderFileUpload,
    };
  },
});
</script>

<style scoped>
.agent-detail-page {
  min-height: 100vh;
  background-color: #f8fafc;
  background-image: radial-gradient(at 0% 0%, hsla(253, 16%, 7%, 0) 0, transparent 50%),
    radial-gradient(at 50% 0%, hsla(225, 39%, 30%, 0.05) 0, transparent 50%),
    radial-gradient(at 100% 0%, hsla(339, 49%, 30%, 0) 0, transparent 50%);
  position: relative;
  display: flex;
  flex-direction: column;
}

/* 装饰性背景 */
.agent-detail-page::before {
  content: '';
  position: absolute;
  top: -10%;
  right: -5%;
  width: 50%;
  height: 50%;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.05) 0%, rgba(255, 255, 255, 0) 70%);
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;
}

.main-container {
  max-width: 1600px;
  width: 100%;
  margin: 0 auto;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  z-index: 1;
  height: calc(100vh - 64px); /* Adjust based on BaseLayout header */
}

/* Glassmorphism Header */
.glass-header {
  height: auto;
  padding: 1.25rem 2rem;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02), 0 2px 4px -1px rgba(0, 0, 0, 0.02);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.back-btn {
  border: none;
  background: rgba(255, 255, 255, 0.5);
  transition: all 0.2s;
  width: 40px;
  height: 40px;
}

.back-btn:hover {
  background: white;
  transform: translateX(-2px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  color: #2563eb;
}

.avatar-wrapper {
  position: relative;
  width: 48px;
  height: 48px;
  cursor: pointer;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.header-avatar {
  width: 100% !important;
  height: 100% !important;
  font-weight: 600;
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  font-size: 1.2rem;
}

.avatar-overlay-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(2px);
  animation: fadeIn 0.2s ease;
}

.upload-btn {
  color: white !important;
  font-size: 1.2rem;
}

.title-section {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.agent-title {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.content-container {
  display: flex;
  gap: 1.5rem;
  flex: 1;
  overflow: hidden; /* Prevent double scrollbars */
}

/* Sidebar */
.glass-sidebar {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  padding: 1rem 0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.custom-menu {
  border-right: none;
  background: transparent;
}

.menu-group-title {
  padding: 1rem 1.5rem 0.5rem;
  font-size: 0.75rem;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

:deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 4px 12px;
  border-radius: 10px;
  color: #64748b;
  font-weight: 500;
}

:deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.8);
  color: #3b82f6;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  color: #2563eb;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(37, 99, 235, 0.05);
}

:deep(.el-icon) {
  font-size: 1.1rem;
}

.action-menu-item {
  margin-top: 4px;
}

.run-item :deep(.el-icon) {
  color: #10b981;
}

.api-item :deep(.el-icon) {
  color: #8b5cf6;
}

/* Main Content */
.glass-main {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  padding: 2rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02);
  overflow-y: auto;
  position: relative;
}

.main-content-wrapper {
  max-width: 900px;
  margin: 0 auto;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* Scrollbar styling */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.3);
  border-radius: 3px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
</style>
