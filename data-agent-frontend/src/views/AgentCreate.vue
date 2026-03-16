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
    <div class="agent-create-page">
      <div class="create-form-wrapper">
        <!-- 页面标题 -->
        <div class="page-header">
          <h2>创建智能体</h2>
          <p>配置您的专属数据分析智能体，让AI帮助您更好地理解和分析数据</p>
        </div>

        <!-- 表单区域 -->
        <div class="form-section">
          <div class="section-card">
            <div class="form-layout-container">
              <!-- 左侧列：基础信息 -->
              <div class="form-column-left">
                <!-- 头像上传组件 -->
                <div class="form-group avatar-group">
                  <label class="form-label">头像设置</label>
                  <div class="avatar-upload">
                    <div class="avatar-preview">
                      <img
                        :src="agentForm.avatar"
                        alt="智能体头像"
                        @load="handleImageLoad"
                        @error="handleImageError"
                      />
                    </div>
                    <div class="avatar-controls">
                      <div class="avatar-buttons">
                        <el-button @click="regenerateAvatar" size="small" :icon="Refresh" circle />
                        <el-button
                          @click="triggerFileUpload"
                          :disabled="uploading"
                          size="default"
                          :icon="uploading ? Loading : Upload"
                        >
                          {{ uploading ? '上传中...' : '上传图片' }}
                        </el-button>
                        <input
                          ref="fileInput"
                          type="file"
                          accept="image/*"
                          style="display: none"
                          @change="handleFileUpload"
                        />
                      </div>
                      <p class="avatar-tip">200x200px, JPG/PNG</p>
                    </div>
                  </div>
                </div>

                <div class="form-item">
                  <label class="form-label">
                    智能体名称
                    <span class="required">*</span>
                  </label>
                  <el-input
                    v-model="agentForm.name"
                    placeholder="给智能体起个响亮的名字"
                    size="large"
                    class="custom-input"
                  />
                </div>

                <div class="form-item">
                  <label class="form-label">
                    分类
                    <span class="required">*</span>
                  </label>
                  <el-input
                    v-model="agentForm.category"
                    placeholder="例如：数据分析、财务助手"
                    size="large"
                    class="custom-input"
                  />
                </div>

                <div class="form-item">
                  <label class="form-label">
                    标签
                    <span class="required">*</span>
                  </label>
                  <el-input
                    v-model="agentForm.tags"
                    placeholder="输入标签，用逗号分隔"
                    size="large"
                    class="custom-input"
                  >
                    <template #prefix>
                      <el-icon class="el-input__icon"><PriceTag /></el-icon>
                    </template>
                  </el-input>
                </div>

                <div class="form-item">
                  <label class="form-label">发布状态</label>
                  <el-select
                    v-model="agentForm.status"
                    placeholder="选择状态"
                    style="width: 100%"
                    size="large"
                    class="custom-select"
                  >
                    <el-option key="draft" label="草稿 (Draft)" value="draft">
                      <span style="float: left">草稿 (Draft)</span>
                      <span style="float: right; color: #e6a23c; font-size: 13px">
                        <el-icon><EditPen /></el-icon>
                      </span>
                    </el-option>
                    <el-option key="published" label="已发布 (Published)" value="published">
                      <span style="float: left">已发布 (Published)</span>
                      <span style="float: right; color: #67c23a; font-size: 13px">
                        <el-icon><CircleCheck /></el-icon>
                      </span>
                    </el-option>
                    <el-option key="offline" label="已下线 (Offline)" value="offline">
                      <span style="float: left">已下线 (Offline)</span>
                      <span style="float: right; color: #909399; font-size: 13px">
                        <el-icon><Remove /></el-icon>
                      </span>
                    </el-option>
                  </el-select>
                </div>
              </div>

              <!-- 右侧列：详细配置 -->
              <div class="form-column-right">
                <div class="form-item">
                  <label class="form-label">功能描述</label>
                  <el-input
                    v-model="agentForm.description"
                    :rows="4"
                    type="textarea"
                    placeholder="简要描述智能体的主要功能和用途..."
                    size="large"
                    class="custom-textarea"
                    resize="none"
                  />
                </div>

                <div class="form-item full-height-item">
                  <label class="form-label">系统提示词 (System Prompt)</label>
                  <div class="prompt-editor-wrapper">
                    <el-input
                      v-model="agentForm.prompt"
                      :rows="15"
                      type="textarea"
                      placeholder="设定智能体的人设、职责和行为准则..."
                      size="large"
                      class="custom-textarea prompt-textarea"
                    />
                  </div>
                  <p class="field-help">定义智能体的行为模式和专业领域，支持 Markdown 格式。</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部操作按钮 -->
        <div class="bottom-actions-bar">
          <div class="actions-container">
            <el-button @click="goBack" size="large" class="cancel-btn">取消</el-button>
            <el-button
              type="primary"
              :icon="Plus"
              @click="createAgent"
              :loading="loading"
              size="large"
              class="submit-btn"
            >
              {{ loading ? '创建中...' : '立即创建' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </BaseLayout>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  Plus,
  Refresh,
  Upload,
  Loading,
  PriceTag,
  EditPen,
  CircleCheck,
  Remove,
} from '@element-plus/icons-vue';
import BaseLayout from '../layouts/BaseLayout.vue';
import agentService from '@/services/agent';
import { fileUploadApi } from '@/services/fileUpload';

export default defineComponent({
  name: 'AgentCreate',
  components: {
    BaseLayout,
    PriceTag,
    EditPen,
    CircleCheck,
    Remove,
  },
  setup() {
    const router = useRouter();
    const loading = ref(false);
    const fileInput = ref<HTMLInputElement | null>(null);
    const uploading = ref(false);

    const agentForm = reactive({
      name: '',
      description: '',
      avatar: '',
      category: '',
      tags: '',
      prompt: '',
      status: 'draft',
      humanReviewEnabled: false,
    });

    // 组件挂载时生成随机头像
    onMounted(() => {
      agentForm.avatar = generateFallbackAvatar();
    });

    // 备用头像生成函数
    const generateFallbackAvatar = (): string => {
      const colors = [
        '3B82F6',
        '8B5CF6',
        '10B981',
        'F59E0B',
        'EF4444',
        '6366F1',
        'EC4899',
        '14B8A6',
      ];
      const randomColor = colors[Math.floor(Math.random() * colors.length)];
      const letters = ['AI', '数据', '智能', 'DA', 'BI', 'ML', 'DL', 'NL'];
      const randomLetter = letters[Math.floor(Math.random() * letters.length)];

      const svg = `<svg width="200" height="200" xmlns="http://www.w3.org/2000/svg">
        <rect width="200" height="200" fill="#${randomColor}"/>
        <text x="100" y="120" font-family="Arial, sans-serif" font-size="48" font-weight="bold" text-anchor="middle" fill="white">${randomLetter}</text>
      </svg>`;

      return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`;
    };

    const goBack = () => {
      router.push('/agents');
    };

    const regenerateAvatar = () => {
      agentForm.avatar = generateFallbackAvatar();
    };

    // 触发文件选择
    const triggerFileUpload = () => {
      if (fileInput.value) {
        fileInput.value.click();
      }
    };

    // 处理文件上传
    const handleFileUpload = async (event: Event) => {
      const target = event.target as HTMLInputElement;
      const file = target.files?.[0];
      if (!file) return;

      // 验证文件类型
      if (!file.type.startsWith('image/')) {
        ElMessage.error('请选择图片文件');
        return;
      }

      // 验证文件大小 (5MB)
      if (file.size > 5 * 1024 * 1024) {
        ElMessage.error('图片大小不能超过5MB');
        return;
      }

      try {
        uploading.value = true;

        // 显示上传中的预览（使用base64）
        const reader = new FileReader();
        reader.onload = e => {
          if (e.target?.result) {
            agentForm.avatar = e.target.result as string;
          }
        };
        reader.readAsDataURL(file);

        // 上传文件
        const response = await fileUploadApi.uploadAvatar(file);

        if (response.success) {
          // 上传成功，使用服务器返回的URL
          agentForm.avatar = response.url;
          ElMessage.success('头像上传成功');
        } else {
          throw new Error(response.message || '上传失败');
        }
      } catch (error) {
        console.error('头像上传失败:', error);
        ElMessage.error('头像上传失败: ' + (error instanceof Error ? error.message : '未知错误'));
        // 恢复之前的头像
        agentForm.avatar = generateFallbackAvatar();
      } finally {
        uploading.value = false;
        // 清空文件输入
        if (fileInput.value) {
          fileInput.value.value = '';
        }
      }
    };

    // 图片加载成功处理
    const handleImageLoad = () => {
      console.log('头像图片加载成功');
    };

    // 图片加载失败处理
    const handleImageError = () => {
      console.error('头像图片加载失败');
      // 设置默认头像
      agentForm.avatar = generateFallbackAvatar();
    };

    const createAgent = async () => {
      if (!agentForm.name.trim() || !agentForm.category.trim() || !agentForm.tags.trim()) {
        ElMessage.error('请填写必要的字段！');
        return;
      }

      try {
        loading.value = true;

        const agentData = {
          name: agentForm.name.trim(),
          description: agentForm.description.trim(),
          avatar: agentForm.avatar.trim(),
          category: agentForm.category.trim(),
          tags: agentForm.tags.trim(),
          prompt: agentForm.prompt.trim(),
          status: agentForm.status,
          humanReviewEnabled: agentForm.humanReviewEnabled ? 1 : 0,
        };

        const result = await agentService.create(agentData);

        ElMessage.success(
          `智能体创建成功！状态：${agentData.status === 'published' ? '已发布' : '草稿'}`,
        );
        await router.push(`/agent/${result.id}`);
      } catch (error) {
        console.error('创建智能体失败:', error);
        ElMessage.error('创建失败，请重试');
      } finally {
        loading.value = false;
      }
    };

    return {
      Plus,
      Refresh,
      Upload,
      Loading,
      agentForm,
      loading,
      fileInput,
      uploading,
      goBack,
      regenerateAvatar,
      triggerFileUpload,
      handleFileUpload,
      handleImageLoad,
      handleImageError,
      createAgent,
    };
  },
});
</script>

<style scoped>
.agent-create-page {
  padding: 32px 20px 100px;
  background: #f8fafc;
  min-height: 100vh;
}

.create-form-wrapper {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 32px;
  text-align: center;
}

.page-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.page-header p {
  color: #64748b;
  margin: 0;
  font-size: 16px;
}

.section-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  border: 1px solid #e2e8f0;
}

/* 布局容器 */
.form-layout-container {
  display: flex;
  gap: 48px;
  align-items: flex-start;
}

.form-column-left {
  flex: 0 0 320px;
  display: flex;
  flex-direction: column;
}

.form-column-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

/* 头像区域 */
.avatar-group {
  margin-bottom: 32px;
}

.avatar-upload {
  display: flex;
  gap: 24px;
  align-items: center;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  border: 4px solid #fff;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  background: #f1f5f9;
  flex-shrink: 0;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-controls {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.avatar-buttons {
  display: flex;
  gap: 12px;
}

.avatar-tip {
  margin: 0;
  font-size: 13px;
  color: #94a3b8;
}

/* 表单通用样式 */
.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  font-size: 14px;
  color: #334155;
}

.required {
  color: #ef4444;
  margin-left: 4px;
}

.form-item {
  margin-bottom: 24px;
}

.full-height-item {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.custom-input :deep(.el-input__wrapper),
.custom-select :deep(.el-select__wrapper),
.custom-textarea :deep(.el-textarea__inner) {
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  transition: all 0.2s;
  padding-left: 12px;
}

.custom-input :deep(.el-input__wrapper):hover,
.custom-select :deep(.el-select__wrapper):hover,
.custom-textarea :deep(.el-textarea__inner):hover {
  border-color: #94a3b8;
}

.custom-input :deep(.el-input__wrapper.is-focus),
.custom-select :deep(.el-select__wrapper.is-focused),
.custom-textarea :deep(.el-textarea__inner:focus) {
  border-color: #6366f1;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2);
}

.custom-textarea :deep(.el-textarea__inner) {
  padding: 12px;
  font-family: inherit;
}

.prompt-editor-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.prompt-textarea :deep(.el-textarea__inner) {
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  line-height: 1.6;
  background-color: #f8fafc;
  height: 100% !important;
  min-height: 400px;
}

.field-help {
  margin-top: 8px;
  font-size: 12px;
  color: #94a3b8;
}

/* 底部固定操作栏 */
.bottom-actions-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 16px 0;
  box-shadow: 0 -4px 6px -1px rgba(0, 0, 0, 0.05);
  z-index: 50;
  border-top: 1px solid #e2e8f0;
}

.actions-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 40px;
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

.cancel-btn {
  width: 100px;
}

.submit-btn {
  width: 140px;
  font-weight: 600;
  box-shadow: 0 4px 6px -1px rgba(79, 70, 229, 0.2);
}

/* Responsive */
@media (max-width: 1024px) {
  .form-layout-container {
    flex-direction: column;
    gap: 0;
  }

  .form-column-left {
    flex: none;
    width: 100%;
    margin-bottom: 24px;
  }

  .form-column-right {
    width: 100%;
  }
}
</style>
