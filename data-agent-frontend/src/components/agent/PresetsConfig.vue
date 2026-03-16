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
  <div class="config-card">
    <div class="card-header">
      <h2 class="section-title">预设问题管理</h2>
      <p class="section-desc">配置智能体的常用预设问题，帮助用户快速开始对话。</p>
    </div>

    <div class="action-bar">
      <div class="search-wrapper">
        <!-- 预留搜索框位置，保持布局一致性 -->
      </div>
      <el-button type="primary" @click="openCreateDialog" :icon="Plus">添加问题</el-button>
    </div>

    <div class="table-container">
      <el-table
        :data="presetQuestionList"
        style="width: 100%"
        :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600' }"
        :row-style="{ height: '56px' }"
      >
        <el-table-column prop="id" label="ID" min-width="60" />
        <el-table-column prop="question" label="问题内容" min-width="250" show-overflow-tooltip>
          <template #default="scope">
            <span class="question-text">{{ scope.row.question }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" min-width="80" align="center" />
        <el-table-column label="状态" min-width="100" align="center">
          <template #default="scope">
            <el-tag
              :type="scope.row.isActive ? 'success' : 'info'"
              effect="plain"
              round
              size="small"
            >
              {{ scope.row.isActive ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" min-width="150" fixed="right">
          <template #default="scope">
            <el-button @click="editQuestion(scope.row)" type="primary" link :icon="Edit">
              编辑
            </el-button>
            <el-button @click="deleteQuestion(scope.row)" type="danger" link :icon="Delete">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加/编辑预设问题Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑预设问题' : '添加预设问题'"
      width="520px"
      class="custom-dialog"
      align-center
    >
      <el-form
        :model="questionForm"
        label-width="80px"
        ref="questionFormRef"
        label-position="top"
        class="custom-form"
      >
        <el-form-item label="问题内容" prop="question" required>
          <el-input
            v-model="questionForm.question"
            type="textarea"
            :rows="4"
            placeholder="请输入预设问题内容，例如：帮我查询最近的销售数据"
            resize="none"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="显示排序" prop="sortOrder">
              <el-input-number
                v-model="questionForm.sortOrder"
                :min="0"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用状态" prop="isActive">
              <div class="switch-wrapper">
                <el-switch v-model="questionForm.isActive" />
                <span class="switch-label">{{ questionForm.isActive ? '已启用' : '已禁用' }}</span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveQuestion">
            {{ isEdit ? '保存修改' : '立即创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, Ref } from 'vue';
import { Plus, Edit, Delete } from '@element-plus/icons-vue';
import presetQuestionService, {
  PresetQuestion,
  PresetQuestionDTO,
} from '@/services/presetQuestion';
import { ElMessage, ElMessageBox } from 'element-plus';

export default defineComponent({
  name: 'AgentPresetsConfig',
  props: {
    agentId: {
      type: Number,
      required: true,
    },
  },
  setup(props) {
    const presetQuestionList: Ref<PresetQuestion[]> = ref([]);
    const dialogVisible: Ref<boolean> = ref(false);
    const isEdit: Ref<boolean> = ref(false);
    const questionForm: Ref<PresetQuestion> = ref({
      agentId: props.agentId,
      question: '',
      sortOrder: 0,
      isActive: true,
    });
    const currentEditId: Ref<number | null> = ref(null);

    const openCreateDialog = () => {
      isEdit.value = false;
      questionForm.value = {
        agentId: props.agentId,
        question: '',
        sortOrder: 0,
        isActive: true,
      };
      dialogVisible.value = true;
    };

    const loadPresetQuestions = async () => {
      try {
        presetQuestionList.value = await presetQuestionService.list(props.agentId);
      } catch (error) {
        ElMessage.error('加载预设问题列表失败');
        console.error('加载预设问题失败:', error);
      }
    };

    const editQuestion = (question: PresetQuestion) => {
      isEdit.value = true;
      currentEditId.value = question.id || null;
      // Use spread to clone the object to avoid direct mutation
      questionForm.value = { ...question };
      dialogVisible.value = true;
    };

    const deleteQuestion = async (question: PresetQuestion) => {
      if (!question.id) return;

      try {
        await ElMessageBox.confirm(
          `确定要删除预设问题 "${question.question.substring(0, 50)}..." 吗？`,
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
            title: '删除确认',
          },
        );

        const result = await presetQuestionService.delete(props.agentId, question.id);
        if (result) {
          ElMessage.success('删除成功');
          await loadPresetQuestions();
        } else {
          ElMessage.error('删除失败');
        }
      } catch {
        // User cancelled
      }
    };

    const saveQuestion = async () => {
      try {
        if (!questionForm.value.question || questionForm.value.question.trim() === '') {
          ElMessage.error('请输入预设问题');
          return;
        }

        let questionsToSave: PresetQuestionDTO[] = [];

        // Logic preserved: Batch save approach
        if (isEdit.value && currentEditId.value) {
          questionsToSave = presetQuestionList.value.map(q => {
            const dto: PresetQuestionDTO = {
              question: q.id === currentEditId.value ? questionForm.value.question : q.question,
            };
            if (q.id === currentEditId.value) {
              dto.isActive = questionForm.value.isActive === true;
            } else {
              dto.isActive = q.isActive === true;
            }
            return dto;
          });
        } else {
          // Creating new: add to list
          questionsToSave = [
            ...presetQuestionList.value.map(q => ({
              question: q.question,
              isActive: q.isActive === true,
            })),
            {
              question: questionForm.value.question,
              isActive: questionForm.value.isActive === true,
            },
          ];
        }

        console.log('发送的数据:', JSON.stringify(questionsToSave));

        const result = await presetQuestionService.batchSave(props.agentId, questionsToSave);
        if (result) {
          ElMessage.success(isEdit.value ? '更新成功' : '创建成功');
          dialogVisible.value = false;
          await loadPresetQuestions();
        } else {
          ElMessage.error(isEdit.value ? '更新失败' : '创建失败');
        }
      } catch (error) {
        ElMessage.error(`${isEdit.value ? '更新' : '创建'}失败`);
        console.error('保存预设问题失败:', error);
      }
    };

    onMounted(() => {
      loadPresetQuestions();
    });

    return {
      Plus,
      Edit,
      Delete,
      presetQuestionList,
      dialogVisible,
      isEdit,
      questionForm,
      openCreateDialog,
      editQuestion,
      deleteQuestion,
      saveQuestion,
    };
  },
});
</script>

<style scoped>
.config-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px -1px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  background-color: #ffffff;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-desc {
  font-size: 14px;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

.action-bar {
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.search-wrapper {
  display: flex;
  gap: 12px;
  width: 300px;
}

.table-container {
  padding: 0;
  flex: 1;
  overflow: hidden;
}

.question-text {
  font-weight: 500;
  color: #334155;
}

/* Dialog Styles */
:deep(.custom-dialog) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
}

:deep(.el-dialog__header) {
  margin: 0;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  background-color: #f8fafc;
}

:deep(.el-dialog__title) {
  font-weight: 600;
  color: #0f172a;
  font-size: 16px;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #e2e8f0;
  background-color: #f8fafc;
}

.custom-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #334155;
  padding-bottom: 8px;
}

.switch-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 32px;
}

.switch-label {
  font-size: 14px;
  color: #64748b;
}

/* Button & Tag Styles */
:deep(.el-button--primary.is-link) {
  padding: 4px 8px;
  height: auto;
  font-weight: 500;
}

:deep(.el-button--danger.is-link) {
  padding: 4px 8px;
  height: auto;
  font-weight: 500;
}

:deep(.el-tag) {
  border: none;
  font-weight: 500;
}

:deep(.el-table__inner-wrapper::before) {
  display: none;
}
</style>
