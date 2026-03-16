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
      <h2 class="section-title">访问 API Key</h2>
      <p class="section-desc">为该智能体生成并管理 API Key，用于外部系统访问。</p>
    </div>

    <div class="content-wrapper">
      <div class="sub-section">
        <h3 class="sub-title">
          <el-icon><Key /></el-icon>
          密钥管理
        </h3>

        <div class="key-panel">
          <div class="panel-row">
            <span class="row-label">API Key 状态</span>
            <div class="row-content">
              <el-switch
                v-model="apiKeyEnabled"
                :disabled="!apiKey"
                active-text="已启用"
                inactive-text="已禁用"
                @change="handleToggle"
              />
              <el-tag v-if="!apiKey" type="info" size="small" class="status-tag">未生成</el-tag>
            </div>
          </div>

          <div class="panel-row">
            <span class="row-label">当前 Key</span>
            <div class="row-content key-content">
              <el-input
                v-model="displayKey"
                class="key-input"
                readonly
                placeholder="尚未生成 API Key"
              >
                <template #append>
                  <el-button @click="handleCopy" :disabled="!apiKey || !canCopy" title="复制">
                    <el-icon><CopyDocument /></el-icon>
                  </el-button>
                </template>
              </el-input>

              <div class="key-actions">
                <el-tooltip content="显示/隐藏" placement="top">
                  <el-button @click="toggleMask" :disabled="!apiKey" circle plain>
                    <el-icon>
                      <View v-if="masked" />
                      <Hide v-else />
                    </el-icon>
                  </el-button>
                </el-tooltip>

                <el-button
                  type="primary"
                  @click="handleGenerate"
                  :loading="loading.generate"
                  :icon="Refresh"
                >
                  {{ apiKey ? '重新生成' : '生成 Key' }}
                </el-button>

                <el-button
                  @click="handleReset"
                  :disabled="!apiKey"
                  :loading="loading.reset"
                  :icon="RefreshRight"
                >
                  重置
                </el-button>

                <el-button
                  type="danger"
                  plain
                  @click="handleDelete"
                  :disabled="!apiKey"
                  :loading="loading.delete"
                  :icon="Delete"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>

          <el-alert
            v-if="!canCopy && apiKey"
            type="warning"
            :closable="false"
            show-icon
            class="security-alert"
          >
            <template #title>
              为了您的账户安全，API Key 仅在生成或重置时完整显示一次。请务必立即复制并妥善保管。
            </template>
          </el-alert>
        </div>
      </div>

      <div class="sub-section">
        <h3 class="sub-title">
          <el-icon><Connection /></el-icon>
          调用示例
        </h3>
        <p class="section-desc" style="margin-bottom: 16px">
          使用 `X-API-Key` 请求头调用会话接口。
        </p>

        <div class="code-tabs-wrapper">
          <el-tabs v-model="exampleTab" type="border-card" class="code-tabs">
            <el-tab-pane label="curl" name="curl">
              <div class="code-block">
                <pre><code>{{ curlExample }}</code></pre>
              </div>
            </el-tab-pane>
            <el-tab-pane label="JavaScript" name="js">
              <div class="code-block">
                <pre><code>{{ jsExample }}</code></pre>
              </div>
            </el-tab-pane>
            <el-tab-pane label="Python" name="py">
              <div class="code-block">
                <pre><code>{{ pyExample }}</code></pre>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Key,
  Connection,
  CopyDocument,
  View,
  Hide,
  Refresh,
  RefreshRight,
  Delete,
} from '@element-plus/icons-vue';
import AgentService from '@/services/agent';

export default defineComponent({
  name: 'AgentAccessApi',
  components: {
    Key,
    Connection,
    CopyDocument,
    View,
    Hide,
  },
  props: {
    agentId: {
      type: [Number, String],
      required: false,
      default: null,
    },
  },
  setup(props) {
    const route = useRoute();
    const resolvedAgentId = computed(() => Number(props.agentId ?? route.params.id));

    const apiKey = ref<string | null>(null);
    const apiKeyEnabled = ref<boolean>(false);
    const masked = ref(true);
    const canCopy = ref(false);
    const loading = ref({
      generate: false,
      reset: false,
      delete: false,
      toggle: false,
      fetch: false,
    });
    const exampleTab = ref('curl');

    const maskKey = (key: string) => {
      if (!key) return '';
      if (key.startsWith('****')) return key;
      if (key.length <= 8) return '****';
      return '****' + key.slice(-4);
    };

    const displayKey = computed(() => {
      if (!apiKey.value) return '';
      return masked.value ? maskKey(apiKey.value) : apiKey.value;
    });

    const curlExample = computed(() => {
      const base = window.location.origin;
      const id = resolvedAgentId.value;
      return `# 创建会话
curl -X POST "${base}/api/agent/${id}/sessions" \\
  -H "Content-Type: application/json" \\
  -H "X-API-Key: <your_api_key>" \\
  -d '{"title":"demo"}'

# 发送消息
curl -X POST "${base}/api/sessions/<sessionId>/messages" \\
  -H "Content-Type: application/json" \\
  -H "X-API-Key: <your_api_key>" \\
  -d '{"role":"user","content":"给我一个示例","messageType":"text"}'`;
    });

    const jsExample = computed(() => {
      const base = window.location.origin;
      const id = resolvedAgentId.value;
      return String.raw`const apiKey = '<your_api_key>';
const baseUrl = '${base}/api';
const agentId = ${id};

(async () => {
  // 创建会话
  const sessionRes = await fetch(\`${'${baseUrl}'}/agent/${'${agentId}'}/sessions\`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-API-Key': apiKey,
    },
    body: JSON.stringify({ title: 'demo' }),
  });
  const session = await sessionRes.json();

  // 发送消息
  await fetch(\`${'${baseUrl}'}/sessions/${'${session.id}'}/messages\`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-API-Key': apiKey,
    },
    body: JSON.stringify({ role: 'user', content: '你好', messageType: 'text' }),
  });
})();`;
    });

    const pyExample = computed(() => {
      const base = window.location.origin;
      const id = resolvedAgentId.value;
      return `import requests

api_key = '<your_api_key>'
base_url = '${base}/api'

headers = {
    'Content-Type': 'application/json',
    'X-API-Key': api_key,
}

# 创建会话
session_resp = requests.post(
    f"{base_url}/agent/${id}/sessions",
    headers=headers,
    json={"title": "demo"},
)
session_id = session_resp.json().get("id")

# 发送消息
requests.post(
    f"{base_url}/sessions/{session_id}/messages",
    headers=headers,
    json={"role": "user", "content": "你好", "messageType": "text"},
)
`;
    });

    const loadApiKey = async () => {
      loading.value.fetch = true;
      try {
        const res = await AgentService.getApiKey(resolvedAgentId.value);
        apiKey.value = res?.apiKey ?? null;
        apiKeyEnabled.value = Boolean(res?.apiKeyEnabled);
        masked.value = true;
        canCopy.value = false;
      } catch (e) {
        ElMessage.error('获取 API Key 失败');
      } finally {
        loading.value.fetch = false;
      }
    };

    const handleGenerate = async () => {
      loading.value.generate = true;
      try {
        const res = await AgentService.generateApiKey(resolvedAgentId.value);
        apiKey.value = res.apiKey;
        apiKeyEnabled.value = Boolean(res.apiKeyEnabled);
        masked.value = false;
        canCopy.value = true;
        ElMessage.success('已生成 API Key');
      } catch (e) {
        ElMessage.error('生成失败');
      } finally {
        loading.value.generate = false;
      }
    };

    const handleReset = async () => {
      if (!apiKey.value) {
        await handleGenerate();
        return;
      }
      loading.value.reset = true;
      try {
        const res = await AgentService.resetApiKey(resolvedAgentId.value);
        apiKey.value = res.apiKey;
        apiKeyEnabled.value = Boolean(res.apiKeyEnabled);
        masked.value = false;
        canCopy.value = true;
        ElMessage.success('已重置 API Key');
      } catch (e) {
        ElMessage.error('重置失败');
      } finally {
        loading.value.reset = false;
      }
    };

    const handleDelete = async () => {
      if (!apiKey.value) return;
      try {
        await ElMessageBox.confirm('确认删除当前 API Key？删除后需重新生成。', '提示', {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'warning',
        });
      } catch (e) {
        return;
      }

      loading.value.delete = true;
      try {
        const res = await AgentService.deleteApiKey(resolvedAgentId.value);
        apiKey.value = res.apiKey;
        apiKeyEnabled.value = Boolean(res.apiKeyEnabled);
        masked.value = true;
        canCopy.value = false;
        ElMessage.success('已删除 API Key');
      } catch (e) {
        ElMessage.error('删除失败');
      } finally {
        loading.value.delete = false;
      }
    };

    const handleCopy = async () => {
      if (!canCopy.value || !apiKey.value) {
        ElMessage.info('请重新生成或重置后复制完整 Key');
        return;
      }
      try {
        await navigator.clipboard.writeText(apiKey.value);
        ElMessage.success('已复制到剪贴板');
      } catch (e) {
        ElMessage.error('复制失败');
      }
    };

    const toggleMask = () => {
      if (!apiKey.value) return;
      masked.value = !masked.value;
    };

    const handleToggle = async (val: boolean) => {
      loading.value.toggle = true;
      try {
        const res = await AgentService.toggleApiKey(resolvedAgentId.value, val);
        apiKeyEnabled.value = Boolean(res.apiKeyEnabled);
        // 返回值可能是掩码
        apiKey.value = res.apiKey;
        masked.value = true;
        canCopy.value = false;
        ElMessage.success(val ? '已启用 API Key' : '已禁用 API Key');
      } catch (e) {
        apiKeyEnabled.value = !val;
        ElMessage.error('切换失败');
      } finally {
        loading.value.toggle = false;
      }
    };

    onMounted(() => {
      loadApiKey();
    });

    return {
      apiKey,
      apiKeyEnabled,
      masked,
      canCopy,
      loading,
      exampleTab,
      displayKey,
      curlExample,
      jsExample,
      pyExample,
      handleGenerate,
      handleReset,
      handleDelete,
      handleCopy,
      handleToggle,
      toggleMask,
      Refresh,
      RefreshRight,
      Delete,
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

.content-wrapper {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.sub-section {
  margin-bottom: 32px;
}

.sub-title {
  font-size: 16px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.sub-title .el-icon {
  color: #6366f1;
}

.key-panel {
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  padding: 24px;
}

.panel-row {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.row-label {
  width: 120px;
  font-weight: 500;
  color: #475569;
  flex-shrink: 0;
}

.row-content {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.key-content {
  flex-wrap: wrap;
  gap: 16px;
}

.status-tag {
  margin-left: 8px;
}

.key-input {
  max-width: 400px;
}

.key-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.security-alert {
  margin-top: 20px;
}

/* Code Tabs Styling */
.code-tabs-wrapper {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.code-tabs :deep(.el-tabs__header) {
  background-color: #f1f5f9;
  border-bottom: 1px solid #e2e8f0;
}

.code-tabs :deep(.el-tabs__item.is-active) {
  background-color: #ffffff;
  border-bottom-color: #ffffff;
  font-weight: 600;
}

.code-block {
  background-color: #0f172a;
  border-radius: 4px;
  padding: 16px;
  overflow-x: auto;
  position: relative;
}

.code-block pre {
  margin: 0;
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #e2e8f0;
}
</style>
