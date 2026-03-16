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
  <div class="business-knowledge-config">
    <div class="config-card">
      <div class="card-header">
        <h2 class="section-title">业务知识管理</h2>
        <p class="section-desc">管理企业知识引擎，配置业务术语、黑话和常用表达。</p>
      </div>

      <!-- Toolbar -->
      <div class="action-bar">
        <div class="left-actions">
          <h3 class="subsection-title">业务知识列表</h3>
        </div>
        <div class="right-actions">
          <el-input
            v-model="searchKeyword"
            placeholder="请输入关键词搜索"
            style="width: 320px"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button
            @click="refreshVectorStore"
            :loading="refreshLoading"
            type="success"
            plain
            :icon="Document"
          >
            {{ refreshLoading ? '同步中...' : '同步到向量库' }}
          </el-button>
          <el-button @click="openCreateDialog" type="primary" :icon="Plus">添加知识</el-button>
        </div>
      </div>

      <!-- 表格区域 -->
      <div class="table-container">
        <el-table
          :data="businessKnowledgeList"
          style="width: 100%"
          :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600' }"
        >
          <el-table-column prop="id" label="ID" width="80px" align="center">
            <template #default="scope">
              <span class="text-gray-500">#{{ scope.row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="businessTerm" label="业务名词" min-width="150px">
            <template #default="scope">
              <span class="business-term">{{ scope.row.businessTerm }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="description"
            label="描述"
            min-width="200px"
            show-overflow-tooltip
          />
          <el-table-column prop="synonyms" label="同义词" min-width="150px">
            <template #default="scope">
              <div v-if="scope.row.synonyms" class="synonyms-tags">
                <el-tag
                  v-for="tag in scope.row.synonyms.split(/,|，/)"
                  :key="tag"
                  size="small"
                  effect="plain"
                  type="info"
                  class="synonym-tag"
                >
                  {{ tag.trim() }}
                </el-tag>
              </div>
              <span v-else class="text-gray-400 text-xs">-</span>
            </template>
          </el-table-column>
          <el-table-column label="向量化状态" min-width="120px">
            <template #default="scope">
              <el-tag
                :type="getVectorStatusType(scope.row.embeddingStatus)"
                size="small"
                effect="light"
              >
                <span class="flex items-center">
                  <el-icon
                    v-if="scope.row.embeddingStatus === 'PROCESSING'"
                    class="is-loading mr-1"
                  >
                    <Loading />
                  </el-icon>
                  <el-icon v-else-if="scope.row.embeddingStatus === 'COMPLETED'" class="mr-1">
                    <Check />
                  </el-icon>
                  <el-icon v-else-if="scope.row.embeddingStatus === 'FAILED'" class="mr-1">
                    <Warning />
                  </el-icon>
                  {{ getStatusText(scope.row.embeddingStatus) }}
                </span>
                <el-tooltip
                  v-if="scope.row.embeddingStatus === 'FAILED' && scope.row.errorMsg"
                  :content="scope.row.errorMsg"
                  placement="top"
                >
                  <el-icon class="ml-1 cursor-pointer"><Warning /></el-icon>
                </el-tooltip>
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="是否召回" min-width="100px">
            <template #default="scope">
              <el-switch
                :model-value="scope.row.isRecall"
                @change="val => toggleRecall(scope.row, val)"
                size="small"
                style="--el-switch-on-color: #10b981"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createdTime" label="创建时间" min-width="160px">
            <template #default="scope">
              <span class="text-gray-500 text-xs">{{ scope.row.createdTime }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200px" fixed="right">
            <template #default="scope">
              <div class="operation-buttons">
                <el-button @click="editKnowledge(scope.row)" size="small" type="primary" link>
                  编辑
                </el-button>
                <el-button
                  v-if="scope.row.embeddingStatus === 'FAILED'"
                  @click="retryEmbedding(scope.row)"
                  size="small"
                  type="warning"
                  link
                  :loading="retryLoadingMap[scope.row.id]"
                >
                  重试
                </el-button>
                <el-button @click="deleteKnowledge(scope.row)" size="small" type="danger" link>
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- todo: 添加分页 -->
    </div>

    <!-- 添加/编辑业务知识Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑业务知识' : '添加业务知识'"
      width="600px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        :model="knowledgeForm"
        label-width="100px"
        ref="knowledgeFormRef"
        class="knowledge-form"
      >
        <el-form-item label="业务名词" prop="businessTerm" required>
          <el-input v-model="knowledgeForm.businessTerm" placeholder="例如：GMV, DAU" />
        </el-form-item>

        <el-form-item label="描述" prop="description" required>
          <el-input
            v-model="knowledgeForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述该业务名词的定义、计算公式或业务含义..."
          />
        </el-form-item>

        <el-form-item label="同义词" prop="synonyms">
          <el-input
            v-model="knowledgeForm.synonyms"
            type="textarea"
            :rows="2"
            placeholder="请输入同义词，多个同义词用逗号分隔"
          />
          <div class="form-hint">例如：总交易额, 成交金额</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveKnowledge" :loading="saveLoading">
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, Ref } from 'vue';
import { Plus, Search, Document, Warning, Check, Loading } from '@element-plus/icons-vue';
import businessKnowledgeService, {
  BusinessKnowledgeVO,
  CreateBusinessKnowledgeDTO,
  UpdateBusinessKnowledgeDTO,
} from '@/services/businessKnowledge';
import { ElMessage, ElMessageBox } from 'element-plus';

export default defineComponent({
  name: 'BusinessKnowledgeConfig',
  components: {
    Search,
    Warning,
    Check,
    Loading,
  },
  props: {
    agentId: {
      type: Number,
      required: true,
    },
  },
  setup(props) {
    const businessKnowledgeList: Ref<BusinessKnowledgeVO[]> = ref([]);
    const dialogVisible: Ref<boolean> = ref(false);
    const isEdit: Ref<boolean> = ref(false);
    const searchKeyword: Ref<string> = ref('');
    const knowledgeForm: Ref<BusinessKnowledgeVO> = ref({
      businessTerm: '',
      description: '',
      synonyms: '',
      isRecall: false,
    } as BusinessKnowledgeVO);

    const currentEditId: Ref<number | null> = ref(null);
    const refreshLoading: Ref<boolean> = ref(false);
    const saveLoading: Ref<boolean> = ref(false);
    const retryLoadingMap: Ref<Record<number, boolean>> = ref({});

    const openCreateDialog = () => {
      isEdit.value = false;
      knowledgeForm.value = {
        businessTerm: '',
        description: '',
        synonyms: '',
        isRecall: true, // 默认为开启召回
      } as BusinessKnowledgeVO;
      dialogVisible.value = true;
    };

    // 处理搜索
    const handleSearch = () => {
      loadBusinessKnowledge();
    };

    // 加载业务知识列表
    const loadBusinessKnowledge = async () => {
      try {
        businessKnowledgeList.value = await businessKnowledgeService.list(
          props.agentId,
          searchKeyword.value || undefined,
        );
      } catch (error) {
        ElMessage.error('加载业务知识列表失败');
        console.error('Failed to load business knowledge:', error);
      }
    };

    // 编辑业务知识
    const editKnowledge = (knowledge: BusinessKnowledgeVO) => {
      isEdit.value = true;
      currentEditId.value = knowledge.id || null;
      knowledgeForm.value = { ...knowledge };
      dialogVisible.value = true;
    };

    // 删除业务知识
    const deleteKnowledge = async (knowledge: BusinessKnowledgeVO) => {
      if (!knowledge.id) return;

      try {
        await ElMessageBox.confirm(
          `确定要删除业务知识 "${knowledge.businessTerm}" 吗？`,
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          },
        );

        const result = await businessKnowledgeService.delete(knowledge.id);
        if (result) {
          ElMessage.success('删除成功');
          await loadBusinessKnowledge();
        } else {
          ElMessage.error('删除失败');
        }
      } catch {
        // 用户取消操作时不显示错误消息
      }
    };

    // 切换召回状态
    const toggleRecall = async (knowledge: BusinessKnowledgeVO, isRecall: boolean) => {
      if (!knowledge.id) return;

      try {
        const result = await businessKnowledgeService.recallKnowledge(knowledge.id, isRecall);
        if (result) {
          ElMessage.success(`${isRecall ? '设为召回' : '取消召回'}成功`);
          knowledge.isRecall = isRecall;
        } else {
          ElMessage.error(`${isRecall ? '设为召回' : '取消召回'}失败`);
        }
      } catch (error) {
        ElMessage.error(`${isRecall ? '设为召回' : '取消召回'}失败`);
        console.error('Failed to toggle recall:', error);
      }
    };

    // 保存业务知识
    const saveKnowledge = async () => {
      // 简单验证
      if (!knowledgeForm.value.businessTerm?.trim()) {
        ElMessage.warning('请输入业务名词');
        return;
      }
      if (!knowledgeForm.value.description?.trim()) {
        ElMessage.warning('请输入描述');
        return;
      }

      saveLoading.value = true;
      try {
        if (isEdit.value && currentEditId.value) {
          // 更新操作使用 UpdateBusinessKnowledgeDTO
          const updateData: UpdateBusinessKnowledgeDTO = {
            businessTerm: knowledgeForm.value.businessTerm,
            description: knowledgeForm.value.description,
            synonyms: knowledgeForm.value.synonyms,
            agentId: props.agentId,
          };

          const result = await businessKnowledgeService.update(currentEditId.value, updateData);
          if (result) {
            ElMessage.success('更新成功');
          } else {
            ElMessage.error('更新失败');
            return;
          }
        } else {
          // 创建操作使用 CreateBusinessKnowledgeDTO
          const createData: CreateBusinessKnowledgeDTO = {
            businessTerm: knowledgeForm.value.businessTerm,
            description: knowledgeForm.value.description,
            synonyms: knowledgeForm.value.synonyms,
            isRecall: knowledgeForm.value.isRecall,
            agentId: props.agentId,
          };

          await businessKnowledgeService.create(createData);
          ElMessage.success('创建成功');
        }

        dialogVisible.value = false;
        await loadBusinessKnowledge();
      } catch (error) {
        ElMessage.error(`${isEdit.value ? '更新' : '创建'}失败`);
        console.error('Failed to save knowledge:', error);
      } finally {
        saveLoading.value = false;
      }
    };

    // 刷新向量存储
    const refreshVectorStore = async () => {
      try {
        await ElMessageBox.confirm(
          '如果所有向量状态正常，即无需同步。确定要清除现有数据并开始重新同步吗？',
          '确认同步',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          },
        );

        refreshLoading.value = true;
        const result = await businessKnowledgeService.refreshAllKnowledgeToVectorStore(
          props.agentId.toString(),
        );
        if (result) {
          ElMessage.success('同步到向量库成功');
          await loadBusinessKnowledge(); // 刷新列表以更新状态
        } else {
          ElMessage.error('同步到向量库失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('同步到向量库失败');
          console.error('Failed to refresh vector store:', error);
        }
      } finally {
        refreshLoading.value = false;
      }
    };

    // 重试向量化
    const retryEmbedding = async (knowledge: BusinessKnowledgeVO) => {
      if (!knowledge.id) return;

      try {
        // 设置加载状态为true
        retryLoadingMap.value[knowledge.id] = true;

        const result = await businessKnowledgeService.retryEmbedding(knowledge.id);
        if (result) {
          ElMessage.success('重试向量化成功');
          // 刷新列表以更新状态
          await loadBusinessKnowledge();
        } else {
          ElMessage.error('重试向量化失败');
        }
      } catch (error) {
        ElMessage.error('重试向量化失败');
        console.error('Failed to retry vectorization:', error);
      } finally {
        // 无论成功还是失败，都将加载状态设置为false
        retryLoadingMap.value[knowledge.id] = false;
      }
    };

    // 获取向量化状态对应的标签类型
    const getVectorStatusType = (status?: string): string => {
      switch (status) {
        case 'COMPLETED':
          return 'success';
        case 'FAILED':
          return 'danger';
        case 'PENDING':
          return 'warning';
        case 'PROCESSING':
          return 'primary';
        default:
          return 'info';
      }
    };

    const getStatusText = (status?: string): string => {
      switch (status) {
        case 'COMPLETED':
          return '完成';
        case 'FAILED':
          return '失败';
        case 'PENDING':
          return '等待中';
        case 'PROCESSING':
          return '处理中';
        default:
          return status || '未知';
      }
    };

    onMounted(() => {
      loadBusinessKnowledge();
    });

    return {
      Plus,
      Search,
      Document,
      businessKnowledgeList,
      dialogVisible,
      isEdit,
      searchKeyword,
      knowledgeForm,
      refreshLoading,
      saveLoading,
      retryLoadingMap,
      openCreateDialog,
      editKnowledge,
      deleteKnowledge,
      toggleRecall,
      saveKnowledge,
      handleSearch,
      refreshVectorStore,
      retryEmbedding,
      getVectorStatusType,
      getStatusText,
    };
  },
});
</script>

<style scoped>
.business-knowledge-config {
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
  margin: 0 0 0.25rem 0;
  letter-spacing: -0.01em;
}

.section-desc {
  color: #64748b;
  margin: 0;
  font-size: 0.85rem;
}

.action-bar {
  padding: 1.25rem 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f1f5f9;
}

.subsection-title {
  font-size: 1rem;
  font-weight: 600;
  color: #334155;
  margin: 0;
}

.right-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.table-container {
  padding: 0;
}

/* Custom Table Styles */
:deep(.el-table) {
  --el-table-header-bg-color: #f8fafc;
  --el-table-border-color: #f1f5f9;
  --el-table-row-hover-bg-color: #f8fafc;
}

:deep(.el-table th.el-table__cell) {
  background-color: #f8fafc;
  color: #475569;
  font-weight: 600;
  height: 48px;
}

:deep(.el-table .cell) {
  padding: 0 16px;
}

.business-term {
  font-weight: 500;
  color: #334155;
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
}

.synonyms-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.operation-buttons {
  display: flex;
  gap: 8px;
}

.form-hint {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

.mr-1 {
  margin-right: 4px;
}

.ml-1 {
  margin-left: 4px;
}

.flex {
  display: flex;
}

.items-center {
  align-items: center;
}

.cursor-pointer {
  cursor: pointer;
}

.text-gray-500 {
  color: #64748b;
}

.text-xs {
  font-size: 12px;
}
</style>
