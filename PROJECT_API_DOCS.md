# DataAgent 后端接口与功能文档

本文档基于 `data-agent-management` 项目代码生成，详细列出了项目的功能模块、API 接口及相关数据结构。

## 1. 项目概览

DataAgent 是一个基于 Spring AI Alibaba 的数据智能体管理平台，提供自然语言查数（Text-to-SQL）、数据分析报表生成等功能。后端主要负责智能体管理、数据源配置、知识库管理、语义模型构建以及对话流的处理。

## 2. 功能模块与接口详情

### 2.1 智能体管理 (Agent Management)

负责智能体的创建、配置、发布及 API Key 管理。

**Controller**: `AgentController` (`/api/agent`)

| 方法   | 路径                     | 描述                | 请求参数/Body                                     | 返回值                        |
| :----- | :----------------------- | :------------------ | :------------------------------------------------ | :---------------------------- |
| GET    | `/list`                  | 获取智能体列表      | `status` (String, 可选), `keyword` (String, 可选) | `List<Agent>`                 |
| GET    | `/{id}`                  | 获取智能体详情      | Path: `id`                                        | `Agent`                       |
| POST   | `/`                      | 创建智能体          | Body: `Agent`                                     | `Agent`                       |
| PUT    | `/{id}`                  | 更新智能体          | Path: `id`, Body: `Agent`                         | `Agent`                       |
| DELETE | `/{id}`                  | 删除智能体          | Path: `id`                                        | void                          |
| POST   | `/{id}/publish`          | 发布智能体          | Path: `id`                                        | `Agent`                       |
| POST   | `/{id}/offline`          | 下线智能体          | Path: `id`                                        | `Agent`                       |
| GET    | `/{id}/api-key`          | 获取 API Key (脱敏) | Path: `id`                                        | `ApiResponse<ApiKeyResponse>` |
| POST   | `/{id}/api-key/generate` | 生成 API Key        | Path: `id`                                        | `ApiResponse<ApiKeyResponse>` |
| POST   | `/{id}/api-key/reset`    | 重置 API Key        | Path: `id`                                        | `ApiResponse<ApiKeyResponse>` |
| DELETE | `/{id}/api-key`          | 删除 API Key        | Path: `id`                                        | `ApiResponse<ApiKeyResponse>` |
| POST   | `/{id}/api-key/enable`   | 启用/禁用 API Key   | Path: `id`, Query: `enabled` (boolean)            | `ApiResponse<ApiKeyResponse>` |

**相关实体**: `Agent`

- `name`: 智能体名称
- `description`: 描述
- `status`: 状态 (draft, published, offline)
- `apiKey`: 对外访问密钥
- `prompt`: 自定义 Prompt 配置

### 2.2 智能体数据源配置 (Agent Datasource)

管理智能体关联的数据源及 Schema 初始化。

**Controller**: `AgentDatasourceController` (`/api/agent/{agentId}/datasources`)

| 方法   | 路径              | 描述                   | 请求参数/Body                     | 返回值                               |
| :----- | :---------------- | :--------------------- | :-------------------------------- | :----------------------------------- |
| GET    | `/`               | 获取智能体关联的数据源 | Path: `agentId`                   | `ApiResponse<List<AgentDatasource>>` |
| GET    | `/active`         | 获取当前激活的数据源   | Path: `agentId`                   | `ApiResponse<AgentDatasource>`       |
| POST   | `/init`           | 初始化 Schema (向量化) | Path: `agentId`                   | `ApiResponse<?>`                     |
| POST   | `/{datasourceId}` | 添加数据源关联         | Path: `agentId`, `datasourceId`   | `ApiResponse<AgentDatasource>`       |
| POST   | `/tables`         | 更新选中的数据表       | Body: `UpdateDatasourceTablesDTO` | `ApiResponse<?>`                     |
| DELETE | `/{datasourceId}` | 移除数据源关联         | Path: `agentId`, `datasourceId`   | `ApiResponse<?>`                     |
| PUT    | `/toggle`         | 启用/禁用数据源        | Body: `ToggleDatasourceDTO`       | `ApiResponse<AgentDatasource>`       |

**相关实体**: `AgentDatasource`

- `selectTables`: 选中的表列表
- `isActive`: 是否激活

### 2.3 智能体知识库 (Agent Knowledge)

管理 RAG 知识库，支持文档上传和 QA 问答对。

**Controller**: `AgentKnowledgeController` (`/api/agent-knowledge`)

| 方法   | 路径                    | 描述               | 请求参数/Body                       | 返回值                                 |
| :----- | :---------------------- | :----------------- | :---------------------------------- | :------------------------------------- |
| POST   | `/create`               | 创建知识(上传文件) | Form: `CreateKnowledgeDTO` (含文件) | `ApiResponse<AgentKnowledgeVO>`        |
| GET    | `/{id}`                 | 获取知识详情       | Path: `id`                          | `ApiResponse<AgentKnowledgeVO>`        |
| PUT    | `/{id}`                 | 更新知识           | Body: `UpdateKnowledgeDTO`          | `ApiResponse<AgentKnowledgeVO>`        |
| DELETE | `/{id}`                 | 删除知识           | Path: `id`                          | `ApiResponse<Boolean>`                 |
| PUT    | `/recall/{id}`          | 更新召回状态       | Path: `id`, Query: `isRecall`       | `ApiResponse<AgentKnowledgeVO>`        |
| POST   | `/query/page`           | 分页查询知识列表   | Body: `AgentKnowledgeQueryDTO`      | `PageResponse<List<AgentKnowledgeVO>>` |
| POST   | `/retry-embedding/{id}` | 重试向量化         | Path: `id`                          | `ApiResponse<AgentKnowledgeVO>`        |

**相关DTO**: `CreateKnowledgeDTO`

- `type`: 知识类型 (DOCUMENT, QA, FAQ)
- `file`: 文件 (MultipartFile)
- `question`/`content`: 问答内容
- `splitterType`: 分块策略 (token, recursive)

### 2.4 业务术语/知识 (Business Knowledge)

管理业务术语，帮助 LLM 理解行业黑话。

**Controller**: `BusinessKnowledgeController` (`/api/business-knowledge`)

| 方法   | 路径                    | 描述         | 请求参数/Body                      | 返回值                                   |
| :----- | :---------------------- | :----------- | :--------------------------------- | :--------------------------------------- |
| GET    | `/`                     | 列表查询     | Query: `agentId`, `keyword`        | `ApiResponse<List<BusinessKnowledgeVO>>` |
| POST   | `/`                     | 创建业务术语 | Body: `CreateBusinessKnowledgeDTO` | `ApiResponse<BusinessKnowledgeVO>`       |
| PUT    | `/{id}`                 | 更新业务术语 | Body: `UpdateBusinessKnowledgeDTO` | `ApiResponse<BusinessKnowledgeVO>`       |
| DELETE | `/{id}`                 | 删除业务术语 | Path: `id`                         | `ApiResponse<Boolean>`                   |
| POST   | `/recall/{id}`          | 设置是否召回 | Query: `isRecall`                  | `ApiResponse<Boolean>`                   |
| POST   | `/refresh-vector-store` | 刷新向量库   | Query: `agentId`                   | `ApiResponse<Boolean>`                   |

### 2.5 数据源管理 (Datasource Management)

全局数据源配置，支持 MySQL, PostgreSQL, H2 等。

**Controller**: `DatasourceController` (`/api/datasource`)

| 方法 | 路径                               | 描述           | 请求参数/Body                    | 返回值                               |
| :--- | :--------------------------------- | :------------- | :------------------------------- | :----------------------------------- |
| GET  | `/`                                | 获取数据源列表 | Query: `status`, `type`          | `List<Datasource>`                   |
| POST | `/`                                | 创建数据源     | Body: `Datasource`               | `Datasource`                         |
| PUT  | `/{id}`                            | 更新数据源     | Body: `Datasource`               | `Datasource`                         |
| POST | `/{id}/test`                       | 测试连接       | Path: `id`                       | `ApiResponse`                        |
| GET  | `/{id}/tables`                     | 获取所有表名   | Path: `id`                       | `List<String>`                       |
| GET  | `/{id}/tables/{tableName}/columns` | 获取表字段     | Path: `id`, `tableName`          | `ApiResponse<List<String>>`          |
| GET  | `/{id}/logical-relations`          | 获取逻辑外键   | Path: `id`                       | `ApiResponse<List<LogicalRelation>>` |
| POST | `/{id}/logical-relations`          | 添加逻辑外键   | Body: `CreateLogicalRelationDTO` | `ApiResponse<LogicalRelation>`       |

**相关实体**: `Datasource`

- `type`: 数据库类型
- `host`, `port`, `databaseName`, `username`, `password`
- `connectionUrl`: JDBC URL

**相关实体**: `LogicalRelation`

- 定义表与表之间的逻辑关联 (1:1, 1:N)，辅助 Text-to-SQL 生成。

### 2.6 语义模型 (Semantic Model)

为数据库字段添加业务语义（别名、描述、同义词）。

**Controller**: `SemanticModelController` (`/api/semantic-model`)

| 方法 | 路径                 | 描述           | 请求参数/Body                       | 返回值                             |
| :--- | :------------------- | :------------- | :---------------------------------- | :--------------------------------- |
| GET  | `/`                  | 查询语义模型   | Query: `keyword`, `agentId`         | `ApiResponse<List<SemanticModel>>` |
| POST | `/`                  | 创建语义模型   | Body: `SemanticModelAddDTO`         | `ApiResponse<Boolean>`             |
| POST | `/batch-import`      | 批量导入(JSON) | Body: `SemanticModelBatchImportDTO` | `ApiResponse<BatchImportResult>`   |
| POST | `/import/excel`      | Excel 导入     | Form: `file`, `agentId`             | `ApiResponse<BatchImportResult>`   |
| GET  | `/template/download` | 下载导入模板   | -                                   | Excel File                         |

**相关实体**: `SemanticModel`

- `tableName`, `columnName`: 物理表/字段
- `businessName`: 业务名称 (e.g., "客户满意度")
- `businessDescription`: 业务描述
- `synonyms`: 同义词

### 2.7 对话与执行 (Chat & Graph)

核心对话交互接口。

**Controller**: `ChatController` (`/api`)

| 方法 | 路径                                 | 描述           | 请求参数/Body          | 返回值              |
| :--- | :----------------------------------- | :------------- | :--------------------- | :------------------ |
| POST | `/agent/{id}/sessions`               | 创建会话       | Path: `id`             | `ChatSession`       |
| GET  | `/sessions/{sessionId}/messages`     | 获取会话消息   | Path: `sessionId`      | `List<ChatMessage>` |
| POST | `/sessions/{sessionId}/messages`     | 发送/保存消息  | Body: `ChatMessageDTO` | `ChatMessage`       |
| POST | `/sessions/{sessionId}/reports/html` | 下载 HTML 报表 | Body: Content          | File                |

**Controller**: `GraphController` (`/api`)

| 方法 | 路径             | 描述                       | 请求参数/Body                                             | 返回值                           |
| :--- | :--------------- | :------------------------- | :-------------------------------------------------------- | :------------------------------- |
| GET  | `/stream/search` | **核心流式对话接口** (SSE) | Query: `agentId`, `query`, `threadId`, `humanFeedback`... | SSE Stream (`GraphNodeResponse`) |

**相关DTO**: `GraphRequest`

- `query`: 用户提问
- `humanFeedback`: 是否包含人工反馈
- `nl2sqlOnly`: 是否仅执行 SQL 生成而不执行图表分析

### 2.8 模型配置 (Model Config)

配置 LLM (Chat) 和 Embedding 模型。

**Controller**: `ModelConfigController` (`/api/model-config`)

| 方法 | 路径             | 描述             | 请求参数/Body          | 返回值                              |
| :--- | :--------------- | :--------------- | :--------------------- | :---------------------------------- |
| GET  | `/list`          | 获取模型配置列表 | -                      | `ApiResponse<List<ModelConfigDTO>>` |
| POST | `/add`           | 新增配置         | Body: `ModelConfigDTO` | `ApiResponse<String>`               |
| POST | `/activate/{id}` | 切换激活模型     | Path: `id`             | `ApiResponse<String>`               |
| POST | `/test`          | 连通性测试       | Body: `ModelConfigDTO` | `ApiResponse<String>`               |

### 2.9 提示词配置 (Prompt Config)

管理系统 Prompt 和各类任务（SQL生成、Python生成）的 Prompt 模板。

**Controller**: `PromptConfigController` (`/api/prompt-config`)

| 方法 | 路径                         | 描述             | 请求参数/Body           | 返回值           |
| :--- | :--------------------------- | :--------------- | :---------------------- | :--------------- |
| POST | `/save`                      | 保存 Prompt 配置 | Body: `PromptConfigDTO` | `ResponseEntity` |
| GET  | `/list-by-type/{promptType}` | 按类型查询       | Path: `promptType`      | `ResponseEntity` |
| GET  | `/active/{promptType}`       | 获取当前激活配置 | Path: `promptType`      | `ResponseEntity` |

支持的 Prompt 类型: `report-generator`, `planner`, `sql-generator`, `python-generator`, `rewrite`.

### 2.10 其他接口

- **预设问题**: `AgentPresetQuestionController` (`/api/agent/{agentId}/preset-questions`) - 管理 Agent 的推荐问题。
- **文件上传**: `FileUploadController` (`/api/upload`) - 上传头像等文件。
- **会话事件**: `SessionEventController` (`/api/agent/{agentId}/sessions/stream`) - 监听会话状态更新。
- **健康检查**: `EchoController` (`/echo/ok`)

## 3. 数据实体字段详情 (Key Entities)

### Agent (智能体)

```java
Long id;
String name;          // 名称
String description;   // 描述
String status;        // 状态 (draft, published, offline)
String apiKey;        // API Key
Integer apiKeyEnabled;// API Key 开关
String prompt;        // 自定义 System Prompt
```

### Datasource (数据源)

```java
Integer id;
String name;          // 数据源名称
String type;          // 类型 (mysql, h2, pg, etc.)
String host;
Integer port;
String databaseName;
String username;
String password;
String connectionUrl;
```

### SemanticModel (语义模型)

```java
Long id;
Long agentId;
String tableName;
String columnName;
String businessName;       // 业务名称
String businessDescription;// 业务描述
String synonyms;           // 同义词
String dataType;           // 物理类型
Integer status;            // 0=停用, 1=启用
```

### AgentKnowledge (知识库)

```java
Integer id;
String title;
KnowledgeType type;       // DOCUMENT, QA, FAQ
String content;           // 内容
String question;          // 问题 (FAQ/QA)
String filePath;          // 文件路径
EmbeddingStatus embeddingStatus; // 向量化状态
```

---

_文档生成时间: 2026-03-15_
