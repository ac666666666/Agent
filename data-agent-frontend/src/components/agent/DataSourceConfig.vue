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
  <div class="datasource-config">
    <div class="config-card">
      <div class="card-header">
        <h2 class="section-title">数据源配置</h2>
        <p class="section-desc">管理智能体连接的数据库和数据表，支持多种数据库类型。</p>
      </div>

      <!-- Toolbar -->
      <div class="action-bar">
        <div class="left-actions">
          <h3 class="subsection-title">数据源列表</h3>
        </div>
        <div class="right-actions">
          <el-button @click="dialogVisible = true" type="primary" :icon="Plus" class="action-btn">
            添加数据源
          </el-button>
          <el-button
            @click="initAgentDatasource"
            v-if="!initStatus"
            type="primary"
            plain
            :icon="UploadFilled"
            class="action-btn"
          >
            初始化数据源
          </el-button>
          <el-button v-else type="primary" plain loading class="action-btn">初始化中...</el-button>
        </div>
      </div>

      <!-- Table -->
      <div class="table-container">
        <el-table
          :data="datasource"
          style="width: 100%"
          :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600' }"
          row-key="id"
          @expand-change="handleExpandChange"
        >
          <el-table-column type="expand" width="80" label="展开">
            <template #default="scope">
              <div
                v-if="scope.row.status === 'active'"
                style="padding: 20px; background: #f8fafc; border-radius: 8px; margin: 10px 20px"
              >
                <div
                  style="
                    margin-bottom: 15px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                  "
                >
                  <h4 style="margin: 0; color: #334155; font-size: 0.95rem">数据表管理</h4>
                  <el-button
                    @click="loadDatasourceTables(scope.row)"
                    size="small"
                    type="primary"
                    :loading="tableLoadingStates[scope.row.id]"
                    plain
                  >
                    刷新表列表
                  </el-button>
                </div>

                <div v-if="tableLists[scope.row.id] && tableLists[scope.row.id].length > 0">
                  <el-checkbox-group v-model="selectedTables[scope.row.id]">
                    <el-row :gutter="10">
                      <el-col
                        v-for="table in tableLists[scope.row.id]"
                        :key="table"
                        :span="6"
                        style="margin-bottom: 10px"
                      >
                        <el-checkbox :label="table" size="default">
                          {{ table }}
                        </el-checkbox>
                      </el-col>
                    </el-row>
                  </el-checkbox-group>

                  <div style="margin-top: 20px; text-align: right">
                    <el-button
                      @click="updateDatasourceTables(scope.row)"
                      size="small"
                      type="success"
                      :loading="updateLoadingStates[scope.row.id]"
                    >
                      更新数据表
                    </el-button>
                    <el-button @click="selectAllTables(scope.row)" size="small" type="primary" link>
                      全选
                    </el-button>
                    <el-button @click="clearAllTables(scope.row)" size="small" type="info" link>
                      清空
                    </el-button>
                  </div>
                </div>
                <div
                  v-else-if="tableLoadingStates[scope.row.id]"
                  style="text-align: center; padding: 20px"
                >
                  <el-icon class="is-loading" style="font-size: 20px; color: #6366f1">
                    <Loading />
                  </el-icon>
                  <div style="margin-top: 10px; color: #64748b; font-size: 0.9rem">
                    正在加载表列表...
                  </div>
                </div>
                <div v-else style="text-align: center; padding: 20px; color: #94a3b8">
                  <el-icon style="font-size: 24px"><FolderOpened /></el-icon>
                  <div style="margin-top: 10px; font-size: 0.9rem">
                    暂无表数据，请点击刷新表列表
                  </div>
                </div>
              </div>
              <div v-else style="padding: 20px; text-align: center; color: #94a3b8">
                <el-icon style="font-size: 24px"><Lock /></el-icon>
                <div style="margin-top: 10px; font-size: 0.9rem">请先启用数据源以管理表</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="数据源名称" min-width="140px">
            <template #default="scope">
              <span style="font-weight: 600; color: #334155">{{ scope.row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="类型" min-width="100px">
            <template #default="scope">
              <el-tag effect="plain" type="info" size="small">{{ scope.row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="connectionUrl" label="连接地址" min-width="200px">
            <template #default="scope">
              <el-tooltip
                :content="scope.row.connectionUrl"
                placement="top"
                :disabled="!scope.row.connectionUrl || scope.row.connectionUrl.length <= 50"
              >
                <span class="connection-url-text">
                  {{ scope.row.connectionUrl ? truncateText(scope.row.connectionUrl, 40) : '-' }}
                </span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="连接状态" min-width="100px">
            <template #default="scope">
              <el-tag
                :type="scope.row.testStatus === 'success' ? 'success' : 'danger'"
                size="small"
                effect="light"
              >
                {{ scope.row.testStatus === 'success' ? '连接成功' : '连接失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" min-width="80px">
            <template #default="scope">
              <el-switch
                :model-value="scope.row.status === 'active'"
                @change="val => changeDatasource(scope.row, val)"
                size="small"
                style="--el-switch-on-color: #10b981"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280px" fixed="right">
            <template #default="scope">
              <div style="display: flex; gap: 8px">
                <el-button @click="testConnection(scope.row)" size="small" type="primary" link>
                  测试
                </el-button>
                <el-button
                  @click="openForeignKeyDialog(scope.row)"
                  size="small"
                  type="primary"
                  link
                >
                  <el-icon style="margin-right: 2px"><Connection /></el-icon>
                  逻辑外键
                </el-button>
                <el-button @click="editDatasource(scope.row)" size="small" type="primary" link>
                  编辑
                </el-button>
                <el-button
                  @click="removeAgentDatasource(scope.row)"
                  size="small"
                  type="danger"
                  link
                >
                  移除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>

  <!-- 添加数据源Dialog -->
  <el-dialog v-model="dialogVisible" title="添加数据源" width="900px" destroy-on-close>
    <el-tabs v-model="dialogActiveName" type="border-card" stretch class="custom-tabs">
      <el-tab-pane label="选择已有数据源" name="select">
        <!-- todo: 添加分页和查询 -->
        <el-table
          @current-change="handleSelectDatasourceChange"
          :data="allDatasource"
          highlight-current-row
          style="width: 100%"
          height="400"
        >
          <el-table-column property="name" label="数据源名称" width="150" />
          <el-table-column property="type" label="类型" width="100" />
          <el-table-column property="host" label="Host" width="120" />
          <el-table-column property="port" label="Port" width="80" />
          <el-table-column property="description" label="描述" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button @click="editDatasource(scope.row)" size="small" type="primary" link>
                修改
              </el-button>
              <el-button @click="deleteDatasource(scope.row)" size="small" type="danger" link>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top: 20px; text-align: right">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addSelectDatasource">添加选中数据源</el-button>
        </div>
      </el-tab-pane>
      <el-tab-pane label="添加新数据源" name="add">
        <div style="padding: 10px">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="form-item">
                <label>
                  数据源名称
                  <span style="color: #ef4444">*</span>
                </label>
                <el-input
                  v-model="newDatasource.name"
                  placeholder="请输入数据源名称"
                  size="large"
                />
              </div>
            </el-col>
            <el-col :span="12">
              <div class="form-item">
                <label>
                  数据源类型
                  <span style="color: #ef4444">*</span>
                </label>
                <el-select
                  v-model="newDatasource.type"
                  placeholder="请选择数据源类型"
                  style="width: 100%"
                  size="large"
                >
                  <el-option key="mysql" label="MySQL" value="mysql" />
                  <el-option key="postgresql" label="PostgreSQL" value="postgresql" />
                  <el-option key="sqlserver" label="SQL Server" value="sqlserver" />
                  <el-option key="dameng" label="达梦(Dameng)" value="dameng" />
                  <el-option key="oracle" label="Oracle" value="oracle" />
                </el-select>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="form-item">
                <label>
                  主机地址
                  <span style="color: #ef4444">*</span>
                </label>
                <el-input
                  v-model="newDatasource.host"
                  placeholder="例如：localhost 或 192.168.1.100"
                  size="large"
                />
              </div>
            </el-col>
            <el-col :span="12">
              <div class="form-item">
                <label>
                  端口号
                  <span style="color: #ef4444">*</span>
                </label>
                <el-input-number
                  v-model="newDatasource.port"
                  :min="0"
                  :max="65535"
                  size="large"
                  style="width: 100%"
                />
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="form-item">
                <label v-if="newDatasource.type === 'postgresql'">
                  数据库名
                  <span style="color: #ef4444">*</span>
                </label>
                <label v-else>
                  数据库名
                  <span style="color: #ef4444">*</span>
                </label>
                <el-input
                  v-model="newDatasource.databaseName"
                  :placeholder="
                    newDatasource.type === 'postgresql' ? '例如：postgres' : '请输入数据库名称'
                  "
                  size="large"
                />
              </div>
            </el-col>
            <el-col
              :span="12"
              v-if="newDatasource.type === 'postgresql' || newDatasource.type === 'oracle'"
            >
              <div class="form-item">
                <label>
                  Schema 名
                  <span style="color: #ef4444">*</span>
                </label>
                <el-input
                  v-model="schemaName"
                  :placeholder="
                    newDatasource.type === 'postgresql' ? '例如：public' : '例如：SYSTEM'
                  "
                  size="large"
                />
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col>
              <div class="form-item">
                <label>连接地址</label>
                <el-input
                  v-model="newDatasource.connectionUrl"
                  placeholder="请输入JDBC地址（若不填则自动生成）"
                  size="large"
                />
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="form-item">
                <label>
                  用户名
                  <span style="color: #ef4444">*</span>
                </label>
                <el-input
                  v-model="newDatasource.username"
                  placeholder="请输入数据库用户名"
                  size="large"
                />
              </div>
            </el-col>
            <el-col :span="12">
              <div class="form-item">
                <label>
                  密码
                  <span style="color: #ef4444">*</span>
                </label>
                <el-input
                  v-model="newDatasource.password"
                  placeholder="请输入数据库密码"
                  size="large"
                  show-password
                />
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="30">
            <el-col :span="24">
              <div class="form-item">
                <label>描述</label>
                <el-input
                  v-model="newDatasource.description"
                  :rows="3"
                  type="textarea"
                  placeholder="请输入数据源描述（可选）"
                  size="large"
                />
              </div>
            </el-col>
          </el-row>

          <div style="text-align: right; margin-top: 10px">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="createNewDatasource">创建并添加</el-button>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-dialog>

  <el-dialog v-model="editDialogVisible" title="编辑数据源" width="900px">
    <div style="padding: 0 10px">
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="form-item">
            <label>
              数据源名称
              <span style="color: #ef4444">*</span>
            </label>
            <el-input
              v-model="editingDatasource.name"
              placeholder="请输入数据源名称"
              size="large"
            />
          </div>
        </el-col>
        <el-col :span="12">
          <div class="form-item">
            <label>
              数据源类型
              <span style="color: #ef4444">*</span>
            </label>
            <el-select
              v-model="editingDatasource.type"
              placeholder="请选择数据源类型"
              style="width: 100%"
              size="large"
            >
              <el-option key="mysql" label="MySQL" value="mysql" />
              <el-option key="postgresql" label="PostgreSQL" value="postgresql" />
              <el-option key="dameng" label="达梦(Dameng)" value="dameng" />
            </el-select>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="form-item">
            <label>
              主机地址
              <span style="color: #ef4444">*</span>
            </label>
            <el-input
              v-model="editingDatasource.host"
              placeholder="例如：localhost 或 192.168.1.100"
              size="large"
            />
          </div>
        </el-col>
        <el-col :span="12">
          <div class="form-item">
            <label>
              端口号
              <span style="color: #ef4444">*</span>
            </label>
            <el-input-number
              v-model="editingDatasource.port"
              :min="0"
              :max="65535"
              size="large"
              style="width: 100%"
            />
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="form-item">
            <label v-if="editingDatasource.type === 'postgresql'">
              数据库名
              <span style="color: #ef4444">*</span>
            </label>
            <label v-else>
              数据库名
              <span style="color: #ef4444">*</span>
            </label>
            <el-input
              v-model="editingDatasource.databaseName"
              :placeholder="
                editingDatasource.type === 'postgresql' ? '例如：postgres' : '请输入数据库名称'
              "
              size="large"
            />
          </div>
        </el-col>
        <el-col
          :span="12"
          v-if="editingDatasource.type === 'postgresql' || editingDatasource.type === 'oracle'"
        >
          <div class="form-item">
            <label>
              Schema 名
              <span style="color: #ef4444">*</span>
            </label>
            <el-input
              v-model="schemaNameEdit"
              :placeholder="
                editingDatasource.type === 'postgresql' ? '例如：public' : '例如：SYSTEM'
              "
              size="large"
            />
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col>
          <div class="form-item">
            <label>连接地址</label>
            <el-input
              v-model="editingDatasource.connectionUrl"
              placeholder="请输入JDBC地址（若不填则自动生成）"
              size="large"
            />
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="form-item">
            <label>
              用户名
              <span style="color: #ef4444">*</span>
            </label>
            <el-input
              v-model="editingDatasource.username"
              placeholder="请输入数据库用户名"
              size="large"
            />
          </div>
        </el-col>
        <el-col :span="12">
          <div class="form-item">
            <label>
              密码
              <span style="color: #ef4444">*</span>
            </label>
            <el-input
              v-model="editingDatasource.password"
              placeholder="请输入数据库密码"
              size="large"
              show-password
            />
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="30">
        <el-col :span="24">
          <div class="form-item">
            <label>描述</label>
            <el-input
              v-model="editingDatasource.description"
              :rows="3"
              type="textarea"
              placeholder="请输入数据源描述（可选）"
              size="large"
            />
          </div>
        </el-col>
      </el-row>

      <div style="text-align: right; margin-top: 10px">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEditDatasource">保存修改</el-button>
      </div>
    </div>
  </el-dialog>

  <!-- 逻辑外键配置Dialog（逻辑外键管理） -->
  <el-dialog
    v-model="foreignKeyDialogVisible"
    title="逻辑外键配置"
    width="900px"
    :close-on-click-modal="false"
  >
    <div v-if="currentForeignKeyDatasource">
      <div
        style="
          margin-bottom: 20px;
          padding: 12px;
          background: #f0f9ff;
          border-radius: 6px;
          border-left: 4px solid #0ea5e9;
        "
      >
        <p style="margin: 0; font-size: 14px; color: #475569">
          当前配置数据源：
          <span style="font-weight: 600; color: #0f172a">
            {{ currentForeignKeyDatasource.name }}
          </span>
        </p>
      </div>

      <!-- 已生效的逻辑外键列表 -->
      <div style="margin-bottom: 30px">
        <h4
          style="
            font-size: 14px;
            font-weight: 600;
            color: #334155;
            margin-bottom: 15px;
            display: flex;
            align-items: center;
            gap: 8px;
          "
        >
          <el-icon color="#6366f1"><Link /></el-icon>
          已生效的逻辑外键 (Logical Foreign Keys)
        </h4>
        <el-table
          :data="foreignKeyList"
          border
          style="width: 100%"
          size="small"
          :header-cell-style="{ background: '#f8fafc', color: '#64748b' }"
        >
          <el-table-column prop="sourceTableName" label="主表 (Source)" min-width="120px">
            <template #default="scope">
              <span style="font-family: monospace; color: #6366f1; font-weight: 500">
                {{ scope.row.sourceTableName }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="sourceColumnName" label="字段" min-width="100px">
            <template #default="scope">
              <span style="font-family: monospace; color: #475569">
                {{ scope.row.sourceColumnName }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="关系" min-width="80px" align="center">
            <template #default="scope">
              <el-tag size="small" effect="plain">{{ scope.row.relationType || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="targetTableName" label="关联表 (Target)" min-width="120px">
            <template #default="scope">
              <span style="font-family: monospace; color: #10b981; font-weight: 500">
                {{ scope.row.targetTableName }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="targetColumnName" label="字段" min-width="100px">
            <template #default="scope">
              <span style="font-family: monospace; color: #475569">
                {{ scope.row.targetColumnName }}
              </span>
            </template>
          </el-table-column>
          <el-table-column
            prop="description"
            label="描述"
            min-width="120px"
            show-overflow-tooltip
          />
          <el-table-column label="操作" width="120px" fixed="right">
            <template #default="scope">
              <el-button @click="editForeignKey(scope.row)" size="small" type="primary" link>
                编辑
              </el-button>
              <el-button
                @click="deleteForeignKey(scope.row, scope.$index)"
                size="small"
                type="danger"
                link
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div
          v-if="!foreignKeyList || foreignKeyList.length === 0"
          style="
            text-align: center;
            padding: 30px;
            color: #94a3b8;
            background: #f8fafc;
            border: 1px dashed #e2e8f0;
            border-radius: 6px;
            margin-top: 10px;
          "
        >
          <div style="font-size: 0.9rem">暂无逻辑外键配置</div>
        </div>
      </div>

      <!-- 新增/编辑关联关系表单 -->
      <div
        style="background: #f8fafc; padding: 20px; border-radius: 8px; border: 1px solid #e2e8f0"
      >
        <h4 style="font-size: 14px; font-weight: 600; color: #334155; margin-bottom: 15px">
          <el-icon style="margin-right: 6px; vertical-align: middle; color: #6366f1">
            <CirclePlus v-if="!editingForeignKey" />
            <Edit v-else />
          </el-icon>
          <span style="vertical-align: middle">
            {{ editingForeignKey ? '编辑关联关系' : '新增关联关系' }}
          </span>
        </h4>

        <el-row :gutter="12">
          <!-- 主表 -->
          <el-col :span="5">
            <div style="margin-bottom: 5px">
              <label style="font-size: 12px; font-weight: 600; color: #64748b">
                主表 (Left Table)
              </label>
            </div>
            <el-select
              v-model="newForeignKey.sourceTableName"
              placeholder="请选择表..."
              style="width: 100%"
              @change="handleSourceTableChange"
              clearable
              filterable
            >
              <el-option v-for="table in tableList" :key="table" :label="table" :value="table" />
            </el-select>
          </el-col>

          <!-- 主表字段 -->
          <el-col :span="4">
            <div style="margin-bottom: 5px">
              <label style="font-size: 12px; font-weight: 600; color: #64748b">字段</label>
            </div>
            <el-select
              v-model="newForeignKey.sourceColumnName"
              placeholder="先选表"
              style="width: 100%"
              :disabled="!newForeignKey.sourceTableName"
              clearable
              filterable
            >
              <el-option
                v-for="column in sourceColumnList"
                :key="column"
                :label="column"
                :value="column"
              />
            </el-select>
          </el-col>

          <!-- 关系图标 -->
          <el-col
            :span="1"
            style="
              text-align: center;
              display: flex;
              align-items: flex-end;
              justify-content: center;
              padding-bottom: 8px;
            "
          >
            <el-icon color="#94a3b8" :size="16"><Right /></el-icon>
          </el-col>

          <!-- 关联表 -->
          <el-col :span="5">
            <div style="margin-bottom: 5px">
              <label style="font-size: 12px; font-weight: 600; color: #64748b">
                关联表 (Right Table)
              </label>
            </div>
            <el-select
              v-model="newForeignKey.targetTableName"
              placeholder="请选择表..."
              style="width: 100%"
              @change="handleTargetTableChange"
              clearable
              filterable
            >
              <el-option v-for="table in tableList" :key="table" :label="table" :value="table" />
            </el-select>
          </el-col>

          <!-- 关联表字段 -->
          <el-col :span="4">
            <div style="margin-bottom: 5px">
              <label style="font-size: 12px; font-weight: 600; color: #64748b">字段</label>
            </div>
            <el-select
              v-model="newForeignKey.targetColumnName"
              placeholder="先选表"
              style="width: 100%"
              :disabled="!newForeignKey.targetTableName"
              clearable
              filterable
            >
              <el-option
                v-for="column in targetColumnList"
                :key="column"
                :label="column"
                :value="column"
              />
            </el-select>
          </el-col>

          <!-- 添加/更新按钮 -->
          <el-col :span="5" style="display: flex; align-items: flex-end">
            <el-button @click="saveOrUpdateForeignKey" type="primary" style="width: 100%">
              <el-icon style="margin-right: 4px"><Check /></el-icon>
              {{ editingForeignKey ? '更新' : '添加' }}
            </el-button>
          </el-col>
        </el-row>

        <el-row style="margin-top: 15px" :gutter="12">
          <el-col :span="8">
            <div style="margin-bottom: 5px">
              <label style="font-size: 12px; font-weight: 600; color: #64748b">
                关系类型 (Relation Type)
              </label>
            </div>
            <el-select
              v-model="newForeignKey.relationType"
              placeholder="选择关系类型（可选）"
              clearable
              style="width: 100%"
            >
              <el-option label="1:1 (一对一)" value="1:1" />
              <el-option label="1:N (一对多)" value="1:N" />
              <el-option label="N:1 (多对一)" value="N:1" />
            </el-select>
          </el-col>

          <!-- 描述输入框 -->
          <el-col :span="16">
            <div style="margin-bottom: 5px">
              <label style="font-size: 12px; font-weight: 600; color: #64748b">
                描述 (Description)
              </label>
            </div>
            <el-input
              v-model="newForeignKey.description"
              placeholder="例如 '订单关联用户'，帮助 LLM 理解语义"
              clearable
            />
          </el-col>
        </el-row>
      </div>
    </div>

    <template #footer>
      <div style="text-align: right">
        <el-button @click="foreignKeyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveForeignKeyConfig" :loading="savingForeignKeys">
          保存全部配置
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, Ref, watch } from 'vue';
import {
  Plus,
  UploadFilled,
  Loading,
  FolderOpened,
  Lock,
  Connection,
  Link,
  CirclePlus,
  Check,
  Right,
  Edit,
} from '@element-plus/icons-vue';
import datasourceService from '@/services/datasource';
import { Datasource, AgentDatasource } from '@/services/datasource';
import { ApiResponse } from '@/services/common';
import { ElMessage, ElMessageBox } from 'element-plus';
import agentDatasourceService from '@/services/agentDatasource';
import logicalRelationService, { LogicalRelation } from '@/services/logicalRelation';

export default defineComponent({
  name: 'AgentDataSourceConfig',
  props: {
    agentId: {
      type: Number,
      required: true,
    },
  },
  setup(props) {
    // 当前Agent关联的数据源列表
    const datasource: Ref<Datasource[]> = ref([]);
    const initStatus: Ref<boolean> = ref(false);
    const dialogVisible: Ref<boolean> = ref(false);
    const dialogActiveName: Ref<string> = ref('select');
    // 所有数据源列表
    const allDatasource: Ref<Datasource[]> = ref([]);
    const newDatasource: Ref<Datasource> = ref({ port: 3306 } as Datasource);
    const selectedDatasourceId: Ref<number | null> = ref(null);
    const editDialogVisible: Ref<boolean> = ref(false);
    const editingDatasource: Ref<Datasource> = ref({} as Datasource);

    // PostgreSQL/Oracle 额外的schema字段
    const schemaName: Ref<string> = ref('');
    const schemaNameEdit: Ref<string> = ref('');

    // 数据表管理相关状态
    const tableLists: Ref<Record<number, string[]>> = ref({});
    const selectedTables: Ref<Record<number, string[]>> = ref({});
    const tableLoadingStates: Ref<Record<number, boolean>> = ref({});
    const updateLoadingStates: Ref<Record<number, boolean>> = ref({});
    const agentDatasourceList: Ref<AgentDatasource[]> = ref([]);

    // 逻辑外键管理相关状态
    const foreignKeyDialogVisible: Ref<boolean> = ref(false);
    const currentForeignKeyDatasource: Ref<Datasource | null> = ref(null);
    const foreignKeyList: Ref<LogicalRelation[]> = ref([]);
    const editingForeignKey: Ref<LogicalRelation | null> = ref(null); // 正在编辑的外键
    const newForeignKey: Ref<LogicalRelation> = ref({
      sourceTableName: '',
      sourceColumnName: '',
      targetTableName: '',
      targetColumnName: '',
      relationType: '',
      description: '',
    } as LogicalRelation);
    const tableList: Ref<string[]> = ref([]);
    const sourceColumnList: Ref<string[]> = ref([]);
    const targetColumnList: Ref<string[]> = ref([]);
    const savingForeignKeys: Ref<boolean> = ref(false);

    watch(dialogVisible, newValue => {
      if (newValue) {
        loadAllDatasource();
        newDatasource.value = { port: 3306 } as Datasource;
        schemaName.value = '';
      }
    });

    // 初始化Agent数据源列表
    const loadAgentDatasource = async () => {
      selectedDatasourceId.value = null;
      try {
        const response = await agentDatasourceService.getAgentDatasource(props.agentId);
        agentDatasourceList.value = response || [];
        const agentDatasource: AgentDatasource[] = response || [];
        datasource.value = agentDatasource.map(item => {
          const datasourceItem = { ...item.datasource };
          datasourceItem.status = item.isActive === 1 ? 'active' : 'inactive';

          // 初始化已选择的表
          if (item.selectTables && item.datasource?.id) {
            selectedTables.value[item.datasource.id] = [...item.selectTables];
          }

          return datasourceItem;
        });
      } catch (error) {
        ElMessage.error('加载当前智能体的数据源列表失败');
        console.error('Failed to load datasource:', error);
      }
    };

    const handleSelectDatasourceChange = (value: Datasource) => {
      if (value === null || value === undefined) {
        selectedDatasourceId.value = null;
      } else {
        selectedDatasourceId.value = value.id;
      }
    };

    const loadAllDatasource = async () => {
      try {
        const response = await datasourceService.getAllDatasource();
        allDatasource.value = response || [];
      } catch (error) {
        ElMessage.error('加载所有数据源列表失败');
        console.error('Failed to load all datasource:', error);
      }
    };

    // 初始化Agent数据源
    const initAgentDatasource = async () => {
      initStatus.value = true;
      try {
        try {
          // 获取智能体配置的启用数据源
          const usedDatasource: AgentDatasource =
            await agentDatasourceService.getActiveAgentDatasource(props.agentId);

          if (usedDatasource.datasource == null && usedDatasource.datasourceId == null) {
            ElMessage.warning(
              '当前智能体没有启用的数据源！请添加一个新数据源，或者启用已有的数据源',
            );
            return;
          } else if (
            usedDatasource.selectTables == null ||
            usedDatasource.selectTables.length === 0
          ) {
            ElMessage.warning(
              '当前启用的数据源没有选择相应的数据表！请点击相应数据源左侧按钮，选择相应数据表并更新！',
            );
            return;
          }
        } catch {
          ElMessage.warning('当前智能体没有启用的数据源！请添加一个新数据源，或者启用已有的数据源');
          return;
        }

        const response: ApiResponse<null> = await agentDatasourceService.initSchema(props.agentId);
        if (response.success === undefined || response.success == null || !response.success) {
          ElMessage.error(`初始化数据源失败`);
          throw new Error('初始化数据源失败');
        }

        ElMessage.success('初始化当前智能体的数据源成功');
      } catch (error) {
        ElMessage.error('初始化当前智能体的数据源失败');
        console.error('Failed to init datasource:', error);
      } finally {
        initStatus.value = false;
      }
    };

    // 更改数据源状态
    const changeDatasource = async (row: Datasource, active: boolean) => {
      const datasourceId = row.id;
      try {
        const response: ApiResponse = await agentDatasourceService.toggleDatasourceForAgent(
          props.agentId,
          { datasourceId, isActive: active },
        );
        if (response.success) {
          ElMessage.success('操作成功！');
          row.status = active ? 'active' : 'inactive';
        } else {
          ElMessage.error('操作失败！');
          console.error('Failed to change datasource:', response);
        }
      } catch (error) {
        ElMessage.error('操作失败！');
        console.error('Failed to change datasource:', error);
      }
    };

    // 测试数据源连接
    const testConnection = async (row: Datasource) => {
      const datasourceId = row.id;
      try {
        const response: ApiResponse = await datasourceService.testConnection(datasourceId);
        if (response.success) {
          ElMessage.success('测试连接成功！');
          row.testStatus = 'success';
        } else {
          ElMessage.error('测试连接失败！');
          console.error('Failed to test connection:', response);
          row.testStatus = 'fail';
        }
      } catch (error) {
        ElMessage.error('测试连接失败！');
        console.error('Failed to test connection:', error);
        row.testStatus = 'fail';
      }
    };

    // 移除Agent数据源
    const removeAgentDatasource = async (row: Datasource) => {
      const datasourceId = row.id;

      try {
        await ElMessageBox.confirm('是否要删除当前数据源吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        });
      } catch (error) {
        return;
      }

      try {
        const response: ApiResponse = await agentDatasourceService.removeDatasourceFromAgent(
          props.agentId,
          datasourceId,
        );
        if (response.success) {
          ElMessage.success('移除成功！');
          datasource.value = datasource.value.filter(item => item.id !== datasourceId);
        } else {
          ElMessage.error('移除失败！');
          console.error('Failed to remove datasource:', response);
        }
      } catch (error) {
        ElMessage.error('移除失败！');
        console.error('Failed to remove datasource:', error);
      }
    };

    const addDatasourceToAgent = async (datasourceId: number) => {
      try {
        await agentDatasourceService.addDatasourceToAgent(props.agentId, datasourceId);
        await loadAgentDatasource();
        ElMessage.success('添加数据源成功');
        dialogVisible.value = false;
      } catch (error) {
        ElMessage.error('添加数据源失败');
        console.error('Failed to add datasource:', error);
      }
    };

    const addSelectDatasource = async () => {
      const datasourceId = selectedDatasourceId.value;
      if (datasourceId === null || datasourceId === undefined) {
        ElMessage.warning('请选择一个数据源');
        return;
      }
      await addDatasourceToAgent(datasourceId);
    };

    const validateDatasourceForm = (
      datasourceForm: Datasource,
      needsSchema: boolean = false,
      schemaValue: string = '',
    ): string[] => {
      const errors: string[] = [];

      if (!datasourceForm.name || datasourceForm.name.trim() === '') {
        errors.push('数据源名称不能为空');
      }

      if (!datasourceForm.type) {
        errors.push('请选择数据源类型');
      }

      if (!datasourceForm.host || datasourceForm.host.trim() === '') {
        errors.push('主机地址不能为空');
      }

      if (!datasourceForm.port || datasourceForm.port <= 0 || datasourceForm.port > 65535) {
        errors.push('请输入有效的端口号（1-65535）');
      }

      if (!datasourceForm.databaseName || datasourceForm.databaseName.trim() === '') {
        errors.push('数据库名不能为空');
      }

      if (needsSchema && (!schemaValue || schemaValue.trim() === '')) {
        errors.push('Schema 名不能为空');
      }

      if (!datasourceForm.username || datasourceForm.username.trim() === '') {
        errors.push('用户名不能为空');
      }

      if (!datasourceForm.password || datasourceForm.password.trim() === '') {
        errors.push('密码不能为空');
      }

      return errors;
    };

    const createNewDatasource = async () => {
      const needsSchema =
        newDatasource.value.type === 'postgresql' || newDatasource.value.type === 'oracle';
      const formErrors: string[] = validateDatasourceForm(
        newDatasource.value,
        needsSchema,
        schemaName.value,
      );
      if (formErrors.length > 0) {
        ElMessage.error(formErrors.join('\r\n'));
        return;
      }
      try {
        // 如果是PostgreSQL或Oracle，合并数据库名和schema名
        if (needsSchema && schemaName.value) {
          newDatasource.value.databaseName = `${newDatasource.value.databaseName}|${schemaName.value}`;
        }
        const datasource: Datasource = await datasourceService.createDatasource(
          newDatasource.value,
        );
        const id = datasource.id;
        if (id === null || id === undefined) {
          throw new Error('创建数据源失败');
        }
        await addDatasourceToAgent(id);
      } catch (error) {
        ElMessage.error('创建数据源失败');
        console.error('Failed to create datasource:', error);
      }
      dialogVisible.value = false;
    };
    const editDatasource = (row: Datasource) => {
      editingDatasource.value = JSON.parse(JSON.stringify(row));
      // 如果是PostgreSQL或Oracle，分离数据库名和schema名
      const needsSchema =
        editingDatasource.value.type === 'postgresql' || editingDatasource.value.type === 'oracle';
      if (needsSchema && editingDatasource.value.databaseName) {
        const parts = editingDatasource.value.databaseName.split('|');
        if (parts.length === 2) {
          editingDatasource.value.databaseName = parts[0];
          schemaNameEdit.value = parts[1];
        } else {
          schemaNameEdit.value = '';
        }
      } else {
        schemaNameEdit.value = '';
      }
      editDialogVisible.value = true;
    };

    const saveEditDatasource = async () => {
      const needsSchema =
        editingDatasource.value.type === 'postgresql' || editingDatasource.value.type === 'oracle';
      const formErrors: string[] = validateDatasourceForm(
        editingDatasource.value,
        needsSchema,
        schemaNameEdit.value,
      );
      if (formErrors.length > 0) {
        ElMessage.error(formErrors.join('\n'));
        return;
      }

      try {
        // 如果是PostgreSQL或Oracle，合并数据库名和schema名
        if (needsSchema && schemaNameEdit.value) {
          editingDatasource.value.databaseName = `${editingDatasource.value.databaseName}|${schemaNameEdit.value}`;
        }
        const response: Datasource = await datasourceService.updateDatasource(
          editingDatasource.value.id!,
          editingDatasource.value,
        );
        if (response && response.id) {
          ElMessage.success('修改成功！');
          const index = allDatasource.value.findIndex(
            item => item.id === editingDatasource.value.id,
          );
          if (index >= 0) {
            allDatasource.value[index] = response;
          }
          editDialogVisible.value = false;
        } else {
          ElMessage.error('修改失败！');
          console.error('Failed to update datasource:', response);
        }
      } catch (error) {
        ElMessage.error('修改失败！');
        console.error('Failed to update datasource:', error);
      }
    };

    const deleteDatasource = async (row: Datasource) => {
      const datasourceId = row.id;

      try {
        await ElMessageBox.confirm('删除后无法恢复，确定要删除该数据源吗？', '确认删除', {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'warning',
        });
      } catch (error) {
        return;
      }

      try {
        const response: ApiResponse<void> = await datasourceService.deleteDatasource(datasourceId!);
        if (response.success) {
          ElMessage.success('删除成功！');
          allDatasource.value = allDatasource.value.filter(item => item.id !== datasourceId);
        } else {
          ElMessage.error('删除失败！');
          console.error('Failed to delete datasource:', response);
        }
      } catch (error) {
        ElMessage.error('删除失败！');
        console.error('Failed to delete datasource:', error);
      }
    };

    // 加载数据源的表列表
    const loadDatasourceTables = async (datasource: Datasource) => {
      if (!datasource.id) return;

      tableLoadingStates.value[datasource.id] = true;
      try {
        const tables = await datasourceService.getDatasourceTables(datasource.id);
        tableLists.value[datasource.id] = tables;

        // 如果没有初始化已选择的表，则使用当前已选择的表
        if (!selectedTables.value[datasource.id]) {
          const agentDatasource = agentDatasourceList.value.find(
            item => item.datasource?.id === datasource.id,
          );
          selectedTables.value[datasource.id] = agentDatasource?.selectTables || [];
        }

        ElMessage.success(`成功加载 ${tables.length} 个表`);
      } catch (error) {
        ElMessage.error('加载表列表失败');
        console.error('Failed to load datasource tables:', error);
      } finally {
        tableLoadingStates.value[datasource.id] = false;
      }
    };

    // 更新数据源的表列表
    const updateDatasourceTables = async (datasource: Datasource) => {
      if (!datasource.id) return;

      updateLoadingStates.value[datasource.id] = true;
      try {
        const response = await agentDatasourceService.updateDatasourceTables(
          String(props.agentId),
          {
            datasourceId: datasource.id,
            tables: selectedTables.value[datasource.id] || [],
          },
        );

        if (response.success) {
          ElMessage.success('数据表更新成功');
          // 更新本地存储的已选择表
          const agentDatasource = agentDatasourceList.value.find(
            item => item.datasource?.id === datasource.id,
          );
          if (agentDatasource) {
            agentDatasource.selectTables = [...(selectedTables.value[datasource.id] || [])];
          }
        } else {
          ElMessage.error('数据表更新失败');
        }
      } catch (error) {
        ElMessage.error('数据表更新失败');
        console.error('Failed to update datasource tables:', error);
      } finally {
        updateLoadingStates.value[datasource.id] = false;
      }
    };

    // 全选表
    const selectAllTables = (datasource: Datasource) => {
      if (!datasource.id || !tableLists.value[datasource.id]) return;
      selectedTables.value[datasource.id] = [...tableLists.value[datasource.id]];
    };

    // 清空选择的表
    const clearAllTables = (datasource: Datasource) => {
      if (!datasource.id) return;
      selectedTables.value[datasource.id] = [];
    };

    // 文本截断函数
    const truncateText = (text: string, maxLength: number): string => {
      if (!text || text.length <= maxLength) {
        return text;
      }
      return text.substring(0, maxLength) + '...';
    };

    // 处理表格展开事件
    const handleExpandChange = (row: Datasource, expandedRows: Datasource[]) => {
      // 如果当前行被展开（在expandedRows数组中），则自动加载表列表
      if (expandedRows.includes(row) && row.status === 'active' && row.id) {
        loadDatasourceTables(row);
      }
    };

    onMounted(() => {
      loadAgentDatasource();
    });

    // ==================== 逻辑外键管理功能 ====================

    // 打开逻辑外键配置模态框
    const openForeignKeyDialog = async (datasourceRow: Datasource) => {
      if (!datasourceRow.id) {
        ElMessage.warning('数据源ID不存在');
        return;
      }

      currentForeignKeyDatasource.value = datasourceRow;
      foreignKeyDialogVisible.value = true;

      // 加载表列表
      try {
        tableList.value = await datasourceService.getDatasourceTables(datasourceRow.id);
      } catch (error) {
        ElMessage.error('加载表列表失败');
        console.error('Failed to load table list:', error);
      }

      // 加载现有的逻辑外键
      await loadForeignKeys(datasourceRow.id);

      // 重置表单
      resetForeignKeyForm();
    };

    // 加载逻辑外键列表
    const loadForeignKeys = async (datasourceId: number) => {
      try {
        foreignKeyList.value = await logicalRelationService.getLogicalRelations(datasourceId);
      } catch (error) {
        ElMessage.error('加载逻辑外键列表失败');
        console.error('Failed to load logical relations:', error);
      }
    };

    // 主表选择变化，加载字段列表
    const handleSourceTableChange = async (tableName: string) => {
      if (!tableName || !currentForeignKeyDatasource.value?.id) {
        sourceColumnList.value = [];
        newForeignKey.value.sourceColumnName = '';
        return;
      }

      try {
        sourceColumnList.value = await logicalRelationService.getTableColumns(
          currentForeignKeyDatasource.value.id,
          tableName,
        );
        newForeignKey.value.sourceColumnName = '';
      } catch (error) {
        ElMessage.error('加载字段列表失败');
        console.error('Failed to load source columns:', error);
      }
    };

    // 关联表选择变化，加载字段列表
    const handleTargetTableChange = async (tableName: string) => {
      if (!tableName || !currentForeignKeyDatasource.value?.id) {
        targetColumnList.value = [];
        newForeignKey.value.targetColumnName = '';
        return;
      }

      try {
        targetColumnList.value = await logicalRelationService.getTableColumns(
          currentForeignKeyDatasource.value.id,
          tableName,
        );
        newForeignKey.value.targetColumnName = '';
      } catch (error) {
        ElMessage.error('加载字段列表失败');
        console.error('Failed to load target columns:', error);
      }
    };

    // 编辑逻辑外键
    const editForeignKey = async (foreignKey: LogicalRelation) => {
      editingForeignKey.value = foreignKey;

      // 加载数据到表单
      newForeignKey.value = {
        id: foreignKey.id,
        datasourceId: foreignKey.datasourceId,
        sourceTableName: foreignKey.sourceTableName,
        sourceColumnName: foreignKey.sourceColumnName,
        targetTableName: foreignKey.targetTableName,
        targetColumnName: foreignKey.targetColumnName,
        relationType: foreignKey.relationType || '',
        description: foreignKey.description || '',
      };

      // 加载对应的字段列表
      if (foreignKey.sourceTableName && currentForeignKeyDatasource.value?.id) {
        try {
          sourceColumnList.value = await logicalRelationService.getTableColumns(
            currentForeignKeyDatasource.value.id,
            foreignKey.sourceTableName,
          );
        } catch (error) {
          console.error('Failed to load source columns:', error);
        }
      }

      if (foreignKey.targetTableName && currentForeignKeyDatasource.value?.id) {
        try {
          targetColumnList.value = await logicalRelationService.getTableColumns(
            currentForeignKeyDatasource.value.id,
            foreignKey.targetTableName,
          );
        } catch (error) {
          console.error('Failed to load target columns:', error);
        }
      }

      ElMessage.info('正在编辑逻辑外键，修改后点击"更新"按钮');
    };

    // 添加或更新逻辑外键
    const saveOrUpdateForeignKey = async () => {
      // 表单验证
      if (
        !newForeignKey.value.sourceTableName ||
        !newForeignKey.value.sourceColumnName ||
        !newForeignKey.value.targetTableName ||
        !newForeignKey.value.targetColumnName
      ) {
        ElMessage.warning('请完整填写主表、字段、关联表和字段');
        return;
      }

      // 检查是否重复（编辑模式时排除自己）
      const isDuplicate = foreignKeyList.value.some(
        fk =>
          fk.id !== editingForeignKey.value?.id &&
          fk.sourceTableName === newForeignKey.value.sourceTableName &&
          fk.sourceColumnName === newForeignKey.value.sourceColumnName &&
          fk.targetTableName === newForeignKey.value.targetTableName &&
          fk.targetColumnName === newForeignKey.value.targetColumnName,
      );

      if (isDuplicate) {
        ElMessage.warning('该逻辑外键关系已存在');
        return;
      }

      // 判断是编辑还是新增
      if (editingForeignKey.value && editingForeignKey.value.id) {
        // 更新模式
        const index = foreignKeyList.value.findIndex(fk => fk.id === editingForeignKey.value!.id);
        if (index !== -1) {
          foreignKeyList.value[index] = {
            ...foreignKeyList.value[index],
            sourceTableName: newForeignKey.value.sourceTableName,
            sourceColumnName: newForeignKey.value.sourceColumnName,
            targetTableName: newForeignKey.value.targetTableName,
            targetColumnName: newForeignKey.value.targetColumnName,
            relationType: newForeignKey.value.relationType || '',
            description: newForeignKey.value.description || '',
          };
        }
        ElMessage.success('更新成功，请点击"保存全部配置"以保存到数据库');
      } else {
        // 添加模式
        foreignKeyList.value.push({
          sourceTableName: newForeignKey.value.sourceTableName,
          sourceColumnName: newForeignKey.value.sourceColumnName,
          targetTableName: newForeignKey.value.targetTableName,
          targetColumnName: newForeignKey.value.targetColumnName,
          relationType: newForeignKey.value.relationType || '',
          description: newForeignKey.value.description || '',
        });
        ElMessage.success('添加成功，请点击"保存全部配置"以保存到数据库');
      }

      // 重置表单
      resetForeignKeyForm();
    };

    // 删除逻辑外键
    const deleteForeignKey = async (foreignKey: LogicalRelation, index: number) => {
      try {
        await ElMessageBox.confirm('确定要删除这条逻辑外键关系吗？', '确认删除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        });

        foreignKeyList.value.splice(index, 1);
        ElMessage.success('删除成功，请点击"保存全部配置"以保存到数据库');
      } catch {
        // 用户取消操作
      }
    };

    // 保存逻辑外键配置
    const saveForeignKeyConfig = async () => {
      if (!currentForeignKeyDatasource.value?.id) {
        ElMessage.error('数据源ID不存在');
        return;
      }

      savingForeignKeys.value = true;
      try {
        const response = await logicalRelationService.saveLogicalRelations(
          currentForeignKeyDatasource.value.id,
          foreignKeyList.value,
        );

        if (response.success) {
          ElMessage.success('保存成功');
          foreignKeyDialogVisible.value = false;
        } else {
          ElMessage.error('保存失败');
        }
      } catch (error) {
        ElMessage.error('保存失败');
        console.error('Failed to save logical relations:', error);
      } finally {
        savingForeignKeys.value = false;
      }
    };

    // 重置逻辑外键表单
    const resetForeignKeyForm = () => {
      editingForeignKey.value = null; // 重置编辑状态
      newForeignKey.value = {
        sourceTableName: '',
        sourceColumnName: '',
        targetTableName: '',
        targetColumnName: '',
        relationType: '',
        description: '',
      } as LogicalRelation;
      sourceColumnList.value = [];
      targetColumnList.value = [];
    };

    return {
      props,
      Plus,
      UploadFilled,
      Loading,
      FolderOpened,
      Lock,
      datasource,
      initStatus,
      dialogVisible,
      dialogActiveName,
      allDatasource,
      newDatasource,
      editDialogVisible,
      editingDatasource,
      tableLists,
      selectedTables,
      tableLoadingStates,
      updateLoadingStates,
      initAgentDatasource,
      changeDatasource,
      testConnection,
      removeAgentDatasource,
      loadAllDatasource,
      addSelectDatasource,
      createNewDatasource,
      handleSelectDatasourceChange,
      editDatasource,
      saveEditDatasource,
      deleteDatasource,
      loadDatasourceTables,
      updateDatasourceTables,
      selectAllTables,
      clearAllTables,
      truncateText,
      handleExpandChange,
      // PostgreSQL/Oracle Schema字段
      schemaName,
      schemaNameEdit,
      // 逻辑外键管理
      Connection,
      Link,
      CirclePlus,
      Check,
      Right,
      Edit,
      foreignKeyDialogVisible,
      currentForeignKeyDatasource,
      foreignKeyList,
      newForeignKey,
      tableList,
      sourceColumnList,
      targetColumnList,
      savingForeignKeys,
      openForeignKeyDialog,
      handleSourceTableChange,
      handleTargetTableChange,
      editForeignKey,
      saveOrUpdateForeignKey,
      deleteForeignKey,
      saveForeignKeyConfig,
      editingForeignKey,
    };
  },
});
</script>

<style scoped>
.datasource-config {
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
  gap: 0.75rem;
}

.action-btn {
  font-weight: 500;
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

.connection-url-text {
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  font-size: 0.85rem;
  color: #64748b;
}

.form-item {
  margin-bottom: 1.25rem;
}

.form-item label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  font-size: 0.9rem;
  color: #334155;
}

.custom-tabs :deep(.el-tabs__header) {
  background: #f8fafc;
}
</style>
