# 问题诊断和解决方案

## 问题现象
个人中心更新用户名和修改密码时出现500错误，错误信息显示"No static resource api/auth/change-password"

## 根本原因
从错误信息判断，有以下几种可能：

### 1. 浏览器访问的URL不正确 ⭐ 最可能
**症状**：错误信息显示"No static resource"
**原因**：你可能直接访问了后端端口8065，而不是通过前端开发服务器3000端口

**解决方案**：
1. 打开浏览器，确保访问的URL是：
   - ✅ `http://localhost:3000` 或 `http://127.0.0.1:3000`
   - ❌ 不是 `http://localhost:8065` 或其他端口

2. 如果当前URL不对，请：
   - 关闭当前浏览器标签页
   - 打开新标签页，访问 `http://localhost:3000`
   - 重新登录并测试

### 2. 前端开发服务器需要重启
**症状**：代理配置没有生效
**原因**：Vite开发服务器可能需要重启才能应用最新配置

**解决方案**：
```bash
# 在 data-agent-frontend 目录下
cd data-agent-frontend

# 停止当前运行的开发服务器（Ctrl+C）

# 重新启动
npm run dev
```

### 3. 后端服务需要重启
**症状**：JWT拦截器的修复没有生效
**原因**：后端代码修改后需要重新编译和启动

**解决方案**：
```bash
# 在 data-agent-management 目录下
cd data-agent-management

# 停止当前运行的后端服务

# 重新编译和启动
mvn clean install -DskipTests
mvn spring-boot:run
```

## 验证步骤

### 1. 验证后端服务
打开PowerShell，运行：
```powershell
# 测试后端接口（应该返回401）
try { 
    Invoke-RestMethod -Uri "http://127.0.0.1:8065/api/auth/change-password" `
        -Method POST `
        -Headers @{"Authorization"="Bearer invalid.token"; "Content-Type"="application/json"} `
        -Body '{"currentPassword":"old","newPassword":"new123","confirmPassword":"new123"}' 
} catch { 
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)" 
}
```
**预期结果**：Status: 401

### 2. 验证前端代理
打开PowerShell，运行：
```powershell
# 测试前端代理（应该返回401）
try { 
    Invoke-RestMethod -Uri "http://127.0.0.1:3000/api/auth/change-password" `
        -Method POST `
        -Headers @{"Authorization"="Bearer invalid.token"; "Content-Type"="application/json"} `
        -Body '{"currentPassword":"old","newPassword":"new123","confirmPassword":"new123"}' 
} catch { 
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)" 
}
```
**预期结果**：Status: 401

### 3. 验证浏览器访问
1. 打开浏览器开发者工具（F12）
2. 切换到Network标签页
3. 访问 `http://localhost:3000/profile`
4. 尝试修改昵称或密码
5. 查看Network标签页中的请求：
   - Request URL应该是：`http://localhost:3000/api/auth/profile` 或 `http://localhost:3000/api/auth/change-password`
   - 不应该是：`http://localhost:8065/...`

## 已完成的修复

✅ 1. 完善了JWT拦截器的异常处理
   - 添加了对ExpiredJwtException、MalformedJwtException、SignatureException的具体处理
   - 所有JWT异常现在都返回401而不是500

✅ 2. 改进了全局异常处理器
   - 添加了JWT异常的专门处理方法
   - 提供了友好的错误信息

✅ 3. 前端axios拦截器配置正确
   - 会自动处理401错误
   - 自动跳转到登录页面

## 下一步

请按照上面的"解决方案"部分操作，然后告诉我：
1. 你浏览器地址栏中的URL是什么？
2. 验证步骤的结果如何？
3. 是否还有错误？如果有，请提供完整的错误信息。