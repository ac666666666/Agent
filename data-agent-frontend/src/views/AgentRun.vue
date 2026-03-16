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
    <el-container style="height: calc(100vh - 60px); gap: 0">
      <!-- 左侧历史消息栏 -->
      <ChatSessionSidebar
        :agent="agent"
        :handleSetCurrentSession="
          async (session: ChatSession | null) => {
            currentSession = session;
            await selectSession(session);
          }
        "
        :handleGetCurrentSession="
          () => {
            return currentSession;
          }
        "
        :handleSelectSession="selectSession"
        :handleDeleteSessionState="deleteSessionState"
      />

      <!-- 右侧对话栏 -->
      <el-main class="main-chat-area">
        <!-- 消息显示区域 -->
        <div class="chat-container" ref="chatContainer">
          <div v-if="!currentSession" class="empty-state">
            <div class="empty-state-content">
              <el-icon class="empty-icon"><ChatDotRound /></el-icon>
              <h3>开启新对话</h3>
              <p>请选择左侧会话或创建新会话开始</p>
            </div>
            <PresetQuestions
              v-if="agent.id"
              :agentId="agent.id"
              :onQuestionClick="handlePresetQuestionClick"
              class="empty-state-preset"
            />
          </div>
          <div v-else class="messages-area">
            <div
              v-for="message in currentMessages"
              :key="message.id"
              class="message-wrapper"
              :class="message.role"
            >
              <div class="message-row">
                <!-- AI Avatar (Left) -->
                <div v-if="message.role === 'assistant'" class="avatar-wrapper">
                  <el-avatar :size="36" shape="square" :src="agent.avatar" class="ai-avatar">
                    AI
                  </el-avatar>
                </div>

                <!-- Message Content -->
                <div class="message-content-wrapper">
                  <!-- HTML Message -->
                  <div
                    v-if="message.messageType === 'html'"
                    class="message-bubble html-content"
                    v-html="message.content"
                  ></div>

                  <!-- Result Set Message -->
                  <div
                    v-else-if="message.messageType === 'result-set'"
                    class="message-bubble result-set-content"
                  >
                    <ResultSetDisplay
                      v-if="message.content"
                      :resultData="JSON.parse(message.content)"
                      :pageSize="resultSetDisplayConfig.pageSize"
                    />
                  </div>

                  <!-- Markdown Report Message -->
                  <div
                    v-else-if="message.messageType === 'markdown-report'"
                    class="message-bubble report-content"
                  >
                    <div class="report-header">
                      <div class="report-title">
                        <el-icon><Document /></el-icon>
                        <span>分析报告</span>
                      </div>
                      <div class="report-actions">
                        <el-radio-group
                          v-model="requestOptions.reportFormat"
                          size="small"
                          class="format-toggle"
                        >
                          <el-radio-button value="markdown">MD</el-radio-button>
                          <el-radio-button value="html">HTML</el-radio-button>
                        </el-radio-group>
                        <el-dropdown trigger="click">
                          <el-button size="small" circle plain>
                            <el-icon><MoreFilled /></el-icon>
                          </el-button>
                          <template #dropdown>
                            <el-dropdown-menu>
                              <el-dropdown-item
                                @click="downloadMarkdownReportFromMessage(`${message.content}`)"
                              >
                                <el-icon><Download /></el-icon>
                                下载 Markdown
                              </el-dropdown-item>
                              <el-dropdown-item
                                @click="downloadHtmlReportFromMessageByServer(`${message.content}`)"
                              >
                                <el-icon><Download /></el-icon>
                                下载 HTML
                              </el-dropdown-item>
                              <el-dropdown-item @click="openReportFullscreen(message.content)">
                                <el-icon><FullScreen /></el-icon>
                                全屏查看
                              </el-dropdown-item>
                            </el-dropdown-menu>
                          </template>
                        </el-dropdown>
                      </div>
                    </div>
                    <div class="report-body custom-scrollbar">
                      <markdown-agent-container
                        v-if="requestOptions.reportFormat === 'markdown'"
                        class="md-body"
                        :content="message.content"
                        :options="options"
                      />
                      <ReportHtmlView v-else :content="message.content" />
                    </div>
                  </div>

                  <!-- Text Message (Default) -->
                  <div v-else class="message-bubble text-content">
                    <div class="markdown-body" v-html="formatMessageContent(message)"></div>
                  </div>
                </div>

                <!-- User Avatar (Right) -->
                <div v-if="message.role === 'user'" class="avatar-wrapper">
                  <el-avatar :size="36" shape="square" class="user-avatar">
                    <el-icon><UserFilled /></el-icon>
                  </el-avatar>
                </div>
              </div>
            </div>

            <!-- Streaming Response -->
            <div v-if="isStreaming" class="message-wrapper assistant streaming">
              <div class="message-row">
                <div class="avatar-wrapper">
                  <el-avatar :size="36" shape="square" :src="agent.avatar" class="ai-avatar">
                    AI
                  </el-avatar>
                </div>
                <div class="message-content-wrapper">
                  <div class="message-bubble streaming-content">
                    <div class="streaming-indicator">
                      <span class="dot"></span>
                      <span class="dot"></span>
                      <span class="dot"></span>
                    </div>
                    <div class="agent-response-container">
                      <template v-for="(nodeBlock, index) in nodeBlocks" :key="index">
                        <!-- Report Node -->
                        <div
                          v-if="
                            nodeBlock.length > 0 &&
                            nodeBlock[0].nodeName === 'ReportGeneratorNode' &&
                            nodeBlock[0].textType === 'MARK_DOWN'
                          "
                          class="agent-response-block"
                        >
                          <div class="response-block-title">{{ nodeBlock[0].nodeName }}</div>
                          <div class="response-block-body">
                            <markdown-agent-container
                              v-if="requestOptions.reportFormat === 'markdown'"
                              class="md-body"
                              :content="getMarkdownContentFromNode(nodeBlock)"
                              :options="options"
                            />
                            <ReportHtmlView
                              v-else
                              :content="getMarkdownContentFromNode(nodeBlock)"
                            />
                          </div>
                        </div>
                        <!-- Result Set Node -->
                        <div
                          v-else-if="nodeBlock.length > 0 && nodeBlock[0].textType === 'RESULT_SET'"
                          class="agent-response-block"
                        >
                          <div class="response-block-title">{{ nodeBlock[0].nodeName }}</div>
                          <div class="response-block-body">
                            <ResultSetDisplay
                              v-if="nodeBlock[0].text"
                              :resultData="JSON.parse(nodeBlock[0].text)"
                              :pageSize="resultSetDisplayConfig.pageSize"
                            />
                          </div>
                        </div>
                        <!-- Default Node -->
                        <div v-else v-html="generateNodeHtml(nodeBlock)"></div>
                      </template>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Human Feedback -->
        <HumanFeedback
          v-if="showHumanFeedback"
          :request="lastRequest"
          :handleFeedback="handleHumanFeedback"
          class="human-feedback-overlay"
        />

        <!-- Input Area -->
        <div class="input-section" v-if="currentSession">
          <div class="input-card">
            <!-- Toolbar -->
            <div class="input-toolbar">
              <div class="toolbar-left">
                <el-tooltip content="预设问题" placement="top">
                  <el-popover placement="top-start" :width="400" trigger="click">
                    <template #reference>
                      <el-button circle size="small" plain>
                        <el-icon><ChatLineSquare /></el-icon>
                      </el-button>
                    </template>
                    <PresetQuestions
                      v-if="currentSession && agent.id"
                      :agentId="agent.id"
                      :onQuestionClick="handlePresetQuestionClick"
                    />
                  </el-popover>
                </el-tooltip>

                <el-divider direction="vertical" />

                <div class="switch-item">
                  <span class="label">NL2SQL</span>
                  <el-switch
                    v-model="requestOptions.nl2sqlOnly"
                    size="small"
                    :disabled="isStreaming || showHumanFeedback"
                    @change="handleNl2sqlOnlyChange"
                  />
                </div>

                <div class="switch-item">
                  <span class="label">人工反馈</span>
                  <el-switch
                    v-model="requestOptions.humanFeedback"
                    size="small"
                    :disabled="requestOptions.nl2sqlOnly || isStreaming || showHumanFeedback"
                  />
                </div>

                <el-popover placement="top" :width="250" trigger="click">
                  <template #reference>
                    <el-button size="small" text>
                      更多设置
                      <el-icon><ArrowDown /></el-icon>
                    </el-button>
                  </template>
                  <div class="more-settings-popover">
                    <div class="setting-item">
                      <span>自动滚动</span>
                      <el-switch v-model="autoScroll" size="small" />
                    </div>
                    <div class="setting-item">
                      <span>显示SQL结果</span>
                      <el-switch
                        v-model="resultSetDisplayConfig.showSqlResults"
                        size="small"
                        :disabled="isStreaming"
                      />
                    </div>
                    <div class="setting-item">
                      <span>每页数量</span>
                      <el-select
                        v-model="resultSetDisplayConfig.pageSize"
                        size="small"
                        style="width: 80px"
                      >
                        <el-option :value="5" label="5" />
                        <el-option :value="10" label="10" />
                        <el-option :value="20" label="20" />
                      </el-select>
                    </div>
                  </div>
                </el-popover>
              </div>
            </div>

            <!-- Textarea -->
            <div class="input-box">
              <el-input
                v-model="userInput"
                type="textarea"
                :rows="3"
                placeholder="输入您的问题，按 Enter 发送，Shift + Enter 换行..."
                :disabled="isStreaming || showHumanFeedback"
                @keydown.enter.exact.prevent="sendMessage"
                class="custom-textarea"
                resize="none"
              />
              <div class="send-actions">
                <el-button
                  v-if="!isStreaming"
                  type="primary"
                  @click="sendMessage"
                  :disabled="showHumanFeedback || !userInput.trim()"
                  circle
                  class="send-btn"
                >
                  <el-icon><Promotion /></el-icon>
                </el-button>
                <el-button
                  v-else
                  type="danger"
                  @click="stopStreaming"
                  circle
                  class="send-btn stop-btn"
                >
                  <el-icon><CircleClose /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-main>
    </el-container>

    <!-- 报告全屏遮罩 -->
    <Teleport to="body">
      <div
        v-if="showReportFullscreen"
        class="report-fullscreen-overlay"
        @click.self="closeReportFullscreen"
      >
        <div class="report-fullscreen-container">
          <div class="report-fullscreen-header">
            <span class="report-fullscreen-title">
              {{ requestOptions.reportFormat === 'markdown' ? 'Markdown 报告' : 'HTML 报告' }}
            </span>
            <el-button
              type="danger"
              circle
              class="report-fullscreen-close"
              @click="closeReportFullscreen"
            >
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
          <div class="report-fullscreen-content">
            <markdown-agent-container
              v-if="requestOptions.reportFormat === 'markdown'"
              class="md-body report-fullscreen-body"
              :content="fullscreenReportContent"
              :options="options"
            />
            <ReportHtmlView
              v-else
              :content="fullscreenReportContent"
              class="report-fullscreen-body"
            />
          </div>
        </div>
      </div>
    </Teleport>
  </BaseLayout>
</template>

<script lang="ts">
import { ref, defineComponent, onMounted, nextTick, computed } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  Loading,
  Promotion,
  Document,
  Download,
  CircleClose,
  FullScreen,
  Close,
  ArrowDown,
  ChatDotRound,
  UserFilled,
  MoreFilled,
  ChatLineSquare,
} from '@element-plus/icons-vue';
import hljs from 'highlight.js';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
import 'highlight.js/styles/github.css';
// 导入并注册语言
import sql from 'highlight.js/lib/languages/sql';
import python from 'highlight.js/lib/languages/python';
import json from 'highlight.js/lib/languages/json';

// 注册语言
hljs.registerLanguage('sql', sql);
hljs.registerLanguage('python', python);
hljs.registerLanguage('json', json);
import BaseLayout from '@/layouts/BaseLayout.vue';
import AgentService from '@/services/agent';
import ChatService, { type ChatSession, type ChatMessage } from '@/services/chat';
import GraphService, {
  type GraphRequest,
  type GraphNodeResponse,
  TextType,
} from '@/services/graph';
import { type Agent } from '@/services/agent';
import {
  type ResultData,
  type ResultSetData,
  type ResultSetDisplayConfig,
} from '@/services/resultSet';
import { SessionRuntimeState, useSessionStateManager } from '@/services/sessionStateManager';
import HumanFeedback from '@/components/run/HumanFeedback.vue';
import ChatSessionSidebar from '@/components/run/ChatSessionSidebar.vue';
import PresetQuestions from '@/components/run/PresetQuestions.vue';
import MarkdownAgentContainer from '@/components/run/markdown';
import ReportHtmlView from '@/components/run/ReportHtmlView.vue';
import ResultSetDisplay from '@/components/run/ResultSetDisplay.vue';

// 扩展Window接口以包含自定义方法
declare global {
  interface Window {
    copyTextToClipboard: (btn: HTMLElement) => void;
    handleResultSetPagination: (btn: HTMLElement, direction: 'prev' | 'next') => void;
  }
}

export default defineComponent({
  name: 'AgentRun',
  components: {
    BaseLayout,
    Loading,
    Promotion,
    Document,
    Download,
    CircleClose,
    FullScreen,
    Close,
    ArrowDown,
    ChatDotRound,
    UserFilled,
    MoreFilled,
    ChatLineSquare,
    HumanFeedback,
    ChatSessionSidebar,
    PresetQuestions,
    MarkdownAgentContainer,
    ReportHtmlView,
    ResultSetDisplay,
  },
  created() {
    window.copyTextToClipboard = btn => {
      const text = btn.previousElementSibling.textContent;
      const originalText = btn.textContent;

      navigator.clipboard
        .writeText(text)
        .then(() => {
          btn.textContent = '已复制!';
          setTimeout(() => {
            btn.textContent = originalText;
          }, 3000);
        })
        .catch(() => {
          btn.textContent = '复制失败';
          setTimeout(() => {
            btn.textContent = originalText;
          }, 3000);
        });
    };

    // 结果集翻页事件处理
    window.handleResultSetPagination = (btn: HTMLElement, direction: 'prev' | 'next') => {
      const container = btn.closest('.result-set-container');
      if (!container) return;

      const currentPageElement = container.querySelector('.result-set-current-page');
      const prevBtn = container.querySelector('.result-set-pagination-prev') as HTMLButtonElement;
      const nextBtn = container.querySelector('.result-set-pagination-next') as HTMLButtonElement;
      const pages = container.querySelectorAll('.result-set-page');

      if (!currentPageElement || !prevBtn || !nextBtn || pages.length === 0) return;

      let currentPage = parseInt(currentPageElement.textContent || '1');
      const totalPages = pages.length;

      if (direction === 'prev' && currentPage > 1) {
        currentPage--;
      } else if (direction === 'next' && currentPage < totalPages) {
        currentPage++;
      }

      // 更新页面显示
      pages.forEach((page: Element) => {
        page.classList.remove('result-set-page-active');
      });
      const targetPage = container.querySelector(`.result-set-page[data-page="${currentPage}"]`);
      if (targetPage) {
        targetPage.classList.add('result-set-page-active');
      }

      // 更新页码显示
      currentPageElement.textContent = currentPage.toString();

      // 更新按钮状态
      prevBtn.disabled = currentPage === 1;
      nextBtn.disabled = currentPage === totalPages;
    };
  },
  setup() {
    const route = useRoute();

    // 响应式数据
    const agent = ref<Agent>({} as Agent);
    const currentSession = ref<ChatSession | null>(null);
    const currentMessages = ref<ChatMessage[]>([]);
    const userInput = ref('');
    const { getSessionState, syncStateToView, saveViewToState, deleteSessionState } =
      useSessionStateManager();
    const isStreaming = ref(false);
    const nodeBlocks = ref<GraphNodeResponse[][]>([]);
    const options = ref({
      markdownIt: {
        linkify: true,
      },
      linkAttributes: {
        attrs: {
          target: '_blank',
          rel: 'noopener',
        },
      },
    });
    const requestOptions = ref({
      humanFeedback: false,
      nl2sqlOnly: false,
      reportFormat: 'markdown' as 'markdown' | 'html', // 'markdown' | 'html'，控制报告展示方式
    });
    const showReportFullscreen = ref(false);
    const fullscreenReportContent = ref('');
    const inputControlsCollapsed = ref(false);

    // 监听NL2SQL开关变化
    const handleNl2sqlOnlyChange = (value: boolean) => {
      if (value) {
        // 当仅NL2SQL开启时，禁用人工反馈，并设为false
        requestOptions.value.humanFeedback = false;
      }
    };
    const autoScroll = ref(true);
    const chatContainer = ref<HTMLElement | null>(null);

    // 人工反馈相关数据
    const showHumanFeedback = ref(false);
    const lastRequest = ref<GraphRequest | null>(null);

    // 结果集显示配置
    const resultSetDisplayConfig = ref<ResultSetDisplayConfig>({
      showSqlResults: false,
      pageSize: 20,
    });

    const agentId = computed(() => route.params.id as string);

    const loadAgent = async () => {
      try {
        const agentData = await AgentService.get(parseInt(agentId.value));
        if (agentData) {
          agent.value = agentData;
        } else {
          throw new Error('Agent 不存在');
        }
      } catch (error) {
        ElMessage.error('加载Agent失败');
        console.error('加载Agent失败:', error);
      }
    };

    const selectSession = async (session: ChatSession | null) => {
      // 将源会话状态保存，然后切换到目标会话
      if (currentSession.value) {
        saveViewToState(currentSession.value.id, { isStreaming, nodeBlocks });
      }
      currentSession.value = session;

      try {
        if (session === null) {
          currentMessages.value = [];
          nodeBlocks.value = [];
          isStreaming.value = false;
          return;
        }
        syncStateToView(session.id, { isStreaming, nodeBlocks });
        currentMessages.value = await ChatService.getSessionMessages(session.id);
        scrollToBottom();
      } catch (error) {
        ElMessage.error('加载消息失败');
        console.error('加载消息失败:', error);
      }
    };

    const sendMessage = async () => {
      if (!userInput.value.trim()) {
        ElMessage.warning('请输入请求消息！');
        return;
      }
      if (!currentSession.value || isStreaming.value) {
        ElMessage.warning('智能体正在处理中，请稍后...');
        return;
      }

      const needsTitle = !currentSession.value?.title || currentSession.value.title === '新会话';

      const userMessage: ChatMessage = {
        sessionId: currentSession.value.id,
        role: 'user',
        content: userInput.value,
        messageType: 'text',
        titleNeeded: needsTitle,
      };
      try {
        // 保存用户消息
        const savedMessage = await ChatService.saveMessage(currentSession.value.id, userMessage);
        currentMessages.value.push(savedMessage);
        const sessionState = getSessionState(currentSession.value.id);

        const request: GraphRequest = {
          agentId: agentId.value,
          query: userInput.value,
          humanFeedback: requestOptions.value.humanFeedback,
          nl2sqlOnly: requestOptions.value.nl2sqlOnly,
          rejectedPlan: false,
          humanFeedbackContent: null,
          threadId: sessionState.lastRequest?.threadId || null,
        };

        userInput.value = '';

        await sendGraphRequest(request, true);
      } catch (error) {
        ElMessage.error('未知错误');
        console.error(error);
      }
    };

    const sendGraphRequest = async (request: GraphRequest, rejectedPlan: boolean) => {
      const sessionId = currentSession.value!.id;
      const sessionTitle = currentSession.value!.title;
      const sessionState = getSessionState(sessionId);
      try {
        lastRequest.value = request;
        // 准备流式请求
        isStreaming.value = true;
        nodeBlocks.value = [];

        let currentNodeName: string | null = null;
        let currentBlockIndex: number = -1;
        const pendingSavePromises: Promise<void>[] = [];

        // 重置报告状态
        resetReportState(sessionState, request);

        const saveNodeMessage = (node: GraphNodeResponse[]): Promise<void> => {
          if (!node || !node.length) return Promise.resolve();

          // 特殊处理RESULT_SET节点
          if (node.length > 0 && node[0].textType === TextType.RESULT_SET) {
            try {
              const resultData: ResultData = JSON.parse(node[0].text);
              // 如果type不是table，保存一个特殊的标记，以便在历史消息中能够正确显示
              if (resultData.displayStyle?.type && resultData.displayStyle?.type !== 'table') {
                const aiMessage: ChatMessage = {
                  sessionId,
                  role: 'assistant',
                  content: node[0].text, // 保存原始JSON数据
                  messageType: 'result-set', // 使用特殊的messageType
                };
                return ChatService.saveMessage(sessionId, aiMessage).catch(error => {
                  console.error('保存AI消息失败:', error);
                });
              }
            } catch (error) {
              console.error('解析结果集JSON失败:', error);
            }
          }

          // 使用generateNodeHtml方法生成HTML代码，确保显示与保存一致
          const nodeHtml = generateNodeHtml(node);

          const aiMessage: ChatMessage = {
            sessionId,
            role: 'assistant',
            content: nodeHtml,
            messageType: 'html',
          };

          return ChatService.saveMessage(sessionId, aiMessage).catch(error => {
            console.error('保存AI消息失败:', error);
          });
        };

        // 发送流式请求
        const closeStream = await GraphService.streamSearch(
          request,
          (response: GraphNodeResponse) => {
            if (response.error) {
              ElMessage.error(`处理错误: ${response.text}`);
              return;
            }

            if (sessionState.lastRequest) {
              sessionState.lastRequest.threadId = response.threadId;
            }

            // 检查是否是报告节点
            if (response.nodeName === 'ReportGeneratorNode') {
              const isNewNode: boolean =
                currentNodeName === null || response.nodeName !== currentNodeName;

              if (isNewNode) {
                // 保存上一个节点的消息（如果有）
                if (currentBlockIndex >= 0 && sessionState.nodeBlocks[currentBlockIndex]) {
                  const savePromise = saveNodeMessage(sessionState.nodeBlocks[currentBlockIndex]);
                  pendingSavePromises.push(savePromise);
                }

                // 创建新的节点块
                const newBlock: GraphNodeResponse = {
                  ...response,
                  text: response.text,
                };
                sessionState.nodeBlocks.push([newBlock]);
                currentBlockIndex = sessionState.nodeBlocks.length - 1;
                currentNodeName = response.nodeName;
              }
              // 处理HTML报告
              if (response.textType === 'HTML') {
                sessionState.htmlReportContent += response.text;
                sessionState.htmlReportSize = sessionState.htmlReportContent.length;

                // 更新显示：当前已经收集了多少字节的报告
                const reportNode: GraphNodeResponse[] = sessionState.nodeBlocks.find(
                  (block: GraphNodeResponse[]) =>
                    block.length > 0 &&
                    block[0].nodeName === 'ReportGeneratorNode' &&
                    block[0].textType === 'HTML',
                );
                if (reportNode) {
                  reportNode[0].text = `正在收集HTML报告... 已收集 ${sessionState.htmlReportSize} 字节`;
                } else {
                  sessionState.nodeBlocks.push([
                    {
                      ...response,
                      text: `正在收集HTML报告... 已收集 ${sessionState.htmlReportSize} 字节`,
                    },
                  ]);
                }
              }
              // 处理Markdown报告
              else if (response.textType === 'MARK_DOWN') {
                sessionState.markdownReportContent += response.text;
                const reportNode: GraphNodeResponse[] = sessionState.nodeBlocks.find(
                  (block: GraphNodeResponse[]) =>
                    block.length > 0 &&
                    block[0].nodeName === 'ReportGeneratorNode' &&
                    block[0].textType === 'MARK_DOWN',
                );
                if (reportNode) {
                  reportNode[0].text = `正在收集Markdown报告... 已收集 ${sessionState.markdownReportContent.length} 字节`;
                } else {
                  sessionState.nodeBlocks.push([
                    {
                      ...response,
                      text: `正在收集Markdown报告... 已收集 ${sessionState.markdownReportContent.length} 字节`,
                    },
                  ]);
                }
              }
            } else if (response.textType === TextType.RESULT_SET) {
              currentNodeName = 'result_set';
              if (currentBlockIndex >= 0 && sessionState.nodeBlocks[currentBlockIndex]) {
                const savePromise = saveNodeMessage(sessionState.nodeBlocks[currentBlockIndex]);
                pendingSavePromises.push(savePromise);
              }
              // 创建新的节点块
              const newBlock: GraphNodeResponse = {
                ...response,
                text: response.text,
              };
              sessionState.nodeBlocks.push([newBlock]);
              currentBlockIndex = sessionState.nodeBlocks.length - 1;
            } else {
              // 处理其他节点（同步处理逻辑）
              const isNewNode: boolean =
                currentNodeName === null || response.nodeName !== currentNodeName;

              if (isNewNode) {
                // 保存上一个节点的消息（如果有）
                if (currentBlockIndex >= 0 && sessionState.nodeBlocks[currentBlockIndex]) {
                  const savePromise = saveNodeMessage(sessionState.nodeBlocks[currentBlockIndex]);
                  pendingSavePromises.push(savePromise);
                }

                // 创建新的节点块
                const newBlock: GraphNodeResponse = {
                  ...response,
                  text: response.text,
                };
                sessionState.nodeBlocks.push([newBlock]);
                currentBlockIndex = sessionState.nodeBlocks.length - 1;
                currentNodeName = response.nodeName;
              } else {
                // 继续当前节点的内容
                if (currentBlockIndex >= 0 && sessionState.nodeBlocks[currentBlockIndex]) {
                  const newBlock: GraphNodeResponse = {
                    ...response,
                    text: response.text,
                  };
                  sessionState.nodeBlocks[currentBlockIndex].push(newBlock);
                } else {
                  // 创建新的节点块
                  const newBlock: GraphNodeResponse = {
                    ...response,
                    text: response.text,
                  };
                  sessionState.nodeBlocks.push([newBlock]);
                  currentBlockIndex = sessionState.nodeBlocks.length - 1;
                  currentNodeName = response.nodeName;
                }
              }
            }

            // 如果是当前显示的会话，同步到视图并滚动
            if (currentSession.value?.id === sessionId) {
              nodeBlocks.value = sessionState.nodeBlocks;
              if (autoScroll.value) {
                scrollToBottom();
              }
            }
          },
          async (error: Error) => {
            ElMessage.error(`流式请求失败: ${error.message}`);
            console.error('error: ' + error);
            // 等待所有待处理的保存操作完成
            if (pendingSavePromises.length > 0) {
              await Promise.all(pendingSavePromises);
            }
            sessionState.isStreaming = false;
            sessionState.closeStream = null;
            currentNodeName = null;
            // 出错时只有当前会话才重新加载
            if (currentSession.value?.id === sessionId) {
              isStreaming.value = false;
              await selectSession(currentSession.value);
            }
          },
          async () => {
            // 等待所有待处理的保存操作完成
            if (pendingSavePromises.length > 0) {
              await Promise.all(pendingSavePromises);
            }

            // 保存报告到后端
            if (sessionState.htmlReportContent) {
              const htmlReportMessage: ChatMessage = {
                sessionId,
                role: 'assistant',
                content: sessionState.htmlReportContent,
                messageType: 'html-report',
              };

              await ChatService.saveMessage(sessionId, htmlReportMessage)
                .then(savedMessage => {
                  if (currentSession.value?.id === sessionId) {
                    currentMessages.value.push(savedMessage);
                  }
                })
                .catch(error => {
                  ElMessage.error('保存HTML报告失败！');
                  console.error('保存HTML报告失败:', error);
                });
              // 对话的HTML报告保存后结束流式响应，并判断是否需要同步页面
              sessionState.isStreaming = false;
              if (currentSession.value?.id === sessionId) {
                isStreaming.value = false;
                nodeBlocks.value = [];
              }
            } else if (sessionState.markdownReportContent) {
              const markdownMessage: ChatMessage = {
                sessionId,
                role: 'assistant',
                content: sessionState.markdownReportContent,
                messageType: 'markdown-report',
              };

              await ChatService.saveMessage(sessionId, markdownMessage)
                .then(savedMessage => {
                  if (currentSession.value?.id === sessionId) {
                    currentMessages.value.push(savedMessage);
                  }
                })
                .catch(error => {
                  console.error('保存Markdown报告失败:', error);
                });

              sessionState.isStreaming = false;
              if (currentSession.value?.id === sessionId) {
                isStreaming.value = false;
                nodeBlocks.value = [];
              }
            } else {
              // 其他节点，可能是错误或人类反馈模式
              // 保存最后一个节点的消息（如果有）
              if (currentBlockIndex >= 0 && sessionState.nodeBlocks[currentBlockIndex]) {
                await saveNodeMessage(sessionState.nodeBlocks[currentBlockIndex]);
              }

              // 如果是人工反馈模式，显示反馈组件
              if (requestOptions.value.humanFeedback && rejectedPlan) {
                showHumanFeedback.value = true;
              } else {
                // 所有节点处理完成
                sessionState.isStreaming = false;
                // 如果是当前显示的会话，同步到视图
                if (currentSession.value?.id === sessionId) {
                  isStreaming.value = false;
                }
              }
            }

            ElMessage.success(`会话[${sessionTitle}]处理完成`);
            currentNodeName = null;
            closeStream();
            // 只有当前会话才重新加载消息
            if (currentSession.value?.id === sessionId) {
              await selectSession(currentSession.value);
            }
          },
        );
        // 保存closeStream函数到会话状态
        sessionState.closeStream = closeStream;
      } catch (error) {
        ElMessage.error('发送消息失败');
        console.error('发送消息失败:', error);
        sessionState.isStreaming = false;
        sessionState.closeStream = null;
        if (currentSession.value?.id === sessionId) {
          isStreaming.value = false;
        }
      }
    };

    const formatMessageContent = (message: ChatMessage) => {
      if (message.messageType === 'text') {
        return message.content.replace(/\n/g, '<br>');
      }
      return message.content;
    };

    // 服务器端下载html报告
    const downloadHtmlReportFromMessageByServer = async (content: string) => {
      if (!content) {
        ElMessage.warning('没有可下载的HTML报告');
        return;
      }
      if (!currentSession.value) {
        ElMessage.warning('当前没有会话信息');
        return;
      }
      try {
        await ChatService.downloadHtmlReport(currentSession.value.id, content);
        ElMessage.success('HTML报告下载成功');
      } catch (error) {
        console.error('下载HTML报告失败:', error);
        ElMessage.error('下载HTML报告失败');
      }
    };

    const openReportFullscreen = (content: string) => {
      fullscreenReportContent.value = content;
      showReportFullscreen.value = true;
    };

    const closeReportFullscreen = () => {
      showReportFullscreen.value = false;
      fullscreenReportContent.value = '';
    };

    const downloadMarkdownReportFromMessage = (content: string) => {
      if (!content) {
        ElMessage.warning('没有可下载的Markdown报告');
        return;
      }

      const blob = new Blob([content], { type: 'text/markdown' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `report_${new Date().getTime()}.md`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      ElMessage.success('Markdown报告下载成功');
    };

    // 生成节点容器的HTML代码
    const generateNodeHtml = (node: GraphNodeResponse[]) => {
      const content = formatNodeContent(node);

      return `
        <div class="agent-response-block" style="display: block !important; width: 100% !important;">
          <div class="agent-response-title">${node.length > 0 ? node[0].nodeName : '空节点'}</div>
          <div class="agent-response-content">${content}</div>
        </div>
      `;
    };

    const formatNodeContent = (node: GraphNodeResponse[]) => {
      let content = '';

      for (let idx = 0; idx < node.length; idx++) {
        if (node[idx].textType === TextType.HTML) {
          content += node[idx].text;
        } else if (node[idx].textType === TextType.TEXT) {
          content += node[idx].text.replace(/\n/g, '<br>');
        } else if (
          node[idx].textType === TextType.JSON ||
          node[idx].textType === TextType.PYTHON ||
          node[idx].textType === TextType.SQL
        ) {
          let pre = '';
          let p = idx;
          for (; p < node.length; p++) {
            if (node[p].textType !== node[idx].textType) {
              break;
            }
            pre += node[p].text;
          }
          try {
            // 使用 highlight.js 进行代码高亮
            const language = node[idx].textType.toLowerCase();
            const highlighted = hljs.highlight(pre, { language });
            content += `<pre><div style="display: flex; justify-content: space-between; align-items: center; background: #f8f9fa; padding: 8px 12px; border-bottom: none; font-family: system-ui, sans-serif; font-size: 14px;"><span style="color: #666;">${language}</span><span hidden>${pre}</span><button onclick='copyTextToClipboard(this)' style="background: #f8f9fa; border: none; padding: 4px 12px; border-radius: 12px; font-size: 13px; cursor: pointer; transition: background 0.2s;">复制</button></div><code class="hljs ${language}">${highlighted.value}</code></pre>`;
          } catch (error) {
            // 如果高亮失败，返回原始代码
            content += `<pre><code>${pre}</code></pre>`;
          }
          if (p < node.length) {
            idx = p - 1;
          } else {
            break;
          }
        } else if (node[idx].textType === TextType.MARK_DOWN) {
          let markdown = '';
          let p = idx;
          for (; p < node.length; p++) {
            if (node[p].textType !== TextType.MARK_DOWN) {
              break;
            }
            markdown += node[p].text;
          }

          const safeHtml = markdownToHtml(markdown);
          content += `<div class="markdown-report">${safeHtml}</div>`;

          if (p < node.length) {
            idx = p - 1;
          } else {
            break;
          }
        } else if (node[idx].textType === TextType.RESULT_SET) {
          // 渲染结果集
          if (!resultSetDisplayConfig.value.showSqlResults) {
            // 如果用户关闭了显示SQL结果，直接忽略这个节点
            continue;
          }

          try {
            // 解析JSON字符串
            const resultData: ResultData = JSON.parse(node[idx].text);
            const resultSetData = resultData.resultSet;

            // 检查是否有错误信息
            if (resultSetData.errorMsg) {
              content += `<div class="result-set-error">错误: ${resultSetData.errorMsg}</div>`;
              continue;
            }

            // 检查数据是否为空
            if (
              !resultSetData.column ||
              resultSetData.column.length === 0 ||
              !resultSetData.data ||
              resultSetData.data.length === 0
            ) {
              content += `<div class="result-set-empty">查询结果为空</div>`;
              continue;
            }

            // 如果type是table，保持原有逻辑生成表格HTML
            // 否则返回空字符串，因为已经在模板中用ResultSetDisplay组件处理了
            if (resultData.displayStyle?.type === 'table' || !resultData.displayStyle?.type) {
              const tableHtml = generateResultSetTable(
                resultSetData,
                resultSetDisplayConfig.value.pageSize,
              );
              content += tableHtml;
            }
            // 如果type不是table，不生成HTML，由模板中的ResultSetDisplay组件处理
          } catch (error) {
            console.error('解析结果集JSON失败:', error);
            content += `<div class="result-set-error">解析结果集数据失败: ${error.message}</div>`;
          }
        } else {
          console.warn(`不支持的 textType: ${node[idx].textType}`);
          content += node[idx].text;
        }
      }

      return content;
    };

    // Markdown转HTML
    const markdownToHtml = (markdown: string): string => {
      if (!markdown) return '';
      // marked 默认会转为字符串，这里仅做必要的配置
      marked.setOptions({ gfm: true, breaks: true });
      const rawHtml = marked.parse(markdown) as string;
      return DOMPurify.sanitize(rawHtml);
    };

    // 重置报告状态
    const resetReportState = (sessionState: SessionRuntimeState, request: GraphRequest) => {
      sessionState.isStreaming = true;
      sessionState.nodeBlocks = [];
      sessionState.lastRequest = request;
      sessionState.htmlReportContent = '';
      sessionState.htmlReportSize = 0;
      sessionState.markdownReportContent = '';
    };

    const scrollToBottom = () => {
      nextTick(() => {
        if (chatContainer.value) {
          chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
        }
      });
    };

    const handleHumanFeedback = async (
      request: GraphRequest,
      rejectedPlan: boolean,
      content: string,
    ) => {
      content = content.trim() || 'Accept';
      showHumanFeedback.value = false;
      const newRequest: GraphRequest = { ...request };
      newRequest.rejectedPlan = rejectedPlan;
      newRequest.humanFeedbackContent = content;
      await sendGraphRequest(newRequest, rejectedPlan);
    };

    // 处理预设问题点击
    const handlePresetQuestionClick = async (question: string) => {
      if (isStreaming.value) {
        ElMessage.warning('智能体正在处理中，请稍后...');
        return;
      }

      // 如果没有会话，先创建新会话
      if (!currentSession.value) {
        try {
          const newSession = await ChatService.createSession(parseInt(agentId.value), '新会话');
          currentSession.value = newSession;
          ElMessage.success('新会话创建成功');
        } catch (error) {
          ElMessage.error('创建会话失败');
          return;
        }
      }

      userInput.value = question;
      // 自动发送消息
      nextTick(() => {
        sendMessage();
      });
    };

    // 停止流式响应
    const stopStreaming = async () => {
      if (!currentSession.value) {
        ElMessage.warning('当前没有活动的会话');
        return;
      }

      const sessionId = currentSession.value.id;
      const sessionState = getSessionState(sessionId);

      try {
        // 检查是否有活动的流式连接
        if (!sessionState.closeStream) {
          ElMessage.warning('没有正在进行的对话');
          return;
        }

        // 关闭 EventSource 连接
        sessionState.closeStream();
        sessionState.closeStream = null;

        // 保存已接收的节点消息
        if (sessionState.nodeBlocks && sessionState.nodeBlocks.length > 0) {
          const saveNodeMessage = (node: GraphNodeResponse[]): Promise<void> => {
            if (!node || !node.length) return Promise.resolve();

            const nodeHtml = generateNodeHtml(node);

            const aiMessage: ChatMessage = {
              sessionId,
              role: 'assistant',
              content: nodeHtml,
              messageType: 'html',
            };

            return ChatService.saveMessage(sessionId, aiMessage).catch(error => {
              console.error('保存AI消息失败:', error);
            });
          };

          // 保存所有未保存的节点块
          const savePromises = sessionState.nodeBlocks.map(block => saveNodeMessage(block));
          await Promise.all(savePromises).catch(error => {
            console.error('保存节点消息时出错:', error);
          });
        }

        // 清理流式状态
        sessionState.isStreaming = false;
        sessionState.nodeBlocks = [];
        sessionState.htmlReportContent = '';
        sessionState.htmlReportSize = 0;
        sessionState.markdownReportContent = '';

        // 如果是当前显示的会话，同步更新视图
        if (currentSession.value?.id === sessionId) {
          isStreaming.value = false;
          nodeBlocks.value = [];
        }

        // 重新加载会话消息以刷新显示
        await selectSession(currentSession.value);

        ElMessage.success('已停止对话');
      } catch (error) {
        console.error('停止对话时出错:', error);
        ElMessage.error('停止对话失败');
        // 确保状态清理总是执行
        sessionState.isStreaming = false;
        sessionState.closeStream = null;
        if (currentSession.value?.id === sessionId) {
          isStreaming.value = false;
          nodeBlocks.value = [];
        }
      }
    };

    // 生成结果集表格HTML
    const generateResultSetTable = (resultSetData: ResultSetData, pageSize: number): string => {
      const columns = resultSetData.column || [];
      const allData = resultSetData.data || [];
      const total = allData.length;

      // 分页逻辑 - 生成所有页面的HTML，通过CSS控制显示
      const totalPages = Math.ceil(total / pageSize);

      let tableHtml = `<div class="result-set-container"><div class="result-set-header"><div class="result-set-info"><span>查询结果 (共 ${total} 条记录)</span><div class="result-set-pagination-controls"><span class="result-set-pagination-info">第 <span class="result-set-current-page">1</span> 页，共 ${totalPages} 页</span><div class="result-set-pagination-buttons"><button class="result-set-pagination-btn result-set-pagination-prev" onclick="handleResultSetPagination(this, 'prev')" disabled>上一页</button><button class="result-set-pagination-btn result-set-pagination-next" onclick="handleResultSetPagination(this, 'next')" ${
        totalPages > 1 ? '' : 'disabled'
      }>下一页</button></div></div></div></div><div class="result-set-table-container">`;

      // 生成所有页面的表格
      for (let page = 1; page <= totalPages; page++) {
        const startIndex = (page - 1) * pageSize;
        const endIndex = Math.min(startIndex + pageSize, total);
        const currentPageData = allData.slice(startIndex, endIndex);

        tableHtml += `<div class="result-set-page ${
          page === 1 ? 'result-set-page-active' : ''
        }" data-page="${page}"><table class="result-set-table"><thead><tr>`;

        // 添加表头
        columns.forEach(column => {
          tableHtml += `<th>${escapeHtml(column)}</th>`;
        });

        tableHtml += `</tr></thead><tbody>`;

        // 添加表格数据
        if (currentPageData.length === 0) {
          tableHtml += `<tr><td colspan="${columns.length}" class="result-set-empty-cell">暂无数据</td></tr>`;
        } else {
          currentPageData.forEach(row => {
            tableHtml += `<tr>`;
            columns.forEach(column => {
              const value = row[column] || '';
              tableHtml += `<td>${escapeHtml(value)}</td>`;
            });
            tableHtml += `</tr>`;
          });
        }

        tableHtml += `</tbody></table></div>`;
      }

      tableHtml += `</div></div>`;

      return tableHtml;
    };

    // 从节点块中提取 Markdown 内容
    const getMarkdownContentFromNode = (node: GraphNodeResponse[]): string => {
      if (!node || node.length === 0) {
        return '';
      }

      // 如果是 ReportGeneratorNode 且类型为 MARK_DOWN，从 sessionState 获取完整内容
      // 这样可以实时显示流式接收到的 markdown 内容
      const firstNode = node[0];
      if (firstNode.nodeName === 'ReportGeneratorNode' && firstNode.textType === 'MARK_DOWN') {
        const sessionId = currentSession.value?.id;
        if (sessionId) {
          const sessionState = getSessionState(sessionId);
          // 返回实时更新的 markdown 内容
          return sessionState.markdownReportContent || '';
        }
      }

      // 否则从节点中提取所有 MARK_DOWN 类型的文本
      let markdown = '';
      for (let idx = 0; idx < node.length; idx++) {
        if (node[idx].textType === 'MARK_DOWN') {
          let p = idx;
          for (; p < node.length; p++) {
            if (node[p].textType !== 'MARK_DOWN') {
              break;
            }
            markdown += node[p].text;
          }
          if (p < node.length) {
            idx = p - 1;
          } else {
            break;
          }
        }
      }

      return markdown;
    };

    // HTML转义函数
    const escapeHtml = (text: string): string => {
      const div = document.createElement('div');
      div.textContent = text;
      return div.innerHTML;
    };

    // 生命周期
    onMounted(async () => {
      await loadAgent();
    });

    return {
      agent,
      currentSession,
      currentMessages,
      userInput,
      isStreaming,
      requestOptions,
      showReportFullscreen,
      fullscreenReportContent,
      inputControlsCollapsed,
      autoScroll,
      chatContainer,
      nodeBlocks,
      agentId,
      showHumanFeedback,
      lastRequest,
      resultSetDisplayConfig,
      options,
      getMarkdownContentFromNode,
      selectSession,
      sendMessage,
      formatMessageContent,
      formatNodeContent,
      generateNodeHtml,
      handleNl2sqlOnlyChange,
      openReportFullscreen,
      closeReportFullscreen,
      downloadMarkdownReportFromMessage,
      downloadHtmlReportFromMessageByServer,
      markdownToHtml,
      resetReportState,
      handleHumanFeedback,
      handlePresetQuestionClick,
      stopStreaming,
      deleteSessionState,
    };
  },
});
</script>

<style scoped>
.main-chat-area {
  background-color: #f8fafc;
  padding: 0;
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
  overflow: hidden;
}

/* 聊天容器样式 */
.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px 15% 40px;
  scroll-behavior: smooth;
  min-height: 0;
}

.empty-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 32px;
}

.empty-state-content {
  text-align: center;
  color: #64748b;
}

.empty-icon {
  font-size: 64px;
  color: #e2e8f0;
  margin-bottom: 16px;
}

.empty-state-content h3 {
  font-size: 18px;
  font-weight: 600;
  color: #334155;
  margin: 0 0 8px;
}

.empty-state-preset {
  width: 100%;
  max-width: 600px;
}

.messages-area {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.message-wrapper {
  width: 100%;
  display: flex;
}

.message-row {
  display: flex;
  gap: 16px;
  max-width: 85%;
  width: 100%;
}

.message-wrapper.user {
  justify-content: flex-end;
}

.message-wrapper.user .message-row {
  flex-direction: row;
  justify-content: flex-end;
}

.avatar-wrapper {
  flex-shrink: 0;
  margin-top: 4px;
}

.ai-avatar {
  background: #eff6ff;
  color: #3b82f6;
  font-weight: 600;
  border: 1px solid #dbeafe;
}

.user-avatar {
  background: #f1f5f9;
  color: #64748b;
}

.message-content-wrapper {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.message-wrapper.user .message-content-wrapper {
  align-items: flex-end;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 15px;
  line-height: 1.6;
  position: relative;
  word-wrap: break-word;
  max-width: 100%;
}

.text-content {
  background: white;
  border: 1px solid #e2e8f0;
  color: #1e293b;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  border-top-left-radius: 2px;
}

.message-wrapper.user .text-content {
  background: #4f46e5;
  color: white;
  border: none;
  box-shadow: 0 4px 6px -1px rgba(79, 70, 229, 0.1), 0 2px 4px -1px rgba(79, 70, 229, 0.06);
  border-radius: 12px;
  border-top-right-radius: 2px;
}

.message-wrapper.user .markdown-body {
  color: white !important;
}

.message-wrapper.user .markdown-body p {
  color: white !important;
}

.streaming-content {
  background: white;
  border: 1px solid #e2e8f0;
  padding: 16px;
  border-radius: 12px;
  border-top-left-radius: 2px;
}

.streaming-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 24px;
  margin-bottom: 12px;
}

.dot {
  width: 8px;
  height: 8px;
  background-color: #3b82f6;
  border-radius: 50%;
  display: inline-block;
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) {
  animation-delay: -0.32s;
}
.dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%,
  80%,
  100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

.agent-response-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.agent-response-block {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

.response-block-title {
  background: #f1f5f9;
  padding: 8px 12px;
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
  border-bottom: 1px solid #e2e8f0;
}

.response-block-body {
  padding: 12px;
  overflow-x: auto;
}
/* Input Section */
.input-section {
  padding: 0 15% 24px;
  z-index: 10;
}

.input-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
  overflow: hidden;
  transition: all 0.3s ease;
}

.input-card:focus-within {
  border-color: #6366f1;
  box-shadow: 0 20px 25px -5px rgba(99, 102, 241, 0.1), 0 10px 10px -5px rgba(99, 102, 241, 0.04);
}

.input-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: #f8fafc;
  border-bottom: 1px solid #f1f5f9;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.switch-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.switch-item .label {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.input-box {
  position: relative;
  padding: 16px;
}

.custom-textarea :deep(.el-textarea__inner) {
  box-shadow: none !important;
  border: none !important;
  padding: 0;
  padding-right: 48px; /* Space for send button */
  font-size: 15px;
  line-height: 1.5;
  background: transparent;
}

.send-actions {
  position: absolute;
  bottom: 12px;
  right: 12px;
}

.send-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* More Settings Popover */
.more-settings-popover {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #475569;
}

/* Human Feedback Overlay */
.human-feedback-overlay {
  position: absolute;
  bottom: 100px;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 600px;
  z-index: 20;
}

/* Custom Scrollbar */
.custom-scrollbar::-webkit-scrollbar {
  height: 6px;
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

/* Responsive */
@media (max-width: 1024px) {
  .chat-container,
  .input-section {
    padding-left: 5%;
    padding-right: 5%;
  }
}

@media (max-width: 768px) {
  .chat-container,
  .input-section {
    padding-left: 16px;
    padding-right: 16px;
  }

  .message-row {
    max-width: 100%;
  }
}
</style>

<style>
/* 结果集表格样式 */
.result-set-container {
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  margin: 8px 0;
}

.result-set-header {
  background: #f8f9fa;
  padding: 12px 16px;
  border-bottom: 1px solid #e8e8e8;
}

.result-set-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.result-set-pagination-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.result-set-pagination-info {
  font-size: 14px;
  color: #606266;
}

.result-set-pagination-buttons {
  display: flex;
  gap: 8px;
}

.result-set-pagination-btn {
  padding: 6px 12px;
  border: 1px solid #dcdfe6;
  background: white;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.result-set-pagination-btn:hover:not(:disabled) {
  background: #f5f7fa;
  border-color: #c6e2ff;
}

.result-set-pagination-btn:disabled {
  color: #c0c4cc;
  cursor: not-allowed;
  background: #f5f7fa;
}

.result-set-table-container {
  overflow-x: auto;
  position: relative;
}

.result-set-page {
  display: none;
}

.result-set-page-active {
  display: block;
}

.result-set-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.result-set-table th {
  background: #f5f7fa;
  padding: 8px 12px;
  text-align: left;
  font-weight: 600;
  color: #606266;
  border-bottom: 1px solid #e8e8e8;
  white-space: nowrap;
}

.result-set-table td {
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  word-break: break-word;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.result-set-table tr:hover {
  background: #f5f7fa;
}

.result-set-empty-cell {
  text-align: center;
  color: #909399;
  padding: 20px;
}

.result-set-error {
  background: #fef0f0;
  color: #f56c6c;
  padding: 8px 12px;
  border-radius: 4px;
  margin: 8px 0;
  border: 1px solid #fbc4c4;
}

.result-set-empty {
  background: #f4f4f5;
  color: #909399;
  padding: 8px 12px;
  border-radius: 4px;
  margin: 8px 0;
  text-align: center;
}

.result-set-message {
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .result-set-table-container {
    font-size: 12px;
  }

  .result-set-table th,
  .result-set-table td {
    padding: 6px 8px;
  }
}
</style>
