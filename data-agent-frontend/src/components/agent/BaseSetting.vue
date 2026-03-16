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
  <div class="agent-base-setting">
    <div class="setting-card">
      <div class="card-header">
        <h2 class="section-title">基本信息</h2>
        <p class="section-desc">配置智能体的核心身份信息，这些信息将直接展示给最终用户。</p>
      </div>

      <div class="form-container">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="form-item">
              <label class="form-label">
                智能体名称
                <span class="required">*</span>
              </label>
              <el-input
                v-model="props.agent.name"
                placeholder="给智能体起个响亮的名字"
                size="large"
                class="custom-input"
              />
            </div>
          </el-col>
          <el-col :span="12">
            <div class="form-item">
              <label class="form-label">
                分类
                <span class="required">*</span>
              </label>
              <el-input
                v-model="props.agent.category"
                placeholder="例如：数据分析、财务助手"
                size="large"
                class="custom-input"
              />
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <div class="form-item">
              <label class="form-label">功能描述</label>
              <el-input
                v-model="props.agent.description"
                :rows="3"
                type="textarea"
                placeholder="简要描述智能体的主要功能和用途..."
                size="large"
                class="custom-textarea"
                resize="none"
              />
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <div class="form-item">
              <label class="form-label">系统提示词 (System Prompt)</label>
              <div class="prompt-editor-wrapper">
                <el-input
                  v-model="props.agent.prompt"
                  :rows="6"
                  type="textarea"
                  placeholder="设定智能体的人设、职责和行为准则..."
                  size="large"
                  class="custom-textarea prompt-textarea"
                />
              </div>
              <p class="field-help">定义智能体的行为模式和专业领域，支持 Markdown 格式。</p>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <div class="form-item">
              <label class="form-label">标签</label>
              <el-input
                v-model="props.agent.tags"
                placeholder="输入标签，用逗号分隔"
                size="large"
                class="custom-input"
              >
                <template #prefix>
                  <el-icon class="el-input__icon"><PriceTag /></el-icon>
                </template>
              </el-input>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="form-item">
              <label class="form-label">发布状态</label>
              <el-select
                v-model="props.agent.status"
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
          </el-col>
        </el-row>

        <div class="form-divider"></div>

        <el-row :gutter="20" class="meta-info">
          <el-col :span="12">
            <div class="meta-item">
              <span class="meta-label">创建时间：</span>
              <span class="meta-value">{{ formattedCreateTime }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="meta-item">
              <span class="meta-label">最后更新：</span>
              <span class="meta-value">{{ formattedUpdateTime }}</span>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="card-footer">
        <div class="footer-actions">
          <el-button
            class="delete-btn"
            type="danger"
            plain
            :icon="Delete"
            size="large"
            @click="handleDeleteAgent"
          >
            删除智能体
          </el-button>
          <el-button
            class="save-btn"
            type="primary"
            :icon="Check"
            size="large"
            @click="updateAgent"
          >
            保存配置
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Agent } from '@/services/agent';
import agentService from '@/services/agent';
import { defineComponent, computed, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Edit,
  Delete,
  Check,
  PriceTag,
  EditPen,
  CircleCheck,
  Remove,
} from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';

export default defineComponent({
  name: 'AgentBaseSetting',
  components: {
    PriceTag,
    EditPen,
    CircleCheck,
    Remove,
  },
  props: {
    agent: {
      type: Object as () => Agent,
      required: true,
    },
  },
  setup(props) {
    const router = useRouter();

    const updateAgent = async () => {
      if (!props.agent.name || !props.agent.category) {
        ElMessage.warning('请填写必填项（名称、分类）');
        return;
      }

      try {
        const agent = await agentService.update(props.agent.id, props.agent);
        if (agent === null) {
          console.error('更新智能体失败:', agent);
          ElMessage.error('更新失败：未知错误');
        } else {
          ElMessage.success('配置已保存');
          // 保存成功后跳转回智能体列表页
          router.push('/agents');
        }
      } catch (e: any) {
        console.error('更新智能体失败:', e);
        ElMessage.error('更新失败：' + (e.message || '未知错误'));
      }
    };

    const handleDeleteAgent = async () => {
      try {
        await ElMessageBox.confirm(
          `确定要删除智能体 "${props.agent.name}" 吗？<br><span style="color: #909399; font-size: 13px;">此操作将永久删除该智能体及其所有配置，无法恢复。</span>`,
          '危险操作警告',
          {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'warning',
            dangerouslyUseHTMLString: true,
            confirmButtonClass: 'el-button--danger',
          },
        );

        const result = await agentService.delete(props.agent.id);
        if (result) {
          ElMessage.success('智能体已删除');
          await router.push('/agents');
        } else {
          ElMessage.error('删除失败：智能体不存在');
        }
      } catch {
        // 用户点击取消按钮，忽略此错误
      }
    };

    const formatDateTime = (dateString: string): string => {
      if (!dateString) return '-';
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      });
    };

    // 创建计算属性
    const formattedCreateTime = computed(() => formatDateTime(props.agent.createTime || ''));
    const formattedUpdateTime = computed(() => formatDateTime(props.agent.updateTime || ''));

    return {
      Edit,
      Delete,
      Check,
      props,
      updateAgent,
      handleDeleteAgent,
      formattedCreateTime,
      formattedUpdateTime,
    };
  },
});
</script>

<style scoped>
.agent-base-setting {
  width: 100%;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.setting-card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.02);
  border: 1px solid #e2e8f0;
  overflow: hidden;
}

.card-header {
  padding: 1.25rem 1.25rem;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  margin-bottom: 1.5rem;
}

.section-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #0f172a;
  margin: 0 0 0.25rem 0;
  letter-spacing: -0.01em;
}

.section-desc {
  color: #64748b;
  margin: 0;
  font-size: 0.85rem;
}

.form-container {
  padding: 0 1.25rem 1.25rem 1.25rem;
}

.form-item {
  margin-bottom: 1.25rem;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  font-size: 0.9rem;
  color: #334155;
}

.required {
  color: #ef4444;
  margin-left: 4px;
}

.custom-input :deep(.el-input__wrapper),
.custom-select :deep(.el-select__wrapper),
.custom-textarea :deep(.el-textarea__inner) {
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  transition: all 0.2s;
  background-color: #ffffff;
}

.custom-input :deep(.el-input__wrapper:hover),
.custom-select :deep(.el-select__wrapper:hover),
.custom-textarea :deep(.el-textarea__inner:hover) {
  border-color: #cbd5e1;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.05);
}

.custom-input :deep(.el-input__wrapper.is-focus),
.custom-select :deep(.el-select__wrapper.is-focused),
.custom-textarea :deep(.el-textarea__inner:focus) {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.prompt-editor-wrapper {
  border-radius: 8px;
  overflow: hidden;
}

.prompt-textarea :deep(.el-textarea__inner) {
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  font-size: 0.9rem;
  line-height: 1.6;
  background-color: #f8fafc;
}

.field-help {
  margin-top: 0.5rem;
  font-size: 0.8rem;
  color: #94a3b8;
}

.form-divider {
  height: 1px;
  background: #f1f5f9;
  margin: 1rem 0 1.5rem;
}

.meta-info {
  color: #94a3b8;
  font-size: 0.85rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.meta-label {
  font-weight: 500;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  padding: 1.25rem 1.25rem;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  margin-top: 0;
}

.footer-actions {
  display: flex;
  gap: 1rem;
}

.save-btn {
  min-width: 120px;
  font-weight: 600;
  box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.2);
}

.delete-btn {
  min-width: 100px;
}
</style>
