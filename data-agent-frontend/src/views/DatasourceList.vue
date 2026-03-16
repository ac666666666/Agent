<template>
  <BaseLayout>
    <div class="datasource-list-page">
      <div class="page-header">
        <div class="header-content">
          <div>
            <h1 class="page-title">数据源管理</h1>
            <p class="page-subtitle">连接和管理外部数据库，支持 MySQL、PostgreSQL 等多种类型</p>
          </div>
          <div class="header-actions">
            <el-button type="primary" class="create-btn" @click="openCreateDialog">
              <el-icon class="mr-1"><Plus /></el-icon>
              添加数据源
            </el-button>
          </div>
        </div>
      </div>

      <!-- 搜索和筛选 -->
      <el-card shadow="never" class="filter-card">
        <el-form :inline="true" :model="searchForm" class="filter-form">
          <el-form-item label="类型">
            <el-select
              v-model="searchForm.type"
              placeholder="数据库类型"
              style="width: 150px"
              clearable
              @change="handleSearch"
            >
              <el-option label="全部" value="" />
              <el-option label="MySQL" value="mysql" />
              <el-option label="PostgreSQL" value="postgresql" />
              <el-option label="H2" value="h2" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="searchForm.status"
              placeholder="连接状态"
              style="width: 150px"
              clearable
              @change="handleSearch"
            >
              <el-option label="全部" value="" />
              <el-option label="活跃" value="active" />
              <el-option label="无效" value="inactive" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 数据源列表 -->
      <div class="datasource-grid" v-loading="loading">
        <el-empty v-if="!datasourceList.length && !loading" description="暂无数据源" />

        <div v-else class="grid-container">
          <el-card
            v-for="item in datasourceList"
            :key="item.id"
            shadow="hover"
            class="datasource-card"
          >
            <div class="card-body">
              <div class="datasource-icon">
                <i class="bi bi-database"></i>
              </div>
              <div class="datasource-info">
                <div class="info-header">
                  <h3 class="datasource-name" :title="item.name">{{ item.name }}</h3>
                  <el-tag size="small" :type="item.status === 'active' ? 'success' : 'info'">
                    {{ item.status === 'active' ? '活跃' : '无效' }}
                  </el-tag>
                </div>
                <div class="info-details">
                  <div class="detail-item">
                    <span class="label">类型:</span>
                    <span class="value">{{ item.type }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">主机:</span>
                    <span class="value" :title="item.host">{{ item.host || '-' }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">数据库:</span>
                    <span class="value" :title="item.databaseName">{{ item.databaseName }}</span>
                  </div>
                </div>
                <div class="datasource-desc" v-if="item.description">
                  {{ item.description }}
                </div>
              </div>
            </div>
            <div class="card-footer">
              <div class="footer-time">更新于 {{ formatDate(item.updateTime) }}</div>
              <div class="card-actions">
                <el-button type="primary" link size="small" @click="handleTestConnection(item)">
                  测试连接
                </el-button>
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
      </div>

      <!-- 创建/编辑对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑数据源' : '添加数据源'"
        width="600px"
        destroy-on-close
      >
        <el-form :model="datasourceForm" label-width="100px" :rules="rules" ref="formRef">
          <el-form-item label="名称" prop="name">
            <el-input v-model="datasourceForm.name" placeholder="请输入数据源名称" />
          </el-form-item>
          <el-form-item label="类型" prop="type">
            <el-select
              v-model="datasourceForm.type"
              placeholder="请选择数据库类型"
              style="width: 100%"
            >
              <el-option label="MySQL" value="mysql" />
              <el-option label="PostgreSQL" value="postgresql" />
              <el-option label="H2" value="h2" />
            </el-select>
          </el-form-item>

          <template v-if="datasourceForm.type !== 'h2'">
            <el-row :gutter="20">
              <el-col :span="14">
                <el-form-item label="主机" prop="host">
                  <el-input v-model="datasourceForm.host" placeholder="localhost" />
                </el-form-item>
              </el-col>
              <el-col :span="10">
                <el-form-item label="端口" prop="port" label-width="60px">
                  <el-input-number
                    v-model="datasourceForm.port"
                    :min="1"
                    :max="65535"
                    style="width: 100%"
                    controls-position="right"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="数据库名" prop="databaseName">
              <el-input v-model="datasourceForm.databaseName" placeholder="请输入数据库名称" />
            </el-form-item>
            <el-form-item label="用户名" prop="username">
              <el-input v-model="datasourceForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="datasourceForm.password"
                type="password"
                show-password
                placeholder="请输入密码"
              />
            </el-form-item>
          </template>

          <el-form-item label="描述">
            <el-input
              v-model="datasourceForm.description"
              type="textarea"
              :rows="3"
              placeholder="可选：请输入描述信息"
            />
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
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, MoreFilled } from '@element-plus/icons-vue';
import BaseLayout from '@/layouts/BaseLayout.vue';
import DatasourceService, { Datasource } from '@/services/datasource';

const loading = ref(false);
const submitting = ref(false);
const dialogVisible = ref(false);
const isEdit = ref(false);
const formRef = ref();

const datasourceList = ref<Datasource[]>([]);

const searchForm = reactive({
  type: '',
  status: '',
});

const datasourceForm = reactive<Partial<Datasource>>({
  name: '',
  type: 'mysql',
  host: 'localhost',
  port: 3306,
  databaseName: '',
  username: '',
  password: '',
  description: '',
});

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  host: [{ required: true, message: '请输入主机地址', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
  databaseName: [{ required: true, message: '请输入数据库名', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const handleSearch = async () => {
  loading.value = true;
  try {
    const result = await DatasourceService.getAllDatasource(
      searchForm.status || undefined,
      searchForm.type || undefined,
    );
    datasourceList.value = result;
  } catch (error) {
    console.error('Failed to fetch datasources:', error);
    ElMessage.error('获取数据源列表失败');
  } finally {
    loading.value = false;
  }
};

const resetSearch = () => {
  searchForm.type = '';
  searchForm.status = '';
  handleSearch();
};

const openCreateDialog = () => {
  isEdit.value = false;
  Object.assign(datasourceForm, {
    name: '',
    type: 'mysql',
    host: 'localhost',
    port: 3306,
    databaseName: '',
    username: '',
    password: '',
    description: '',
    id: undefined,
  });
  dialogVisible.value = true;
};

const submitForm = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitting.value = true;
      try {
        if (isEdit.value && datasourceForm.id) {
          await DatasourceService.updateDatasource(datasourceForm.id, datasourceForm as Datasource);
          ElMessage.success('更新成功');
        } else {
          await DatasourceService.createDatasource(datasourceForm as Datasource);
          ElMessage.success('创建成功');
        }
        dialogVisible.value = false;
        handleSearch();
      } catch (error) {
        console.error('Submit failed:', error);
        ElMessage.error(isEdit.value ? '更新失败' : '创建失败');
      } finally {
        submitting.value = false;
      }
    }
  });
};

const handleTestConnection = async (item: Datasource) => {
  try {
    const result = await DatasourceService.testConnection(item.id!);
    if (result.success && result.data) {
      ElMessage.success('连接成功');
      handleSearch(); // Refresh status
    } else {
      console.error('Test connection result:', result);
      ElMessage.error(result.message || '连接失败，请检查配置或网络');
    }
  } catch (error: any) {
    console.error('Test connection failed:', error);
    const errorMsg = error.response?.data?.message || error.message || '连接测试失败';
    ElMessage.error(errorMsg);
  }
};

const handleCommand = (command: string, item: Datasource) => {
  if (command === 'edit') {
    isEdit.value = true;
    Object.assign(datasourceForm, item);
    // Ensure port is number
    if (typeof datasourceForm.port === 'string') {
      datasourceForm.port = parseInt(datasourceForm.port);
    }
    dialogVisible.value = true;
  } else if (command === 'delete') {
    ElMessageBox.confirm('确定要删除该数据源吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await DatasourceService.deleteDatasource(item.id!);
        ElMessage.success('删除成功');
        handleSearch();
      } catch (error) {
        ElMessage.error('删除失败');
      }
    });
  }
};

onMounted(() => {
  handleSearch();
});
</script>

<style scoped>
.datasource-list-page {
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
  margin-bottom: -18px;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.datasource-card {
  border-radius: 12px;
  border: none;
  background: var(--card-bg-color);
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.05);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.datasource-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 15px -3px rgba(59, 130, 246, 0.1);
}

.card-body {
  padding: 20px;
  display: flex;
}

.datasource-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background-color: #f0f9ff;
  color: #0ea5e9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
  flex-shrink: 0;
}

html.dark .datasource-icon {
  background-color: rgba(14, 165, 233, 0.1);
  color: #38bdf8;
}

.datasource-info {
  flex: 1;
  min-width: 0;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.datasource-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 140px;
}

.info-details {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.detail-item {
  display: flex;
  margin-bottom: 4px;
}

.detail-item .label {
  width: 60px;
  color: var(--text-secondary);
}

.detail-item .value {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--text-primary);
}

.datasource-desc {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  padding: 12px 20px;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-time {
  font-size: 12px;
  color: var(--text-secondary);
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.more-btn {
  color: var(--text-secondary);
  padding: 4px;
}

.more-btn:hover {
  color: #3b82f6;
}

.danger-item {
  color: #ef4444;
}
</style>
