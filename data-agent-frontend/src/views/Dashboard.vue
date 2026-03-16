<template>
  <BaseLayout>
    <div class="dashboard-container">
      <div class="page-header">
        <h1 class="page-title">仪表盘</h1>
        <p class="page-subtitle">查看系统概览和统计信息</p>
      </div>

      <!-- 模型配置提示卡片 -->
      <el-row v-if="!modelReady && !modelCheckLoading" :gutter="20" class="mb-6">
        <el-col :span="24">
          <el-alert
            title="模型配置提醒"
            type="warning"
            :description="modelConfigMessage"
            show-icon
            :closable="false"
          >
            <template #default>
              <div class="model-config-alert">
                <div class="alert-content">
                  <div class="alert-text">
                    <strong>模型配置提醒</strong>
                    <p>{{ modelConfigMessage }}</p>
                  </div>
                  <el-button type="primary" @click="router.push('/model-config')">
                    立即配置
                  </el-button>
                </div>
              </div>
            </template>
          </el-alert>
        </el-col>
      </el-row>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="mt-6">
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon icon-blue">
                <i class="bi bi-robot"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ agentCount }}</div>
                <div class="stat-label">智能体总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon icon-green">
                <i class="bi bi-database"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ datasourceCount }}</div>
                <div class="stat-label">数据源总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon icon-purple">
                <i class="bi bi-cpu"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ publishedAgentCount }}</div>
                <div class="stat-label">已发布智能体</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 快捷入口 -->
      <el-row :gutter="20" class="mt-6">
        <el-col :span="24">
          <el-card shadow="never" class="quick-actions-card">
            <template #header>
              <div class="card-header">
                <span class="font-medium">快捷操作</span>
              </div>
            </template>
            <div class="quick-actions-container">
              <div class="quick-action-item" @click="router.push('/agent/create')">
                <div class="action-icon icon-blue">
                  <i class="bi bi-plus-lg"></i>
                </div>
                <span class="action-label">创建智能体</span>
              </div>
              <div class="quick-action-item" @click="router.push('/datasources')">
                <div class="action-icon icon-green">
                  <i class="bi bi-database-add"></i>
                </div>
                <span class="action-label">连接数据源</span>
              </div>
              <div class="quick-action-item" @click="router.push('/model-config')">
                <div class="action-icon icon-purple">
                  <i class="bi bi-sliders"></i>
                </div>
                <span class="action-label">模型配置</span>
              </div>
              <div class="quick-action-item" @click="router.push('/agents')">
                <div class="action-icon icon-orange">
                  <i class="bi bi-robot"></i>
                </div>
                <span class="action-label">我的智能体</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 数据可视化图表 -->
      <el-row :gutter="20" class="mt-6">
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <div class="card-header">
                <span class="font-medium">智能体发布状态分布</span>
              </div>
            </template>
            <div ref="agentStatusChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <div class="card-header">
                <span class="font-medium">数据源类型分布</span>
              </div>
            </template>
            <div ref="datasourceTypeChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </BaseLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, nextTick, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import BaseLayout from '@/layouts/BaseLayout.vue';
import AgentService, { Agent } from '@/services/agent';
import DatasourceService, { Datasource } from '@/services/datasource';
import ModelConfigService from '@/services/modelConfig';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';

const router = useRouter();
const agents = ref<Agent[]>([]);
const datasources = ref<Datasource[]>([]);
const loading = ref(false);
const modelReady = ref(true);
const modelCheckLoading = ref(true);
const modelConfigMessage = ref('');

const agentStatusChartRef = ref<HTMLElement | null>(null);
const datasourceTypeChartRef = ref<HTMLElement | null>(null);
let agentStatusChart: echarts.ECharts | null = null;
let datasourceTypeChart: echarts.ECharts | null = null;

const agentCount = computed(() => agents.value.length);
const datasourceCount = computed(() => datasources.value.length);
const publishedAgentCount = computed(
  () => agents.value.filter(a => a.status === 'published').length,
);

const checkModelConfig = async () => {
  try {
    modelCheckLoading.value = true;
    const result = await ModelConfigService.checkReady();
    
    if (!result.ready) {
      modelReady.value = false;
      const missingModels = [];
      if (!result.chatModelReady) {
        missingModels.push('聊天模型');
      }
      if (!result.embeddingModelReady) {
        missingModels.push('嵌入模型');
      }
      modelConfigMessage.value = `检测到您尚未配置${missingModels.join('和')}，请先配置 OpenAI/阿里/Ollama 等模型参数以激活系统功能。`;
    } else {
      modelReady.value = true;
    }
  } catch (error) {
    console.error('检查模型配置失败:', error);
    // 如果检查失败，不显示提示，让用户正常使用
    modelReady.value = true;
  } finally {
    modelCheckLoading.value = false;
  }
};

const initCharts = () => {
  const isDark = document.documentElement.classList.contains('dark');
  const chartTheme = isDark ? 'dark' : undefined;

  if (agentStatusChartRef.value) {
    agentStatusChart = echarts.init(agentStatusChartRef.value, chartTheme);
    updateAgentStatusChart();
  }
  if (datasourceTypeChartRef.value) {
    datasourceTypeChart = echarts.init(datasourceTypeChartRef.value, chartTheme);
    updateDatasourceTypeChart();
  }
};

const updateAgentStatusChart = () => {
  if (!agentStatusChart) return;

  const statusCounts: Record<string, number> = {};
  agents.value.forEach(agent => {
    const status = agent.status || 'unknown';
    statusCounts[status] = (statusCounts[status] || 0) + 1;
  });

  const data = Object.keys(statusCounts).map(status => ({
    name: status === 'published' ? '已发布' : status === 'draft' ? '草稿' : status,
    value: statusCounts[status],
  }));

  const option = {
    color: ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899', '#6366f1'],
    tooltip: {
      trigger: 'item',
    },
    legend: {
      top: '5%',
      left: 'center',
    },
    series: [
      {
        name: '智能体状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: false,
          position: 'center',
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold',
          },
        },
        labelLine: {
          show: false,
        },
        data: data,
      },
    ],
  };

  agentStatusChart.setOption(option);
};

const updateDatasourceTypeChart = () => {
  if (!datasourceTypeChart) return;

  const typeCounts: Record<string, number> = {};
  datasources.value.forEach(ds => {
    const type = ds.type || 'unknown';
    typeCounts[type] = (typeCounts[type] || 0) + 1;
  });

  const data = Object.keys(typeCounts).map(type => ({
    name: type,
    value: typeCounts[type],
  }));

  const option = {
    color: ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899', '#6366f1'],
    tooltip: {
      trigger: 'item',
    },
    legend: {
      top: '5%',
      left: 'center',
    },
    series: [
      {
        name: '数据源类型',
        type: 'pie',
        radius: '50%',
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
      },
    ],
  };

  datasourceTypeChart.setOption(option);
};

const fetchData = async () => {
  loading.value = true;
  try {
    const [agentsData, datasourcesData] = await Promise.all([
      AgentService.list(),
      DatasourceService.getAllDatasource(),
    ]);
    agents.value = agentsData;
    datasources.value = datasourcesData;

    nextTick(() => {
      if (!agentStatusChart || !datasourceTypeChart) {
        initCharts();
      } else {
        updateAgentStatusChart();
        updateDatasourceTypeChart();
      }
    });
  } catch (error) {
    console.error('Failed to fetch dashboard data:', error);
    ElMessage.error('获取仪表盘数据失败');
  } finally {
    loading.value = false;
  }
};

const handleResize = () => {
  agentStatusChart?.resize();
  datasourceTypeChart?.resize();
};

onMounted(() => {
  // 并行执行数据获取和模型配置检查
  Promise.all([
    fetchData(),
    checkModelConfig()
  ]);
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  agentStatusChart?.dispose();
  datasourceTypeChart?.dispose();
});
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
  background: var(--bg-color);
  min-height: calc(100vh - 64px); /* Subtract header height if needed, or let layout handle it */
  border-radius: 8px;
}

.page-header {
  margin-bottom: 32px;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 16px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  background: linear-gradient(90deg, var(--text-primary), #3b82f6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
}

.page-subtitle {
  color: var(--text-secondary);
  margin-top: 8px;
  font-size: 15px;
}

.stat-card {
  height: 100%;
  transition: all 0.3s ease;
  border: none;
  background: var(--card-bg-color);
  box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.05), 0 2px 4px -1px rgba(59, 130, 246, 0.03);
  border-radius: 16px;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 15px -3px rgba(59, 130, 246, 0.1), 0 4px 6px -2px rgba(59, 130, 246, 0.05);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}

.icon-blue {
  background-color: #dbeafe;
  color: #2563eb;
}

html.dark .icon-blue {
  background-color: rgba(37, 99, 235, 0.2);
  color: #60a5fa;
}

.icon-green {
  background-color: #dcfce7;
  color: #16a34a;
}

html.dark .icon-green {
  background-color: rgba(22, 163, 74, 0.2);
  color: #4ade80;
}

.icon-purple {
  background-color: #f3e8ff;
  color: #9333ea;
}

html.dark .icon-purple {
  background-color: rgba(147, 51, 234, 0.2);
  color: #c084fc;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.mt-6 {
  margin-top: 24px;
}

.icon-orange {
  background-color: #ffedd5;
  color: #f97316;
}

html.dark .icon-orange {
  background-color: rgba(249, 115, 22, 0.2);
  color: #fb923c;
}

.quick-actions-card {
  border-radius: 16px;
  background: var(--card-bg-color);
  border: none;
  box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.05);
}

.quick-actions-container {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px;
  border-radius: 12px;
  background-color: var(--bg-color);
  cursor: pointer;
  transition: all 0.2s ease;
  width: 120px;
  border: 1px solid transparent;
}

.quick-action-item:hover {
  transform: translateY(-2px);
  background-color: var(--card-bg-color);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.1);
  border-color: var(--border-color);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-bottom: 8px;
}

.action-label {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.chart-card {
  border-radius: 16px;
  background: var(--card-bg-color);
  border: none;
  box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.05);
}

.chart-container {
  height: 300px;
  width: 100%;
}

.mb-6 {
  margin-bottom: 24px;
}

.model-config-alert {
  width: 100%;
}

.alert-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.alert-text {
  flex: 1;
}

.alert-text strong {
  color: var(--text-primary);
  font-size: 16px;
}

.alert-text p {
  margin: 4px 0 0 0;
  color: var(--text-secondary);
  font-size: 14px;
}
</style>
