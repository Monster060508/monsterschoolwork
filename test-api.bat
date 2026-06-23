@echo off
echo ========================================
echo 企业销售管理系统 API 测试脚本
echo ========================================
echo.

set BASE_URL=http://localhost:8080/api

echo 1. 测试登录接口...
echo POST %BASE_URL%/auth/login
echo {"username":"admin","password":"admin123"}
echo.
curl -X POST %BASE_URL%/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
echo.
echo.

echo 2. 测试获取用户列表...
echo GET %BASE_URL%/users
echo.
curl -X GET %BASE_URL%/users
echo.
echo.

echo 3. 测试获取商品列表...
echo GET %BASE_URL%/products
echo.
curl -X GET %BASE_URL%/products
echo.
echo.

echo 4. 测试获取订单列表...
echo GET %BASE_URL%/orders
echo.
curl -X GET %BASE_URL%/orders
echo.
echo.

echo 5. 测试获取总览统计...
echo GET %BASE_URL%/overview/statistics
echo.
curl -X GET %BASE_URL%/overview/statistics
echo.
echo.

echo 6. 测试AI聊天（非流式）...
echo POST %BASE_URL%/ai/chat
echo {"question":"本月销售情况如何？","conversationId":"test-001"}
echo.
curl -X POST %BASE_URL%/ai/chat -H "Content-Type: application/json" -d "{\"question\":\"本月销售情况如何？\",\"conversationId\":\"test-001\"}"
echo.
echo.

echo 7. 测试AI流式聊天...
echo POST %BASE_URL%/ai/chat/stream
echo {"question":"帮我分析一下热销商品","conversationId":"test-002"}
echo.
curl -X POST %BASE_URL%/ai/chat/stream -H "Content-Type: application/json" -d "{\"question\":\"帮我分析一下热销商品\",\"conversationId\":\"test-002\"}" --no-buffer
echo.
echo.

echo 8. 测试获取对话历史...
echo GET %BASE_URL%/ai/conversations
echo.
curl -X GET %BASE_URL%/ai/conversations
echo.
echo.

echo 9. 测试获取订单状态统计...
echo GET %BASE_URL%/orders/status-count
echo.
curl -X GET %BASE_URL%/orders/status-count
echo.
echo.

echo 10. 测试获取销售排行榜...
echo GET %BASE_URL%/overview/sales-ranking
echo.
curl -X GET %BASE_URL%/overview/sales-ranking
echo.
echo.

echo ========================================
echo 测试完成！
echo ========================================
pause