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
  <div class="agent-knowledge-config">
    <div class="config-card">
      <div class="card-header">
        <h2 class="section-title">智能体知识库</h2>
        <p class="section-desc">管理用于增强智能体能力的知识源。</p>
      </div>

      <!-- Toolbar -->
      <div class="action-bar">
        <div class="left-actions">
          <h3 class="subsection-title">知识列表</h3>
        </div>
        <div class="right-actions">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入知识标题搜索"
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
            @click="toggleFilter"
            :type="filterVisible ? 'primary' : ''"
            :icon="FilterIcon"
            plain
          >
            筛选
          </el-button>
          <el-button @click="openCreateDialog" type="primary" :icon="Plus">添加知识</el-button>
        </div>
      </div>

      <!-- 筛选面板 -->
      <el-collapse-transition>
        <div v-show="filterVisible" class="filter-panel">
          <el-form :inline="true" :model="queryParams" class="filter-form">
            <el-form-item label="知识类型">
              <el-select
                v-model="queryParams.type"
                placeholder="全部类型"
                clearable
                @change="handleSearch"
                style="width: 150px"
              >
                <el-option label="文档" value="DOCUMENT" />
                <el-option label="问答对" value="QA" />
                <el-option label="常见问题" value="FAQ" />
              </el-select>
            </el-form-item>
            <el-form-item label="处理状态">
              <el-select
                v-model="queryParams.embeddingStatus"
                placeholder="全部状态"
                clearable
                @change="handleSearch"
                style="width: 150px"
              >
                <el-option label="COMPLETED" value="COMPLETED" />
                <el-option label="PROCESSING" value="PROCESSING" />
                <el-option label="FAILED" value="FAILED" />
                <el-option label="PENDING" value="PENDING" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button @click="clearFilters" :icon="RefreshLeft" link>清空筛选</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-transition>

      <!-- 表格区域 -->
      <div class="table-container">
        <el-table
          :data="knowledgeList"
          style="width: 100%"
          :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600' }"
          v-loading="loading"
        >
          <el-table-column prop="title" label="标题" min-width="200px">
            <template #default="scope">
              <span class="knowledge-title">{{ scope.row.title }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="类型" min-width="100px">
            <template #default="scope">
              <el-tag
                v-if="scope.row.type === 'DOCUMENT'"
                type="primary"
                effect="plain"
                size="small"
              >
                文档
              </el-tag>
              <el-tag
                v-else-if="scope.row.type === 'QA'"
                type="success"
                effect="plain"
                size="small"
              >
                问答对
              </el-tag>
              <el-tag
                v-else-if="scope.row.type === 'FAQ'"
                type="warning"
                effect="plain"
                size="small"
              >
                常见问题
              </el-tag>
              <el-tag v-else type="info" effect="plain" size="small">{{ scope.row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="分块策略" min-width="120px">
            <template #default="scope">
              <el-tag
                v-if="scope.row.splitterType === 'token'"
                type="info"
                size="small"
                effect="light"
              >
                Token
              </el-tag>
              <el-tag
                v-else-if="scope.row.splitterType === 'recursive'"
                type="info"
                size="small"
                effect="light"
              >
                递归
              </el-tag>
              <el-tag
                v-else-if="scope.row.splitterType === 'sentence'"
                type="info"
                size="small"
                effect="light"
              >
                句子
              </el-tag>
              <el-tag
                v-else-if="scope.row.splitterType === 'paragraph'"
                type="info"
                size="small"
                effect="light"
              >
                段落
              </el-tag>
              <el-tag
                v-else-if="scope.row.splitterType === 'semantic'"
                type="info"
                size="small"
                effect="light"
              >
                语义
              </el-tag>
              <span v-else class="text-gray-400 text-xs">-</span>
            </template>
          </el-table-column>
          <el-table-column label="处理状态" min-width="120px">
            <template #default="scope">
              <el-tag
                v-if="scope.row.embeddingStatus === 'COMPLETED'"
                type="success"
                size="small"
                effect="light"
              >
                <el-icon class="mr-1"><Check /></el-icon>
                完成
              </el-tag>
              <el-tag
                v-else-if="scope.row.embeddingStatus === 'PROCESSING'"
                type="primary"
                size="small"
                effect="light"
              >
                <el-icon class="is-loading mr-1"><Loading /></el-icon>
                处理中
              </el-tag>
              <el-tag
                v-else-if="scope.row.embeddingStatus === 'FAILED'"
                type="danger"
                size="small"
                effect="light"
              >
                <el-tooltip v-if="scope.row.errorMsg" :content="scope.row.errorMsg" placement="top">
                  <span class="flex items-center">
                    <el-icon class="mr-1"><Warning /></el-icon>
                    失败
                  </span>
                </el-tooltip>
                <span v-else>失败</span>
              </el-tag>
              <el-tag v-else type="info" size="small" effect="light">
                {{ scope.row.embeddingStatus }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="召回状态" min-width="100px">
            <template #default="scope">
              <el-switch
                :model-value="scope.row.isRecall"
                @change="toggleStatus(scope.row)"
                size="small"
                style="--el-switch-on-color: #10b981"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220px" fixed="right">
            <template #default="scope">
              <div class="operation-buttons">
                <el-button @click="editKnowledge(scope.row)" size="small" type="primary" link>
                  编辑
                </el-button>
                <el-button
                  v-if="scope.row.embeddingStatus === 'FAILED'"
                  @click="handleRetry(scope.row)"
                  size="small"
                  type="warning"
                  link
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

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </div>

    <!-- 添加/编辑知识弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑知识' : '添加新知识'"
      width="700px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        :model="knowledgeForm"
        label-width="100px"
        ref="knowledgeFormRef"
        class="knowledge-form"
      >
        <!-- 知识类型 -->
        <el-form-item label="知识类型" prop="type" required>
          <el-select
            v-model="knowledgeForm.type"
            placeholder="请选择知识类型"
            @change="handleTypeChange"
            :disabled="isEdit"
            style="width: 100%"
          >
            <el-option label="文档 (文件上传)" value="DOCUMENT" />
            <el-option label="问答对 (Q&A)" value="QA" />
            <el-option label="常见问题 (FAQ)" value="FAQ" />
          </el-select>
        </el-form-item>

        <!-- 提示信息 -->
        <div class="form-tip-alert">
          <el-alert v-if="knowledgeForm.type === 'QA'" type="info" :closable="false" show-icon>
            <template #title>
              请录入具体的'分析需求'作为问题，并在答案中写出详细的'思考步骤'与'数据查找逻辑'。
            </template>
          </el-alert>
          <el-alert v-if="knowledgeForm.type === 'FAQ'" type="info" :closable="false" show-icon>
            <template #title>
              请针对特定的'业务术语'、'指标口径'或'常见歧义'进行提问和定义。
            </template>
          </el-alert>
          <el-alert
            v-if="knowledgeForm.type === 'DOCUMENT'"
            type="info"
            :closable="false"
            show-icon
          >
            <template #title>请上传完整的'数据库表结构'、'码表映射字典'或'业务背景说明'。</template>
          </el-alert>
        </div>

        <!-- 知识标题 -->
        <el-form-item label="知识标题" prop="title" required>
          <el-input v-model="knowledgeForm.title" placeholder="为这份知识起一个易于识别的名称" />
        </el-form-item>

        <!-- 分块策略选择 (仅文档类型) -->
        <el-form-item
          v-if="knowledgeForm.type === 'DOCUMENT' && !isEdit"
          label="分块策略"
          prop="splitterType"
        >
          <el-select
            v-model="knowledgeForm.splitterType"
            placeholder="请选择分块策略"
            style="width: 100%"
          >
            <el-option label="Token 分块" value="token" />
            <el-option label="递归分块" value="recursive" />
            <el-option label="句子分块" value="sentence" />
            <el-option label="段落分块" value="paragraph" />
            <el-option label="语义分块" value="semantic" />
          </el-select>
          <div class="splitter-tip">
            <template v-if="knowledgeForm.splitterType === 'token'">
              ⚡ 速度最快，按固定 token 数切分，适合代码和日志
            </template>
            <template v-else-if="knowledgeForm.splitterType === 'recursive'">
              📚 平衡之选，保留文档结构（段落、章节），适合技术文档
            </template>
            <template v-else-if="knowledgeForm.splitterType === 'sentence'">
              ✨ 保证句子完整性，语义不被截断，适合新闻和文章
            </template>
            <template v-else-if="knowledgeForm.splitterType === 'paragraph'">
              📝 按自然段落分块，保留段落完整性，适合博客、书籍等
            </template>
            <template v-else-if="knowledgeForm.splitterType === 'semantic'">
              🧠 基于语义相似度智能分块，自动识别主题边界，适合论文和长文
            </template>
          </div>
        </el-form-item>

        <!-- 文件上传区域 -->
        <el-form-item v-if="knowledgeForm.type === 'DOCUMENT'" label="上传文件" required>
          <div v-if="!isEdit" style="width: 100%">
            <el-upload
              :auto-upload="false"
              :limit="1"
              :on-change="handleFileChange"
              :on-remove="() => (fileList = [])"
              :file-list="fileList"
              drag
              class="upload-area"
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                拖拽文件到此处或
                <em>点击选择文件</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">支持 PDF, DOCX, TXT, MD 等格式</div>
              </template>
            </el-upload>
          </div>
          <div v-else>
            <el-alert
              type="warning"
              :closable="false"
              show-icon
              title="文档类型不支持修改文件内容，如需修改请删除后重新创建"
            />
          </div>
        </el-form-item>

        <!-- Q&A / FAQ 输入区域 -->
        <template v-if="knowledgeForm.type === 'QA' || knowledgeForm.type === 'FAQ'">
          <el-form-item label="问题" prop="question" required>
            <el-input
              v-model="knowledgeForm.question"
              type="textarea"
              :rows="3"
              placeholder="输入用户可能会问的问题..."
            />
          </el-form-item>
          <el-form-item label="答案" prop="answer" required>
            <el-input
              v-model="knowledgeForm.answer"
              type="textarea"
              :rows="6"
              placeholder="输入标准答案..."
            />
          </el-form-item>
        </template>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeDialog">取消</el-button>
          <el-button type="primary" @click="saveKnowledge" :loading="saveLoading">
            {{ isEdit ? '更新' : '添加并处理' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, Ref, reactive } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Plus,
  Search,
  Filter as FilterIcon,
  RefreshLeft,
  UploadFilled,
  Warning,
  Check,
  Loading,
} from '@element-plus/icons-vue';
import axios from 'axios';
import agentKnowledgeService, {
  AgentKnowledge,
  AgentKnowledgeQueryDTO,
} from '@/services/agentKnowledge';

export default defineComponent({
  name: 'AgentKnowledgeConfig',
  components: {
    Search,
    Warning,
    UploadFilled,
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
    const knowledgeList: Ref<AgentKnowledge[]> = ref([]);
    const total: Ref<number> = ref(0);
    const loading: Ref<boolean> = ref(false);
    const dialogVisible: Ref<boolean> = ref(false);
    const isEdit: Ref<boolean> = ref(false);
    const saveLoading: Ref<boolean> = ref(false);
    const currentEditId: Ref<number | null> = ref(null);
    const fileList: Ref<{ name: string; size: number; raw: File }[]> = ref([]);
    const filterVisible: Ref<boolean> = ref(false);

    // 查询参数
    const queryParams = reactive<AgentKnowledgeQueryDTO>({
      agentId: props.agentId,
      title: '',
      type: '',
      embeddingStatus: '',
      pageNum: 1,
      pageSize: 10,
    });

    // 表单数据
    const knowledgeForm: Ref<
      AgentKnowledge & { question?: string; answer?: string; file?: File; splitterType?: string }
    > = ref({
      agentId: props.agentId,
      title: '',
      content: '',
      type: 'DOCUMENT',
      isRecall: true,
      question: '',
      answer: '',
      splitterType: 'recursive', // 默认使用递归分块
    } as AgentKnowledge & { question?: string; answer?: string; splitterType?: string });

    // 切换筛选面板
    const toggleFilter = () => {
      filterVisible.value = !filterVisible.value;
    };

    // 清空筛选条件
    const clearFilters = () => {
      queryParams.type = '';
      queryParams.embeddingStatus = '';
      handleSearch();
    };

    // 加载知识列表
    const loadKnowledgeList = async () => {
      loading.value = true;
      try {
        const queryDTO = {
          ...queryParams,
          type: queryParams.type ? queryParams.type : '',
          embeddingStatus: queryParams.embeddingStatus ? queryParams.embeddingStatus : '',
        };
        const result = await agentKnowledgeService.queryByPage(queryDTO);
        if (result.success) {
          knowledgeList.value = result.data;
          total.value = result.total;
        } else {
          ElMessage.error(result.message || '加载知识列表失败');
        }
      } catch (error) {
        ElMessage.error('加载知识列表失败');
        console.error('Failed to load knowledge list:', error);
      } finally {
        loading.value = false;
      }
    };

    // 搜索
    const handleSearch = () => {
      queryParams.pageNum = 1;
      loadKnowledgeList();
    };

    // 分页处理
    const handleSizeChange = (val: number) => {
      queryParams.pageSize = val;
      loadKnowledgeList();
    };

    const handleCurrentChange = (val: number) => {
      queryParams.pageNum = val;
      loadKnowledgeList();
    };

    // 打开创建对话框
    const openCreateDialog = () => {
      isEdit.value = false;
      dialogVisible.value = true;
      resetForm();
    };

    // 关闭对话框
    const closeDialog = () => {
      dialogVisible.value = false;
      resetForm();
    };

    // 编辑知识
    const editKnowledge = (knowledge: AgentKnowledge) => {
      isEdit.value = true;
      currentEditId.value = knowledge.id || null;
      knowledgeForm.value = {
        ...knowledge,
        type: knowledge.type,
      };

      if (knowledge.type === 'QA' || knowledge.type === 'FAQ') {
        knowledgeForm.value.answer = knowledge.content;
      }

      dialogVisible.value = true;
    };

    // 切换状态（召回/取消召回）
    const toggleStatus = async (knowledge: AgentKnowledge) => {
      if (!knowledge.id) return;
      const newStatus = !knowledge.isRecall;
      // 直接切换，不再弹窗确认，提升操作效率
      try {
        const result = await agentKnowledgeService.updateRecallStatus(knowledge.id!, newStatus);
        if (result) {
          knowledge.isRecall = newStatus;
          ElMessage.success(`${newStatus ? '已开启召回' : '已关闭召回'}`);
        } else {
          ElMessage.error('操作失败');
        }
      } catch (error) {
        ElMessage.error('操作失败');
        console.error(`Failed to toggle recall status:`, error);
      }
    };

    // 重试向量化
    const handleRetry = async (knowledge: AgentKnowledge) => {
      if (!knowledge.id) return;
      try {
        const success = await agentKnowledgeService.retryEmbedding(knowledge.id);
        if (success) {
          ElMessage.success('重试请求已发送');
          loadKnowledgeList();
        } else {
          ElMessage.error('重试失败');
        }
      } catch (error) {
        ElMessage.error('重试失败');
      }
    };

    // 删除知识
    const deleteKnowledge = (knowledge: AgentKnowledge) => {
      if (!knowledge.id) return;

      ElMessageBox.confirm(`确定要删除知识 "${knowledge.title}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(async () => {
          try {
            const result = await agentKnowledgeService.delete(knowledge.id!);
            if (result) {
              ElMessage.success('删除成功');
              await loadKnowledgeList();
            } else {
              ElMessage.error('删除失败');
            }
          } catch (error) {
            ElMessage.error('删除失败');
            console.error('Failed to delete knowledge:', error);
          }
        })
        .catch(() => {});
    };

    // 处理类型变化
    const handleTypeChange = () => {
      knowledgeForm.value.content = '';
      knowledgeForm.value.question = '';
      knowledgeForm.value.answer = '';
      fileList.value = [];
    };

    // 处理文件变化
    const handleFileChange = (file: { name: string; size: number; raw: File }) => {
      fileList.value = [file];
      knowledgeForm.value.file = file.raw;
    };

    // 格式化文件大小
    const formatFileSize = (bytes: number): string => {
      if (!bytes) return '0 B';
      const k = 1024;
      const sizes = ['B', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
    };

    // 保存知识
    const saveKnowledge = async () => {
      // 表单验证
      if (!knowledgeForm.value.title || !knowledgeForm.value.title.trim()) {
        ElMessage.warning('请输入知识标题');
        return;
      }

      if (knowledgeForm.value.type === 'DOCUMENT') {
        if (!isEdit.value && !knowledgeForm.value.file && fileList.value.length === 0) {
          ElMessage.warning('请上传文件');
          return;
        }
      } else if (knowledgeForm.value.type === 'QA' || knowledgeForm.value.type === 'FAQ') {
        if (!knowledgeForm.value.question || !knowledgeForm.value.question.trim()) {
          ElMessage.warning('请输入问题');
          return;
        }
        if (!knowledgeForm.value.answer || !knowledgeForm.value.answer.trim()) {
          ElMessage.warning('请输入答案');
          return;
        }
        knowledgeForm.value.content = knowledgeForm.value.answer;
      }

      saveLoading.value = true;
      try {
        if (isEdit.value && currentEditId.value) {
          const updateData = {
            ...knowledgeForm.value,
            type: knowledgeForm.value.type?.toUpperCase(),
          };
          const result = await agentKnowledgeService.update(currentEditId.value, updateData);
          if (result) {
            ElMessage.success('更新成功');
          } else {
            ElMessage.error('更新失败');
            return;
          }
        } else {
          const formData = new FormData();
          formData.append('agentId', String(knowledgeForm.value.agentId));
          formData.append('title', knowledgeForm.value.title);
          formData.append('type', knowledgeForm.value.type || 'DOCUMENT');
          formData.append('isRecall', knowledgeForm.value.isRecall ? '1' : '0');

          if (knowledgeForm.value.type === 'DOCUMENT' && knowledgeForm.value.file) {
            formData.append('file', knowledgeForm.value.file);
            // 添加分块策略参数
            if (knowledgeForm.value.splitterType) {
              formData.append('splitterType', knowledgeForm.value.splitterType);
            }
          } else {
            if (knowledgeForm.value.content) {
              formData.append('content', knowledgeForm.value.content);
            }
            if (knowledgeForm.value.question) {
              formData.append('question', knowledgeForm.value.question);
            }
          }

          const response = await axios.post('/api/agent-knowledge/create', formData, {
            headers: {
              'Content-Type': 'multipart/form-data',
            },
          });

          if (response.data.success) {
            ElMessage.success('创建成功');
          } else {
            ElMessage.error(response.data.message || '创建失败');
            return;
          }
        }

        dialogVisible.value = false;
        await loadKnowledgeList();
      } catch (error) {
        ElMessage.error(`${isEdit.value ? '更新' : '创建'}失败`);
        console.error('Failed to save knowledge:', error);
      } finally {
        saveLoading.value = false;
      }
    };

    // 重置表单
    const resetForm = () => {
      knowledgeForm.value = {
        agentId: props.agentId,
        title: '',
        content: '',
        type: 'DOCUMENT',
        isRecall: true,
        question: '',
        answer: '',
        splitterType: 'recursive', // 默认使用递归分块
      } as AgentKnowledge & { question?: string; answer?: string; splitterType?: string };
      currentEditId.value = null;
      fileList.value = [];
    };

    onMounted(() => {
      loadKnowledgeList();
    });

    return {
      Plus,
      Search,
      FilterIcon,
      RefreshLeft,
      UploadFilled,
      Warning,
      Check,
      Loading,
      knowledgeList,
      total,
      loading,
      dialogVisible,
      isEdit,
      saveLoading,
      queryParams,
      knowledgeForm,
      fileList,
      filterVisible,
      toggleFilter,
      clearFilters,
      loadKnowledgeList,
      handleSearch,
      handleSizeChange,
      handleCurrentChange,
      openCreateDialog,
      closeDialog,
      editKnowledge,
      deleteKnowledge,
      saveKnowledge,
      resetForm,
      handleTypeChange,
      handleFileChange,
      toggleStatus,
      handleRetry,
      formatFileSize,
    };
  },
});
</script>

<style scoped>
.agent-knowledge-config {
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

.filter-panel {
  padding: 1.25rem 1.5rem;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
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

.knowledge-title {
  font-weight: 500;
  color: #334155;
}

.operation-buttons {
  display: flex;
  gap: 8px;
}

.pagination-container {
  padding: 1rem 1.5rem;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #f1f5f9;
}

/* Dialog Styles */
.form-tip-alert {
  margin-bottom: 16px;
}

.splitter-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #64748b;
  line-height: 1.5;
  background: #f1f5f9;
  padding: 8px 12px;
  border-radius: 6px;
}

.mr-1 {
  margin-right: 4px;
}
</style>
