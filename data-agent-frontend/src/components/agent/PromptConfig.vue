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
<!-- 提示词优化配置组件 -->
<template>
  <div class="prompt-optimization-config">
    <!-- 消息提示 -->
    <div v-if="message.show" class="message-toast" :class="message.type">
      <span>{{ message.text }}</span>
      <button class="message-close" @click="hideMessage">×</button>
    </div>

    <div class="config-card">
      <div class="card-header">
        <h3 class="section-title">增强式Prompt优化配置</h3>
        <p class="section-desc">
          配置的Prompt仅用作效果优化，支持多个提示词配置，在原始模板基础上进行增强。示例配置：
        </p>
        <ul class="optimization-tips">
          <li>1. 查询的年销售额精确到小数点后两位。</li>
          <li>2. 报告格式第一章节请先总结年销售额</li>
        </ul>
      </div>

      <!-- 智能体Prompt -->
      <div class="section-container">
        <h4 class="subsection-title">智能体Prompt</h4>
        <div class="prompt-display">
          {{
            agentPrompt ||
            '你是一个销售数据分析专家，能够帮助用户分析销售趋势，客户行为和业务指标。'
          }}
        </div>
      </div>

      <!-- 优化配置列表 -->
      <div class="section-container">
        <div class="config-list-header">
          <h4 class="subsection-title">优化配置列表</h4>
          <div class="header-actions">
            <el-button
              v-if="optimizationConfigs.length > 0"
              class="batch-action-btn"
              @click="showBatchActions = !showBatchActions"
              size="small"
              plain
            >
              <el-icon><Setting /></el-icon>
              批量操作
            </el-button>
            <el-button
              class="add-config-btn"
              @click="showAddConfigDialog = true"
              type="primary"
              size="small"
            >
              <el-icon><Plus /></el-icon>
              添加优化配置
            </el-button>
          </div>
        </div>

        <!-- 批量操作面板 -->
        <div v-if="showBatchActions" class="batch-actions-panel">
          <div class="batch-actions-content">
            <div class="batch-selection">
              <el-checkbox
                v-model="isAllSelected"
                :indeterminate="isIndeterminate"
                @change="toggleSelectAll"
              >
                {{ isAllSelected ? '已全选' : `已选择 ${selectedConfigs.length} 个配置` }}
              </el-checkbox>
            </div>
            <div class="batch-buttons">
              <el-button
                type="success"
                size="small"
                @click="batchEnable"
                :disabled="selectedConfigs.length === 0"
                plain
              >
                批量启用
              </el-button>
              <el-button
                type="warning"
                size="small"
                @click="batchDisable"
                :disabled="selectedConfigs.length === 0"
                plain
              >
                批量禁用
              </el-button>
              <el-button size="small" @click="clearSelection">取消选择</el-button>
            </div>
          </div>
        </div>

        <div v-if="optimizationConfigs.length === 0" class="empty-state">
          <el-icon class="empty-icon"><Document /></el-icon>
          <p>暂无优化配置，点击"添加优化配置"开始配置</p>
        </div>

        <div v-else class="config-list">
          <div
            v-for="config in optimizationConfigs"
            :key="config.id"
            class="config-item"
            :class="{ disabled: !config.enabled, selected: selectedConfigs.includes(config.id) }"
          >
            <div class="config-item-header">
              <div class="config-info">
                <el-checkbox v-model="selectedConfigs" :label="config.id" class="config-checkbox" />
                <span class="config-name">{{ config.name }}</span>
                <el-tag
                  v-if="config.priority !== undefined"
                  size="small"
                  effect="plain"
                  class="priority-tag"
                >
                  优先级: {{ config.priority }}
                </el-tag>
              </div>
              <div class="config-actions">
                <el-switch
                  v-model="config.enabled"
                  size="small"
                  @change="toggleConfig(config)"
                  style="--el-switch-on-color: #10b981; margin-right: 8px"
                />
                <el-button type="primary" link size="small" @click="editConfig(config)">
                  编辑
                </el-button>
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleShowPriorityDialog(config)"
                >
                  优先级
                </el-button>
                <el-button type="danger" link size="small" @click="deleteConfig(config.id)">
                  删除
                </el-button>
              </div>
            </div>
            <div class="config-content">
              <p class="config-description-text">{{ config.description }}</p>
              <div class="optimization-prompt">
                {{ config.optimizationPrompt }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑配置对话框 -->
    <el-dialog
      v-model="showAddConfigDialog"
      :title="editingConfig ? '编辑优化配置' : '添加优化配置'"
      width="600px"
      @closed="closeDialog"
    >
      <el-form :model="formData" label-width="100px" class="config-form">
        <el-form-item label="配置名称" required>
          <el-input v-model="formData.name" placeholder="请输入配置名称" />
        </el-form-item>

        <el-form-item label="配置描述">
          <el-input v-model="formData.description" placeholder="请输入配置描述" />
        </el-form-item>

        <el-form-item label="优化提示词" required>
          <el-input
            v-model="formData.optimizationPrompt"
            type="textarea"
            :rows="6"
            placeholder="请输入优化提示词内容，支持模板变量如 {user_requirements_and_plan}"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-input-number
                v-model="formData.priority"
                :min="0"
                :max="100"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示顺序">
              <el-input-number
                v-model="formData.displayOrder"
                :min="0"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeDialog">取消</el-button>
          <el-button type="primary" @click="saveConfig">保存配置</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 优先级设置对话框 -->
    <el-dialog
      v-model="showPriorityDialog"
      title="设置优先级"
      width="400px"
      @closed="closePriorityDialog"
    >
      <el-form :model="priorityForm" label-width="100px" class="priority-form">
        <el-form-item label="优先级">
          <el-input-number
            v-model="priorityForm.priority"
            :min="0"
            :max="100"
            controls-position="right"
            style="width: 100%"
          />
          <div class="form-hint">优先级越高，该配置在多个配置中的执行顺序越靠前</div>
        </el-form-item>
        <el-form-item label="显示顺序">
          <el-input-number
            v-model="priorityForm.displayOrder"
            :min="0"
            controls-position="right"
            style="width: 100%"
          />
          <div class="form-hint">控制配置在列表中的显示顺序</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closePriorityDialog">取消</el-button>
          <el-button type="primary" @click="updatePriority">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Plus, Setting, Document } from '@element-plus/icons-vue';

export default {
  name: 'AgentPromptConfig',
  components: {
    Plus,
    Setting,
    Document,
  },
  props: {
    agentId: {
      type: [String, Number],
      required: true,
    },
    promptType: {
      type: String,
      default: 'report-generator',
    },
    agentPrompt: {
      type: String,
      default: '',
    },
  },
  data() {
    return {
      optimizationConfigs: [],
      showAddConfigDialog: false,
      editingConfig: null,
      showBatchActions: false,
      showPriorityDialog: false,
      selectedConfigs: [],
      editingPriorityConfig: null,
      formData: {
        name: '',
        description: '',
        optimizationPrompt: '',
        priority: 0,
        displayOrder: 0,
      },
      priorityForm: {
        priority: 0,
        displayOrder: 0,
      },
      loading: false,
      message: {
        show: false,
        text: '',
        type: 'success',
      },
    };
  },
  computed: {
    isAllSelected: {
      get() {
        return (
          this.optimizationConfigs.length > 0 &&
          this.selectedConfigs.length === this.optimizationConfigs.length
        );
      },
      set(val) {
        this.toggleSelectAll(val);
      },
    },
    isIndeterminate() {
      return (
        this.selectedConfigs.length > 0 &&
        this.selectedConfigs.length < this.optimizationConfigs.length
      );
    },
  },
  mounted() {
    this.loadOptimizationConfigs();
  },
  methods: {
    async loadOptimizationConfigs() {
      try {
        this.loading = true;
        const query = this.agentId ? `?agentId=${this.agentId}` : '';
        const response = await fetch(`/api/prompt-config/list-by-type/${this.promptType}${query}`);
        const result = await response.json();
        if (result.success) {
          this.optimizationConfigs = result.data || [];
          if (this.optimizationConfigs.length === 0) {
            this.showBatchActions = false;
            this.selectedConfigs = [];
          }
        }
      } catch (error) {
        console.error('加载优化配置失败:', error);
        this.showMessage('加载优化配置失败', 'error');
      } finally {
        this.loading = false;
      }
    },

    async saveConfig() {
      try {
        const configData = {
          ...this.formData,
          promptType: this.promptType,
          agentId: this.agentId ? Number(this.agentId) : null,
          enabled: true,
          creator: 'user',
          priority: this.formData.priority || 0,
          displayOrder: this.formData.displayOrder || 0,
        };

        if (this.editingConfig) {
          configData.id = this.editingConfig.id;
        }

        const response = await fetch('/api/prompt-config/save', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(configData),
        });

        const result = await response.json();
        if (result.success) {
          this.showMessage(result.message || '保存成功', 'success');
          this.closeDialog();
          this.loadOptimizationConfigs();
        } else {
          this.showMessage(result.message || '保存失败', 'error');
        }
      } catch (error) {
        console.error('保存配置失败:', error);
        this.showMessage('保存配置失败', 'error');
      }
    },

    async toggleConfig(config) {
      try {
        // 注意：switch 控件已经改变了 config.enabled 的值
        // 我们需要根据新的值来调用对应的 API
        // 或者如果 switch 只是触发事件，这里根据当前状态取反
        // 这里假设 switch 绑定了 v-model，所以 config.enabled 已经是新的值
        const url = !config.enabled
          ? `/api/prompt-config/${config.id}/disable`
          : `/api/prompt-config/${config.id}/enable`;

        const response = await fetch(url, { method: 'POST' });
        const result = await response.json();

        if (result.success) {
          this.showMessage(result.message, 'success');
          // 重新加载以确保状态同步，或者直接信任前端状态
          // this.loadOptimizationConfigs();
        } else {
          // 失败时恢复状态
          config.enabled = !config.enabled;
          this.showMessage(result.message, 'error');
        }
      } catch (error) {
        config.enabled = !config.enabled;
        console.error('切换配置状态失败:', error);
        this.showMessage('操作失败', 'error');
      }
    },

    async deleteConfig(configId) {
      try {
        await this.$confirm('确定要删除这个优化配置吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        });

        const response = await fetch(`/api/prompt-config/${configId}`, {
          method: 'DELETE',
        });
        const result = await response.json();

        if (result.success) {
          this.showMessage(result.message, 'success');
          this.loadOptimizationConfigs();
          this.selectedConfigs = this.selectedConfigs.filter(id => id !== configId);
        } else {
          this.showMessage(result.message, 'error');
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除配置失败:', error);
          this.showMessage('删除配置失败', 'error');
        }
      }
    },

    editConfig(config) {
      this.editingConfig = config;
      this.formData = {
        name: config.name,
        description: config.description,
        optimizationPrompt: config.optimizationPrompt,
        priority: config.priority || 0,
        displayOrder: config.displayOrder || 0,
      };
      this.showAddConfigDialog = true;
    },

    closeDialog() {
      this.showAddConfigDialog = false;
      this.editingConfig = null;
      this.formData = {
        name: '',
        description: '',
        optimizationPrompt: '',
        priority: 0,
        displayOrder: 0,
      };
    },

    async batchEnable() {
      if (this.selectedConfigs.length === 0) return;

      try {
        const response = await fetch('/api/prompt-config/batch-enable', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(this.selectedConfigs),
        });

        const result = await response.json();
        if (result.success) {
          this.showMessage(result.message, 'success');
          this.loadOptimizationConfigs();
          this.clearSelection();
        } else {
          this.showMessage(result.message, 'error');
        }
      } catch (error) {
        console.error('批量启用失败:', error);
        this.showMessage('批量启用失败', 'error');
      }
    },

    async batchDisable() {
      if (this.selectedConfigs.length === 0) return;

      try {
        const response = await fetch('/api/prompt-config/batch-disable', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(this.selectedConfigs),
        });

        const result = await response.json();
        if (result.success) {
          this.showMessage(result.message, 'success');
          this.loadOptimizationConfigs();
          this.clearSelection();
        } else {
          this.showMessage(result.message, 'error');
        }
      } catch (error) {
        console.error('批量禁用失败:', error);
        this.showMessage('批量禁用失败', 'error');
      }
    },

    clearSelection() {
      this.selectedConfigs = [];
      this.showBatchActions = false;
    },

    toggleSelectAll(val) {
      if (val) {
        this.selectedConfigs = this.optimizationConfigs.map(config => config.id);
      } else {
        this.selectedConfigs = [];
      }
    },

    handleShowPriorityDialog(config) {
      this.editingPriorityConfig = config;
      this.priorityForm = {
        priority: config.priority || 0,
        displayOrder: config.displayOrder || 0,
      };
      this.showPriorityDialog = true;
    },

    async updatePriority() {
      try {
        const response = await fetch(
          `/api/prompt-config/${this.editingPriorityConfig.id}/priority`,
          {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({ priority: this.priorityForm.priority }),
          },
        );

        const result = await response.json();
        if (result.success) {
          this.showMessage('优先级更新成功', 'success');
          this.loadOptimizationConfigs();
          this.closePriorityDialog();
        } else {
          this.showMessage(result.message, 'error');
        }
      } catch (error) {
        console.error('更新优先级失败:', error);
        this.showMessage('更新优先级失败', 'error');
      }
    },

    closePriorityDialog() {
      this.showPriorityDialog = false;
      this.editingPriorityConfig = null;
      this.priorityForm = {
        priority: 0,
        displayOrder: 0,
      };
    },

    showMessage(text, type = 'success') {
      // 使用 Element Plus 的 Message
      if (type === 'success') {
        this.$message.success(text);
      } else {
        this.$message.error(text);
      }
    },

    hideMessage() {
      this.message.show = false;
    },
  },
};
</script>

<style scoped>
.prompt-optimization-config {
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

.config-card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.02);
  border: 1px solid #e2e8f0;
  overflow: hidden;
}

.card-header {
  padding: 1.25rem 1.5rem;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.section-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #0f172a;
  margin: 0 0 0.5rem 0;
  letter-spacing: -0.01em;
}

.section-desc {
  color: #64748b;
  margin: 0 0 12px 0;
  font-size: 0.85rem;
  line-height: 1.5;
}

.optimization-tips {
  margin: 0;
  padding-left: 20px;
  color: #64748b;
  font-size: 0.85rem;
  background: #f1f5f9;
  padding: 12px 12px 12px 32px;
  border-radius: 6px;
}

.optimization-tips li {
  margin-bottom: 4px;
}

.optimization-tips li:last-child {
  margin-bottom: 0;
}

.section-container {
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid #f1f5f9;
}

.section-container:last-child {
  border-bottom: none;
}

.subsection-title {
  font-size: 1rem;
  font-weight: 600;
  color: #334155;
  margin: 0 0 1rem 0;
}

.prompt-display {
  background: #f8fafc;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  color: #334155;
  line-height: 1.6;
  font-size: 0.9rem;
}

.config-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 批量操作面板样式 */
.batch-actions-panel {
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 16px;
}

.batch-actions-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.batch-buttons {
  display: flex;
  gap: 8px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #94a3b8;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px dashed #e2e8f0;
}

.empty-icon {
  font-size: 32px;
  margin-bottom: 10px;
  color: #cbd5e1;
}

.config-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.config-item {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #ffffff;
  transition: all 0.2s ease;
}

.config-item:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  border-color: #cbd5e1;
}

.config-item.disabled {
  opacity: 0.7;
  background: #f8fafc;
}

.config-item.selected {
  border-color: #6366f1;
  background: #eff6ff;
}

.config-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f1f5f9;
  background: #fcfcfc;
  border-radius: 8px 8px 0 0;
}

.config-info {
  display: flex;
  gap: 12px;
  align-items: center;
}

.config-name {
  font-weight: 600;
  color: #334155;
  font-size: 0.95rem;
}

.priority-tag {
  font-size: 12px;
}

.config-actions {
  display: flex;
  align-items: center;
}

.config-content {
  padding: 16px;
}

.config-description-text {
  margin: 0 0 12px 0;
  color: #64748b;
  font-size: 0.9rem;
}

.optimization-prompt {
  background: #f1f5f9;
  padding: 12px;
  border-radius: 6px;
  color: #475569;
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  font-size: 0.85rem;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
  border: 1px solid #e2e8f0;
}

.form-hint {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

/* 覆盖 Element Plus 默认样式 */
:deep(.el-dialog__header) {
  margin-right: 0;
  border-bottom: 1px solid #f1f5f9;
  padding-bottom: 20px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #f1f5f9;
  padding-top: 20px;
}
</style>
