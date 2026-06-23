$BASE_URL = "http://localhost:8080/api"
$results = @()

function Test-Api {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [string]$Body = $null
    )
    
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "测试: $Name" -ForegroundColor Yellow
    Write-Host "方法: $Method  URL: $Url" -ForegroundColor Gray
    
    try {
        $headers = @{ "Content-Type" = "application/json" }
        if ($script:token) {
            $headers["Authorization"] = "Bearer $($script:token)"
        }
        
        $params = @{
            Uri = $Url
            Method = $Method
            Headers = $headers
            UseBasicParsing = $true
        }
        
        if ($Body) {
            $params.Body = $Body
            Write-Host "请求体: $Body" -ForegroundColor Gray
        }
        
        $response = Invoke-WebRequest @params
        $statusCode = $response.StatusCode
        $responseBody = $response.Content
        
        Write-Host "状态码: $statusCode" -ForegroundColor Green
        Write-Host "响应: $($responseBody.Substring(0, [Math]::Min(500, $responseBody.Length)))" -ForegroundColor White
        
        $script:results += [PSCustomObject]@{
            Name = $Name
            Status = "成功"
            StatusCode = $statusCode
        }
        
        return $responseBody
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "状态码: $statusCode" -ForegroundColor Red
        
        $script:results += [PSCustomObject]@{
            Name = $Name
            Status = "失败"
            StatusCode = $statusCode
        }
        
        return $null
    }
}

# ==========================================
# 1. 认证接口测试
# ==========================================
Write-Host "`n`n============================================" -ForegroundColor Magenta
Write-Host "       一、认证接口测试" -ForegroundColor Magenta
Write-Host "============================================" -ForegroundColor Magenta

# 1.1 管理员登录
$loginResult = Test-Api -Name "管理员登录" -Method "POST" -Url "$BASE_URL/auth/login" -Body '{"username":"admin","password":"admin123"}'
if ($loginResult) {
    $loginData = ($loginResult | ConvertFrom-Json).data
    $script:token = $loginData.token
    Write-Host "获取到Token: $($script:token)" -ForegroundColor Green
}

# 1.2 销售员登录
Test-Api -Name "销售员登录(张三)" -Method "POST" -Url "$BASE_URL/auth/login" -Body '{"username":"sales001","password":"sales123"}'

# 1.3 错误密码登录
Test-Api -Name "错误密码登录" -Method "POST" -Url "$BASE_URL/auth/login" -Body '{"username":"admin","password":"wrongpassword"}'

# 1.4 获取当前用户
Test-Api -Name "获取当前用户信息" -Method "GET" -Url "$BASE_URL/auth/me"

# ==========================================
# 2. 用户管理接口测试
# ==========================================
Write-Host "`n`n============================================" -ForegroundColor Magenta
Write-Host "       二、用户管理接口测试" -ForegroundColor Magenta
Write-Host "============================================" -ForegroundColor Magenta

# 2.1 获取用户列表
Test-Api -Name "获取用户列表" -Method "GET" -Url "$BASE_URL/users"

# 2.2 获取单个用户
Test-Api -Name "获取单个用户(ID=1)" -Method "GET" -Url "$BASE_URL/users/1"

# 2.3 创建新用户
Test-Api -Name "创建新用户" -Method "POST" -Url "$BASE_URL/users" -Body '{"username":"testuser","password":"test123","role":"SALES","name":"测试用户","phone":"13900139000","email":"test@company.com"}'

# 2.4 更新用户
Test-Api -Name "更新用户信息" -Method "PUT" -Url "$BASE_URL/users/7" -Body '{"name":"测试用户-已更新","phone":"13900139001"}'

# ==========================================
# 3. 商品管理接口测试
# ==========================================
Write-Host "`n`n============================================" -ForegroundColor Magenta
Write-Host "       三、商品管理接口测试" -ForegroundColor Magenta
Write-Host "============================================" -ForegroundColor Magenta

# 3.1 获取商品列表
Test-Api -Name "获取商品列表(分页)" -Method "GET" -Url "$BASE_URL/products?page=1&size=5"

# 3.2 获取单个商品
Test-Api -Name "获取单个商品(ID=1)" -Method "GET" -Url "$BASE_URL/products/1"

# 3.3 搜索商品
Test-Api -Name "搜索商品(关键词:iPhone)" -Method "GET" -Url "$BASE_URL/products/search?keyword=iPhone"

# 3.4 获取热销商品
Test-Api -Name "获取热销商品TOP5" -Method "GET" -Url "$BASE_URL/products/hot?limit=5"

# 3.5 获取低库存商品
Test-Api -Name "获取低库存商品" -Method "GET" -Url "$BASE_URL/products/low-stock?threshold=20"

# 3.6 价格区间查询
Test-Api -Name "价格区间查询(5000-10000)" -Method "GET" -Url "$BASE_URL/products/price-range?minPrice=5000&maxPrice=10000"

# 3.7 创建新商品
Test-Api -Name "创建新商品" -Method "POST" -Url "$BASE_URL/products" -Body '{"name":"测试商品","description":"这是一个测试商品","price":99.99,"stockQuantity":100}'

# 3.8 更新商品
Test-Api -Name "更新商品信息" -Method "PUT" -Url "$BASE_URL/products/13" -Body '{"name":"测试商品-已更新","price":199.99}'

# ==========================================
# 4. 订单管理接口测试
# ==========================================
Write-Host "`n`n============================================" -ForegroundColor Magenta
Write-Host "       四、订单管理接口测试" -ForegroundColor Magenta
Write-Host "============================================" -ForegroundColor Magenta

# 4.1 获取订单列表
Test-Api -Name "获取订单列表(分页)" -Method "GET" -Url "$BASE_URL/orders?page=1&size=5"

# 4.2 获取单个订单
Test-Api -Name "获取单个订单(ID=1)" -Method "GET" -Url "$BASE_URL/orders/1"

# 4.3 按状态筛选订单
Test-Api -Name "按状态筛选(COMPLETED)" -Method "GET" -Url "$BASE_URL/orders?status=COMPLETED"

# 4.4 按销售人员筛选
Test-Api -Name "按销售人员筛选(ID=2)" -Method "GET" -Url "$BASE_URL/orders?salespersonId=2"

# 4.5 获取订单状态统计
Test-Api -Name "获取订单状态统计" -Method "GET" -Url "$BASE_URL/orders/status-count"

# 4.6 创建新订单
Test-Api -Name "创建新订单" -Method "POST" -Url "$BASE_URL/orders" -Body '{"customerName":"测试客户有限公司","salespersonId":2,"items":[{"productId":1,"quantity":2,"unitPrice":8999.00},{"productId":3,"quantity":5,"unitPrice":1899.00}]}'

# 4.7 更新订单状态
Test-Api -Name "更新订单状态为IN_PROGRESS" -Method "PUT" -Url "$BASE_URL/orders/16/status" -Body '{"status":"IN_PROGRESS"}'

# 4.8 完成订单
Test-Api -Name "完成订单" -Method "POST" -Url "$BASE_URL/orders/16/complete"

# 4.9 取消订单（用一个新订单测试）
Test-Api -Name "创建待取消订单" -Method "POST" -Url "$BASE_URL/orders" -Body '{"customerName":"取消测试客户","salespersonId":3,"items":[{"productId":5,"quantity":1,"unitPrice":3299.00}]}'
Test-Api -Name "取消订单" -Method "POST" -Url "$BASE_URL/orders/17/cancel" -Body '{"reason":"客户要求取消"}'

# ==========================================
# 5. 总览/统计接口测试
# ==========================================
Write-Host "`n`n============================================" -ForegroundColor Magenta
Write-Host "       五、总览/统计接口测试" -ForegroundColor Magenta
Write-Host "============================================" -ForegroundColor Magenta

# 5.1 获取统计数据
Test-Api -Name "获取总览统计" -Method "GET" -Url "$BASE_URL/overview/statistics"

# 5.2 获取销售排行
Test-Api -Name "获取销售排行榜" -Method "GET" -Url "$BASE_URL/overview/sales-ranking"

# 5.3 获取热销商品排行
Test-Api -Name "获取热销商品排行" -Method "GET" -Url "$BASE_URL/overview/product-ranking"

# 5.4 获取销售趋势
Test-Api -Name "获取销售趋势(近7天)" -Method "GET" -Url "$BASE_URL/overview/sales-trend?days=7"

# 5.5 获取订单状态分布
Test-Api -Name "获取订单状态分布" -Method "GET" -Url "$BASE_URL/overview/order-status"

# ==========================================
# 6. AI智能问答接口测试
# ==========================================
Write-Host "`n`n============================================" -ForegroundColor Magenta
Write-Host "       六、AI智能问答接口测试" -ForegroundColor Magenta
Write-Host "============================================" -ForegroundColor Magenta

# 6.1 获取支持的意图列表
Test-Api -Name "获取支持的意图列表" -Method "GET" -Url "$BASE_URL/ai/intents"

# 6.2 AI聊天-销售查询
Test-Api -Name "AI聊天-销售查询" -Method "POST" -Url "$BASE_URL/ai/chat" -Body '{"question":"本月销售情况如何？","conversationId":"test-conv-001"}'

# 6.3 AI聊天-商品查询
Test-Api -Name "AI聊天-商品查询" -Method "POST" -Url "$BASE_URL/ai/chat" -Body '{"question":"哪些商品卖得最好？","conversationId":"test-conv-002"}'

# 6.4 AI聊天-订单查询
Test-Api -Name "AI聊天-订单查询" -Method "POST" -Url "$BASE_URL/ai/chat" -Body '{"question":"最近的订单情况怎么样？","conversationId":"test-conv-003"}'

# 6.5 AI聊天-员工查询
Test-Api -Name "AI聊天-员工查询" -Method "POST" -Url "$BASE_URL/ai/chat" -Body '{"question":"销售团队有哪些人？","conversationId":"test-conv-004"}'

# 6.6 AI聊天-统计分析
Test-Api -Name "AI聊天-统计分析" -Method "POST" -Url "$BASE_URL/ai/chat" -Body '{"question":"帮我统计一下总销售额","conversationId":"test-conv-005"}'

# 6.7 获取对话历史
Test-Api -Name "获取对话历史列表" -Method "GET" -Url "$BASE_URL/ai/conversations"

# 6.8 获取单个对话详情
Test-Api -Name "获取对话详情(test-conv-001)" -Method "GET" -Url "$BASE_URL/ai/conversation/test-conv-001"

# 6.9 AI流式聊天测试
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "测试: AI流式聊天(SSE)" -ForegroundColor Yellow
Write-Host "方法: POST  URL: $BASE_URL/ai/chat/stream" -ForegroundColor Gray
try {
    $headers = @{ "Content-Type" = "application/json"; "Accept" = "text/event-stream" }
    $body = '{"question":"分析一下热销商品的趋势","conversationId":"test-stream-001"}'
    
    $response = Invoke-WebRequest -Uri "$BASE_URL/ai/chat/stream" -Method POST -Headers $headers -Body $body -UseBasicParsing -TimeoutSec 30
    $responseContent = $response.Content
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "SSE响应(前500字符): $($responseContent.Substring(0, [Math]::Min(500, $responseContent.Length)))" -ForegroundColor White
    
    $script:results += [PSCustomObject]@{ Name = "AI流式聊天(SSE)"; Status = "成功"; StatusCode = $response.StatusCode }
}
catch {
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
    $script:results += [PSCustomObject]@{ Name = "AI流式聊天(SSE)"; Status = "失败"; StatusCode = "N/A" }
}

# 6.10 数据分析接口
Test-Api -Name "数据分析接口" -Method "POST" -Url "$BASE_URL/ai/analyze" -Body '{"question":"分析销售数据","data":[{"product":"iPhone","sales":100},{"product":"MacBook","sales":50}]}'

# 6.11 清除对话历史
Test-Api -Name "清除对话历史(test-conv-001)" -Method "DELETE" -Url "$BASE_URL/ai/conversation/test-conv-001"

# ==========================================
# 7. 文件上传接口测试
# ==========================================
Write-Host "`n`n============================================" -ForegroundColor Magenta
Write-Host "       七、文件上传接口测试" -ForegroundColor Magenta
Write-Host "============================================" -ForegroundColor Magenta

# 7.1 获取Markdown文档列表
Test-Api -Name "获取Markdown文档列表" -Method "GET" -Url "$BASE_URL/markdown-documents"

# ==========================================
# 8. PDF导出接口测试
# ==========================================
Write-Host "`n`n============================================" -ForegroundColor Magenta
Write-Host "       八、PDF导出接口测试" -ForegroundColor Magenta
Write-Host "============================================" -ForegroundColor Magenta

# 8.1 导出订单PDF
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "测试: 导出订单PDF" -ForegroundColor Yellow
Write-Host "方法: GET  URL: $BASE_URL/orders/1/export-pdf" -ForegroundColor Gray
try {
    $headers = @{}
    if ($script:token) { $headers["Authorization"] = "Bearer $($script:token)" }
    
    $response = Invoke-WebRequest -Uri "$BASE_URL/orders/1/export-pdf" -Method GET -Headers $headers -UseBasicParsing
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "文件大小: $($response.Content.Length) bytes" -ForegroundColor White
    Write-Host "Content-Type: $($response.Headers['Content-Type'])" -ForegroundColor White
    
    $script:results += [PSCustomObject]@{ Name = "导出订单PDF"; Status = "成功"; StatusCode = $response.StatusCode }
}
catch {
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
    $script:results += [PSCustomObject]@{ Name = "导出订单PDF"; Status = "失败"; StatusCode = "N/A" }
}

# ==========================================
# 9. 删除测试（清理数据）
# ==========================================
Write-Host "`n`n============================================" -ForegroundColor Magenta
Write-Host "       九、删除测试" -ForegroundColor Magenta
Write-Host "============================================" -ForegroundColor Magenta

# 9.1 删除测试商品
Test-Api -Name "删除测试商品" -Method "DELETE" -Url "$BASE_URL/products/13"

# 9.2 删除测试用户
Test-Api -Name "删除测试用户" -Method "DELETE" -Url "$BASE_URL/users/7"

# ==========================================
# 测试结果汇总
# ==========================================
Write-Host "`n`n" -NoNewline
Write-Host "============================================================" -ForegroundColor Magenta
Write-Host "                    测试结果汇总" -ForegroundColor Magenta
Write-Host "============================================================" -ForegroundColor Magenta

$successCount = ($script:results | Where-Object { $_.Status -eq "成功" }).Count
$failCount = ($script:results | Where-Object { $_.Status -eq "失败" }).Count
$totalCount = $script:results.Count

Write-Host "`n"
$script:results | ForEach-Object {
    $color = if ($_.Status -eq "成功") { "Green" } else { "Red" }
    $statusIcon = if ($_.Status -eq "成功") { "[PASS]" } else { "[FAIL]" }
    Write-Host ("  {0,-40} {1} {2}" -f $_.Name, $statusIcon, $_.StatusCode) -ForegroundColor $color
}

Write-Host "`n"
Write-Host ("  总计: {0}  |  成功: {1}  |  失败: {2}" -f $totalCount, $successCount, $failCount) -ForegroundColor $(if ($failCount -eq 0) { "Green" } else { "Yellow" })
Write-Host "`n============================================================" -ForegroundColor Magenta
