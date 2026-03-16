<template>
  <BaseLayout>
    <div class="knowledge-list-page">
      <div class="page-header">
        <div class="header-content">
          <div>
            <h1 class="page-title">知识库管理</h1>
            <p class="page-subtitle">管理智能体的知识库，支持文档、问答对等多种形式</p>
          </div>
          <div class="header-actions">
            <el-button type="primary" class="create-btn" @click="openCreateDialog">
              <el-icon class="mr-1"><Plus /></el-icon>
              添加知识
            </el-button>
          </div>
        </div>
      </div>

      <!-- 搜索和筛选 -->
      <el-card shadow="never" class="filter-card">
        <el-form :inline="true" :model="searchForm" class="filter-form">
          <el-form-item label="所属智能体">
            <el-select
              v-model="searchForm.agentId"
              placeholder="请选择智能体"
              style="width: 200px"
              clearable
              @change="handleSearch"
            >
              <el-option
                v-for="agent in agentList"
                :key="agent.id"
                :label="agent.name"
                :value="agent.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索标题或内容"
              style="width: 200px"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="类型">
            <el-select
              v-model="searchForm.type"
              placeholder="知识类型"
              style="width: 150px"
              clearable
              @change="handleSearch"
            >
              <el-option label="全部" value="" />
              <el-option label="文档" value="document" />
              <el-option label="问答对" value="qa" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 知识列表 -->
      <div class="knowledge-grid" v-loading="loading">
        <el-empty v-if="!knowledgeList.length && !loading" description="暂无知识数据" />

        <div v-else class="grid-container">
          <el-card
            v-for="item in knowledgeList"
            :key="item.id"
            shadow="hover"
            class="knowledge-card"
            @click="viewKnowledge(item)"
          >
            <div class="card-body">
              <div class="knowledge-icon" :class="getIconClass(item.type)">
                <el-icon><component :is="getIconComponent(item.type)" /></el-icon>
              </div>
              <div class="knowledge-info">
                <h3 class="knowledge-title" :title="item.title">{{ item.title || '无标题' }}</h3>
                <div class="knowledge-meta">
                  <el-tag size="small" :type="getTypeTagType(item.type)" effect="plain">
                    {{ getTypeLabel(item.type) }}
                  </el-tag>
                  <span class="update-time">{{ formatDate(item.updatedTime) }}</span>
                </div>
                <p class="knowledge-desc" :title="item.content">
                  {{ truncateContent(item.content) }}
                </p>
              </div>
            </div>
            <div class="card-footer">
              <div class="status-indicator">
                <span
                  class="status-dot"
                  :class="
                    item.embeddingStatus === 'success'
                      ? 'success'
                      : item.embeddingStatus === 'failed'
                      ? 'error'
                      : 'processing'
                  "
                ></span>
                <span class="status-text">{{ getStatusLabel(item.embeddingStatus) }}</span>
              </div>
              <div class="card-actions" @click.stop>
                <el-switch
                  v-model="item.isRecall"
                  size="small"
                  inline-prompt
                  active-text="召回"
                  inactive-text="忽略"
                  @change="val => handleRecallChange(item, val)"
                />
                <el-dropdown trigger="click" @command="cmd => handleCommand(cmd, item)">
                  <el-button type="text" class="more-btn">
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="edit">编辑</el-dropdown-item>
                      <el-dropdown-item command="delete" class="danger-item">删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </el-card>
        </div>

        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :page-sizes="[12, 24, 36, 48]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>

      <!-- 创建/编辑对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑知识' : '添加知识'"
        width="600px"
        destroy-on-close
      >
        <el-form :model="knowledgeForm" label-width="100px" :rules="rules" ref="formRef">
          <el-form-item label="所属智能体" prop="agentId">
            <el-select
              v-model="knowledgeForm.agentId"
              placeholder="请选择智能体"
              style="width: 100%"
              :disabled="isEdit"
            >
              <el-option
                v-for="agent in agentList"
                :key="agent.id"
                :label="agent.name"
                :value="agent.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="知识类型" prop="type">
            <el-radio-group v-model="knowledgeForm.type" :disabled="isEdit">
              <el-radio label="document">文档</el-radio>
              <el-radio label="qa">问答对</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="标题" prop="title">
            <el-input v-model="knowledgeForm.title" placeholder="请输入知识标题" />
          </el-form-item>

          <template v-if="knowledgeForm.type === 'qa'">
            <el-form-item label="问题" prop="question">
              <el-input
                v-model="knowledgeForm.question"
                type="textarea"
                :rows="2"
                placeholder="请输入问题"
              />
            </el-form-item>
            <el-form-item label="答案" prop="content">
              <el-input
                v-model="knowledgeForm.content"
                type="textarea"
                :rows="4"
                placeholder="请输入答案"
              />
            </el-form-item>
          </template>

          <template v-else>
            <el-form-item label="内容" prop="content">
              <el-input
                v-model="knowledgeForm.content"
                type="textarea"
                :rows="6"
                placeholder="请输入文档内容"
              />
            </el-form-item>
          </template>

          <el-form-item label="是否召回">
            <el-switch v-model="knowledgeForm.isRecall" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </BaseLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Plus, Document, ChatLineRound, MoreFilled } from '@element-plus/icons-vue';
import BaseLayout from '@/layouts/BaseLayout.vue';
import AgentService, { Agent } from '@/services/agent';
import AgentKnowledgeService, { AgentKnowledge } from '@/services/agentKnowledge';

const router = useRouter();
const loading = ref(false);
const submitting = ref(false);
const dialogVisible = ref(false);
const isEdit = ref(false);
const formRef = ref();

// 数据
const agentList = ref<Agent[]>([]);
const knowledgeList = ref<AgentKnowledge[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(12);

// 搜索表单
const searchForm = reactive({
  agentId: undefined as number | undefined,
  keyword: '',
  type: '',
});

// 编辑表单
const knowledgeForm = reactive<Partial<AgentKnowledge>>({
  agentId: undefined,
  title: '',
  content: '',
  type: 'document',
  question: '',
  isRecall: true,
});

const rules = {
  agentId: [{ required: true, message: '请选择智能体', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  question: [{ required: true, message: '请输入问题', trigger: 'blur' }],
};

// 获取图标类名
const getIconClass = (type?: string) => {
  return type === 'qa' ? 'icon-qa' : 'icon-doc';
};

// 获取图标组件
const getIconComponent = (type?: string) => {
  return type === 'qa' ? ChatLineRound : Document;
};

// 获取类型标签样式
const getTypeTagType = (type?: string) => {
  return type === 'qa' ? 'success' : 'primary';
};

// 获取类型名称
const getTypeLabel = (type?: string) => {
  const map: Record<string, string> = {
    document: '文档',
    qa: '问答对',
  };
  return map[type || ''] || type || '未知';
};

// 获取状态名称
const getStatusLabel = (status?: string) => {
  const map: Record<string, string> = {
    success: '已索引',
    failed: '失败',
    processing: '处理中',
    pending: '待处理',
  };
  return map[status || ''] || status || '未知';
};

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};

// 截断内容
const truncateContent = (content?: string) => {
  if (!content) return '';
  return content.length > 60 ? content.substring(0, 60) + '...' : content;
};

// 初始化数据
const initData = async () => {
  try {
    const agents = await AgentService.list();
    agentList.value = agents;
    // 如果有智能体，默认选中第一个
    if (agents.length > 0 && !searchForm.agentId) {
      searchForm.agentId = agents[0].id;
    }
    handleSearch();
  } catch (error) {
    console.error('Failed to load agents:', error);
  }
};

// 查询知识列表
const handleSearch = async () => {
  if (!searchForm.agentId) {
    // 如果没有选智能体，清空列表
    knowledgeList.value = [];
    total.value = 0;
    return;
  }

  loading.value = true;
  try {
    const result = await AgentKnowledgeService.queryByPage({
      agentId: searchForm.agentId,
      title: searchForm.keyword,
      type: searchForm.type || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    });

    knowledgeList.value = result.data;
    total.value = result.total;
  } catch (error) {
    console.error('Failed to search knowledge:', error);
    ElMessage.error('获取知识列表失败');
  } finally {
    loading.value = false;
  }
};

const resetSearch = () => {
  searchForm.keyword = '';
  searchForm.type = '';
  handleSearch();
};

const handleSizeChange = (val: number) => {
  pageSize.value = val;
  handleSearch();
};

const handleCurrentChange = (val: number) => {
  pageNum.value = val;
  handleSearch();
};

// 打开创建对话框
const openCreateDialog = () => {
  isEdit.value = false;
  Object.assign(knowledgeForm, {
    agentId: searchForm.agentId, // 默认使用当前选中的智能体
    title: '',
    content: '',
    type: 'document',
    question: '',
    isRecall: true,
    id: undefined,
  });
  dialogVisible.value = true;
};

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitting.value = true;
      try {
        if (isEdit.value && knowledgeForm.id) {
          await AgentKnowledgeService.update(knowledgeForm.id, knowledgeForm);
          ElMessage.success('更新成功');
        } else {
          await AgentKnowledgeService.create(knowledgeForm as AgentKnowledge);
          ElMessage.success('创建成功');
        }
        dialogVisible.value = false;
        handleSearch();
      } catch (error: any) {
        console.error('Submit failed:', error);
        const errorMsg = error.response?.data?.message || (isEdit.value ? '更新失败' : '创建失败');
        ElMessage.error(errorMsg);
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 查看详情
const viewKnowledge = (item: AgentKnowledge) => {
  // 简单的详情查看，复用编辑弹窗但禁用
  handleCommand('edit', item);
};

// 处理召回状态变更
const handleRecallChange = async (item: AgentKnowledge, val: string | number | boolean) => {
  try {
    await AgentKnowledgeService.updateRecallStatus(item.id!, val as boolean);
    ElMessage.success('状态更新成功');
  } catch (error) {
    // 回滚状态
    item.isRecall = !val;
    ElMessage.error('状态更新失败');
  }
};

// 处理下拉菜单命令
const handleCommand = (command: string, item: AgentKnowledge) => {
  if (command === 'edit') {
    isEdit.value = true;
    Object.assign(knowledgeForm, item);
    dialogVisible.value = true;
  } else if (command === 'delete') {
    ElMessageBox.confirm('确定要删除这条知识吗？删除后不可恢复。', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await AgentKnowledgeService.delete(item.id!);
        ElMessage.success('删除成功');
        handleSearch();
      } catch (error) {
        ElMessage.error('删除失败');
      }
    });
  }
};

onMounted(() => {
  initData();
});
</script>

<style scoped>
.knowledge-list-page {
  padding: 24px;
  background: var(--bg-color);
  min-height: calc(100vh - 64px);
  border-radius: 8px;
}

.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.filter-card {
  margin-bottom: 24px;
  border-radius: 12px;
  border: none;
  background: var(--card-bg-color);
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.05);
}

.filter-form {
  margin-bottom: -18px; /* 抵消 el-form-item 的 bottom margin */
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.knowledge-card {
  border-radius: 12px;
  border: none;
  background: var(--card-bg-color);
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.05);
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.knowledge-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 15px -3px rgba(59, 130, 246, 0.1);
}

.card-body {
  padding: 16px;
  display: flex;
  flex: 1;
}

.knowledge-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
  flex-shrink: 0;
}

.icon-doc {
  background-color: #e0f2fe;
  color: #0ea5e9;
}

html.dark .icon-doc {
  background-color: rgba(14, 165, 233, 0.1);
  color: #38bdf8;
}

.icon-qa {
  background-color: #dcfce7;
  color: #10b981;
}

html.dark .icon-qa {
  background-color: rgba(16, 185, 129, 0.1);
  color: #34d399;
}

.knowledge-info {
  flex: 1;
  min-width: 0;
}

.knowledge-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.knowledge-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.update-time {
  font-size: 12px;
  color: var(--text-secondary);
}

.knowledge-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

.card-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-indicator {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: var(--text-secondary);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
  background-color: #cbd5e1;
}

.status-dot.success {
  background-color: #10b981;
}

.status-dot.error {
  background-color: #ef4444;
}

.status-dot.processing {
  background-color: #f59e0b;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.more-btn {
  color: #94a3b8;
  padding: 4px;
}

.more-btn:hover {
  color: #3b82f6;
}

.danger-item {
  color: #ef4444;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}
</style>
