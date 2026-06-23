# API文档

## 概述
本文档详细说明企业销售管理系统的后端API接口，包括接口地址、请求方式、参数说明、返回格式等。

## 基础信息
- **基础路径**: `/api`
- **请求方式**: RESTful API
- **数据格式**: JSON
- **字符编码**: UTF-8
- **认证方式**: JWT Token

## JWT认证说明

### Token获取
通过登录接口 `POST /api/auth/login` 获取JWT Token。

### Token使用
在需要认证的接口请求头中添加：
```
Authorization: Bearer {your_jwt_token}
```

### Token有效期
- 默认有效期：24小时
- 过期后需要重新登录

### Token结构
JWT Token包含以下Claims：
- `userId`: 用户ID
- `username`: 用户名
- `role`: 用户角色 (ADMIN/SALES)
- `sub`: 主题 (用户名)
- `iat`: 签发时间
- `exp`: 过期时间

## 通用响应格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1690000000000
}
```

**响应状态码说明：**
- `200`: 操作成功
- `400`: 请求参数错误
- `401`: 未认证
- `403`: 权限不足
- `404`: 资源不存在
- `500`: 服务器内部错误

## 认证接口

### 1. 用户登录
**接口地址**: `POST /api/auth/login`

**请求参数**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "name": "管理员",
      "role": "ADMIN",
      "photoUrl": "https://oss.example.com/admin.jpg"
    }
  }
}
```

### 2. 用户登出
**接口地址**: `POST /api/auth/logout`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

### 3. 获取当前用户信息
**接口地址**: `GET /api/auth/me`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "管理员",
    "role": "ADMIN",
    "photoUrl": "https://oss.example.com/admin.jpg"
  }
}
```

## 用户管理接口

### 1. 获取用户列表
**接口地址**: `GET /api/users`

**请求参数**:
- `page`: 页码（默认1）
- `size`: 每页数量（默认10）
- `keyword`: 搜索关键词（可选）
- `role`: 角色筛选（可选）

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "admin",
        "name": "管理员",
        "role": "ADMIN",
        "photoUrl": "https://oss.example.com/admin.jpg",
        "createTime": "2024-01-01 10:00:00"
      }
    ],
    "total": 10,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

### 2. 获取用户详情
**接口地址**: `GET /api/users/{id}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "管理员",
    "role": "ADMIN",
    "photoUrl": "https://oss.example.com/admin.jpg",
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-01 10:00:00"
  }
}
```

### 3. 创建用户
**接口地址**: `POST /api/users`

**请求头**:
```
Authorization: Bearer {token}
```

**请求参数**:
```json
{
  "username": "sales1",
  "password": "sales123",
  "name": "张三",
  "role": "SALES",
  "photoUrl": "https://oss.example.com/sales1.jpg"
}
```

**参数验证规则**:
| 参数 | 类型 | 必填 | 规则 |
|------|------|------|------|
| username | String | 是 | 长度3-50个字符 |
| password | String | 是 | 长度6-100个字符 |
| name | String | 是 | 长度不超过50个字符 |
| role | Enum | 是 | ADMIN(管理员) / SALES(销售) |
| photoUrl | String | 否 | 头像URL |

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 2,
    "username": "sales1",
    "name": "张三",
    "role": "SALES",
    "photoUrl": "https://oss.example.com/sales1.jpg",
    "createTime": "2024-01-01 10:00:00"
  }
}
```

**验证失败响应示例**:
```json
{
  "code": 400,
  "message": "参数验证失败",
  "data": {
    "username": "用户名长度必须在3-50个字符之间",
    "password": "密码长度必须在6-100个字符之间"
  }
}
```

### 4. 更新用户
**接口地址**: `PUT /api/users/{id}`

**请求头**:
```
Authorization: Bearer {token}
```

**请求参数**:
```json
{
  "name": "张三（已更新）",
  "role": "SALES",
  "photoUrl": "https://oss.example.com/sales1_updated.jpg"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 2,
    "username": "sales1",
    "name": "张三（已更新）",
    "role": "SALES",
    "photoUrl": "https://oss.example.com/sales1_updated.jpg",
    "updateTime": "2024-01-01 12:00:00"
  }
}
```

### 5. 删除用户
**接口地址**: `DELETE /api/users/{id}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 6. 上传用户照片
**接口地址**: `POST /api/users/upload-photo`

**请求头**:
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**请求参数**:
- `file`: 图片文件

**响应示例**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "https://oss-cn-hangzhou.aliyuncs.com/monster060508/user/1234567890.jpg"
  }
}
```

## 商品管理接口

### 1. 获取商品列表
**接口地址**: `GET /api/products`

**请求参数**:
- `page`: 页码（默认1）
- `size`: 每页数量（默认10）
- `keyword`: 搜索关键词（可选）
- `category`: 商品分类（可选）

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "iPhone 15",
        "description": "苹果最新款手机",
        "price": 7999.00,
        "stockQuantity": 100,
        "imageUrl": "https://oss.example.com/iphone15.jpg",
        "createTime": "2024-01-01 10:00:00"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  }
}
```

### 2. 获取商品详情
**接口地址**: `GET /api/products/{id}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "name": "iPhone 15",
    "description": "苹果最新款手机",
    "price": 7999.00,
    "stockQuantity": 100,
    "imageUrl": "https://oss.example.com/iphone15.jpg",
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-01 10:00:00"
  }
}
```

### 3. 创建商品
**接口地址**: `POST /api/products`

**请求头**:
```
Authorization: Bearer {token}
```

**请求参数**:
```json
{
  "name": "iPhone 15",
  "description": "苹果最新款手机",
  "price": 7999.00,
  "stockQuantity": 100,
  "imageUrl": "https://oss.example.com/iphone15.jpg"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "name": "iPhone 15",
    "description": "苹果最新款手机",
    "price": 7999.00,
    "stockQuantity": 100,
    "imageUrl": "https://oss.example.com/iphone15.jpg",
    "createTime": "2024-01-01 10:00:00"
  }
}
```

### 4. 更新商品
**接口地址**: `PUT /api/products/{id}`

**请求头**:
```
Authorization: Bearer {token}
```

**请求参数**:
```json
{
  "name": "iPhone 15 Pro",
  "description": "苹果最新款专业版手机",
  "price": 8999.00,
  "stockQuantity": 80,
  "imageUrl": "https://oss.example.com/iphone15pro.jpg"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "name": "iPhone 15 Pro",
    "description": "苹果最新款专业版手机",
    "price": 8999.00,
    "stockQuantity": 80,
    "imageUrl": "https://oss.example.com/iphone15pro.jpg",
    "updateTime": "2024-01-01 12:00:00"
  }
}
```

### 5. 删除商品
**接口地址**: `DELETE /api/products/{id}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 6. 上传商品图片
**接口地址**: `POST /api/products/upload-image`

**请求头**:
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**请求参数**:
- `file`: 图片文件

**响应示例**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "https://oss-cn-hangzhou.aliyuncs.com/monster060508/product/1234567890.jpg"
  }
}
```

## 订单管理接口

### 1. 获取订单列表
**接口地址**: `GET /api/orders`

**请求参数**:
- `page`: 页码（默认1）
- `size`: 每页数量（默认10）
- `status`: 订单状态筛选（可选）
- `salespersonId`: 销售人员ID筛选（可选）
- `startDate`: 开始日期（可选）
- `endDate`: 结束日期（可选）

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "records": [
      {
        "id": 1,
        "orderNo": "ORD20240101001",
        "customerName": "北京科技有限公司",
        "totalAmount": 15998.00,
        "status": "PENDING",
        "salespersonId": 2,
        "salespersonName": "张三",
        "createTime": "2024-01-01 10:00:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

### 2. 获取订单详情
**接口地址**: `GET /api/orders/{id}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "orderNo": "ORD20240101001",
    "customerName": "北京科技有限公司",
    "totalAmount": 15998.00,
    "status": "PENDING",
    "salespersonId": 2,
    "salespersonName": "张三",
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "iPhone 15",
        "quantity": 2,
        "unitPrice": 7999.00
      }
    ],
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-01 10:00:00"
  }
}
```

### 3. 创建订单
**接口地址**: `POST /api/orders`

**请求头**:
```
Authorization: Bearer {token}
```

**请求参数**:
```json
{
  "customerName": "北京科技有限公司",
  "salespersonId": 2,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 3,
      "quantity": 1
    }
  ]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "orderNo": "ORD20240101001",
    "customerName": "北京科技有限公司",
    "totalAmount": 15998.00,
    "status": "PENDING",
    "salespersonId": 2,
    "salespersonName": "张三",
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "iPhone 15",
        "quantity": 2,
        "unitPrice": 7999.00
      }
    ],
    "createTime": "2024-01-01 10:00:00"
  }
}
```

### 4. 更新订单状态
**接口地址**: `PUT /api/orders/{id}/status`

**请求头**:
```
Authorization: Bearer {token}
```

**请求参数**:
```json
{
  "status": "IN_PROGRESS"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "状态更新成功",
  "data": {
    "id": 1,
    "orderNo": "ORD20240101001",
    "status": "IN_PROGRESS",
    "updateTime": "2024-01-01 12:00:00"
  }
}
```

### 5. 更新订单信息
**接口地址**: `PUT /api/orders/{id}`

**请求头**:
```
Authorization: Bearer {token}
```

**请求参数**:
```json
{
  "customerName": "北京科技有限公司（已更新）",
  "salespersonId": 3,
  "items": [
    {
      "productId": 1,
      "quantity": 3
    }
  ]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "orderNo": "ORD20240101001",
    "customerName": "北京科技有限公司（已更新）",
    "totalAmount": 23997.00,
    "status": "PENDING",
    "salespersonId": 3,
    "salespersonName": "李四",
    "items": [
      {
        "id": 2,
        "productId": 1,
        "productName": "iPhone 15",
        "quantity": 3,
        "unitPrice": 7999.00
      }
    ],
    "updateTime": "2024-01-01 14:00:00"
  }
}
```

### 6. 删除订单
**接口地址**: `DELETE /api/orders/{id}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 7. 导出订单PDF
**接口地址**: `GET /api/orders/{id}/export-pdf`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**: PDF文件流

**Content-Type**: `application/pdf`

**Content-Disposition**: `attachment; filename=订单_ORD20240101001.pdf`

## 总览模块接口

### 1. 获取销售统计数据
**接口地址**: `GET /api/overview/statistics`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalSales": 1000000.00,
    "monthlySales": 150000.00,
    "dailySales": 5000.00,
    "totalOrders": 500,
    "pendingOrders": 50,
    "inProgressOrders": 100,
    "completedOrders": 300,
    "cancelledOrders": 50
  }
}
```

### 2. 获取销售排行榜
**接口地址**: `GET /api/overview/sales-ranking`

**请求参数**:
- `limit`: 返回数量（默认10）

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "salespersonId": 2,
      "salespersonName": "张三",
      "photoUrl": "https://oss.example.com/sales1.jpg",
      "totalSales": 50000.00,
      "orderCount": 25
    },
    {
      "salespersonId": 3,
      "salespersonName": "李四",
      "photoUrl": "https://oss.example.com/sales2.jpg",
      "totalSales": 45000.00,
      "orderCount": 20
    }
  ]
}
```

### 3. 获取热门商品
**接口地址**: `GET /api/overview/hot-products`

**请求参数**:
- `limit`: 返回数量（默认10）

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "productId": 1,
      "productName": "iPhone 15",
      "imageUrl": "https://oss.example.com/iphone15.jpg",
      "salesCount": 100,
      "salesAmount": 799900.00
    },
    {
      "productId": 2,
      "productName": "MacBook Pro",
      "imageUrl": "https://oss.example.com/macbook.jpg",
      "salesCount": 50,
      "salesAmount": 999900.00
    }
  ]
}
```

### 4. 获取订单趋势数据
**接口地址**: `GET /api/overview/order-trend`

**请求参数**:
- `days`: 天数（默认30）

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "date": "2024-01-01",
      "orderCount": 10,
      "salesAmount": 50000.00
    },
    {
      "date": "2024-01-02",
      "orderCount": 15,
      "salesAmount": 75000.00
    }
  ]
}
```

### 5. 导出总览PDF报表
**接口地址**: `GET /api/overview/export-pdf`

**请求头**:
```
Authorization: Bearer {token}
```

**响应**: PDF文件流

**Content-Type**: `application/pdf`

**Content-Disposition**: `attachment; filename=总览报表_20240101.pdf`

## 智能问答接口

### 1. 发送问题
**接口地址**: `POST /api/ai/chat`

**请求头**:
```
Authorization: Bearer {token}
```

**请求参数**:
```json
{
  "question": "本月销售额最高的销售是谁？",
  "conversationId": "conv_123456"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "answer": "根据数据分析，本月销售额最高的销售是张三，总销售额为50000元，共完成25笔订单。",
    "conversationId": "conv_123456",
    "intent": "sales_query",
    "sql": "SELECT u.name, SUM(o.total_amount) as total_sales FROM orders o JOIN sys_user u ON o.salesperson_id = u.id WHERE MONTH(o.create_time) = MONTH(CURRENT_DATE()) AND o.status = 'COMPLETED' GROUP BY u.id ORDER BY total_sales DESC LIMIT 1",
    "data": [
      {
        "name": "张三",
        "total_sales": 50000.00
      }
    ],
    "timestamp": 1690000000000
  }
}
```

### 2. 流式聊天 (SSE)
**接口地址**: `POST /api/ai/chat/stream`

**请求头**:
```
Authorization: Bearer {token}
Content-Type: application/json
Accept: text/event-stream
```

**请求参数**:
```json
{
  "question": "分析一下热销商品的趋势",
  "conversationId": "conv_123456"
}
```

**响应格式**: Server-Sent Events (SSE)

**事件类型**:
| 事件名 | 说明 | 数据格式 |
|--------|------|----------|
| conversationId | 对话ID | 字符串 |
| token | 流式输出的token | 字符串 |
| complete | 完成事件 | JSON对象 |
| error | 错误事件 | 错误信息字符串 |

**SSE响应示例**:
```
event:conversationId
data:conv_123456

event:token
data:根据

event:token
data:数据

event:token
data:分析

event:complete
data:{"fullResponse":"根据数据分析，热销商品趋势如下...","tokenCount":150}
```

**超时设置**: SSE连接超时时间为5分钟

### 3. 获取对话历史
**接口地址**: `GET /api/ai/conversation/{conversationId}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "role": "user",
      "content": "本月销售额最高的销售是谁？",
      "timestamp": 1690000000000
    },
    {
      "role": "assistant",
      "content": "根据数据分析，本月销售额最高的销售是张三，总销售额为50000元，共完成25笔订单。",
      "timestamp": 1690000000000
    }
  ]
}
```

### 5. 清除对话历史
**接口地址**: `DELETE /api/ai/conversation/{conversationId}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "清除成功",
  "data": null
}
```

## RAG文档问答接口

### 1. 上传并处理文档
**接口地址**: `POST /api/rag/documents`

**请求头**:
```
Authorization: Bearer {token}
```

**请求参数**:
```json
{
  "title": "销售管理系统使用指南",
  "content": "# 概述\n本系统是一个企业级销售管理系统...",
  "metadata": "guide,manual"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "文档上传处理成功",
  "data": {
    "success": true,
    "sourceDocId": 1,
    "chunkCount": 15,
    "message": "文档已成功处理为15个文本块"
  }
}
```

### 2. 语义问答
**接口地址**: `POST /api/rag/query`

**请求头**:
```
Authorization: Bearer {token}
```

**请求参数**:
```json
{
  "question": "如何创建新订单？",
  "topK": 3
}
```

**参数说明**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| question | String | 是 | 用户问题 |
| topK | Integer | 否 | 返回相关文档块数量，默认3 |

**响应示例**:
```json
{
  "code": 200,
  "message": "问答成功",
  "data": {
    "success": true,
    "answer": "创建新订单的步骤：1. 进入订单管理页面 2. 点击"新建订单"按钮 3. 填写客户信息和商品信息 4. 提交订单",
    "sources": [
      {
        "content": "订单创建流程说明...",
        "score": 0.95,
        "sourceTitle": "销售管理系统使用指南"
      }
    ]
  }
}
```

### 3. 获取文档块信息
**接口地址**: `GET /api/rag/documents/{sourceDocId}/chunks`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "content": "文档块内容...",
      "chunkIndex": 0,
      "metadata": "guide"
    }
  ]
}
```

### 4. 删除文档
**接口地址**: `DELETE /api/rag/documents/{sourceDocId}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

## 文件上传接口

### 1. 上传图片
**接口地址**: `POST /api/upload/image`

**请求头**:
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**请求参数**:
- `file`: 图片文件

**响应示例**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "https://oss-cn-hangzhou.aliyuncs.com/monster060508/images/1234567890.jpg",
    "filename": "1234567890.jpg",
    "size": 1024000
  }
}
```

### 2. 上传文件
**接口地址**: `POST /api/upload/file`

**请求头**:
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**请求参数**:
- `file`: 文件

**响应示例**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "https://oss-cn-hangzhou.aliyuncs.com/monster060508/files/1234567890.pdf",
    "filename": "1234567890.pdf",
    "size": 1024000
  }
}
```

## Markdown文档管理接口

**说明**: Markdown文档用于智能问答模块的RAG功能，仅支持Markdown格式文件。

### 1. 获取文档列表
**接口地址**: `GET /api/markdown-documents`

**请求参数**:
- `page`: 页码（默认1）
- `size`: 每页数量（默认10）
- `keyword`: 搜索关键词（可选）

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "销售管理系统使用指南",
        "fileName": "user-guide.md",
        "ossUrl": "https://oss-cn-hangzhou.aliyuncs.com/monster060508/markdown/user-guide.md",
        "fileSize": 1024,
        "uploadUserId": 1,
        "createTime": "2024-01-01 10:00:00"
      }
    ],
    "total": 10,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

### 2. 获取文档详情
**接口地址**: `GET /api/markdown-documents/{id}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "title": "销售管理系统使用指南",
    "fileName": "user-guide.md",
    "ossUrl": "https://oss-cn-hangzhou.aliyuncs.com/monster060508/markdown/user-guide.md",
    "content": "# 销售管理系统使用指南\n\n## 概述\n本系统是一个企业级销售管理系统...",
    "fileSize": 1024,
    "uploadUserId": 1,
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-01 10:00:00"
  }
}
```

### 3. 上传Markdown文档
**接口地址**: `POST /api/markdown-documents`

**请求头**:
```
Authorization: Bearer {token}
```

**请求参数**:
```json
{
  "title": "销售管理系统使用指南",
  "fileName": "user-guide.md",
  "ossUrl": "https://oss-cn-hangzhou.aliyuncs.com/monster060508/markdown/user-guide.md",
  "content": "# 销售管理系统使用指南\n\n## 概述\n本系统是一个企业级销售管理系统...",
  "fileSize": 1024,
  "uploadUserId": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "id": 1,
    "title": "销售管理系统使用指南",
    "fileName": "user-guide.md",
    "ossUrl": "https://oss-cn-hangzhou.aliyuncs.com/monster060508/markdown/user-guide.md",
    "content": "# 销售管理系统使用指南\n\n## 概述\n本系统是一个企业级销售管理系统...",
    "fileSize": 1024,
    "uploadUserId": 1,
    "createTime": "2024-01-01 10:00:00"
  }
}
```

### 4. 更新Markdown文档
**接口地址**: `PUT /api/markdown-documents/{id}`

**请求头**:
```
Authorization: Bearer {token}
```

**请求参数**:
```json
{
  "title": "销售管理系统使用指南（更新版）",
  "content": "# 销售管理系统使用指南\n\n## 概述\n本系统是一个企业级销售管理系统（更新版）..."
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "title": "销售管理系统使用指南（更新版）",
    "fileName": "user-guide.md",
    "ossUrl": "https://oss-cn-hangzhou.aliyuncs.com/monster060508/markdown/user-guide.md",
    "content": "# 销售管理系统使用指南\n\n## 概述\n本系统是一个企业级销售管理系统（更新版）...",
    "fileSize": 1024,
    "uploadUserId": 1,
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-01 12:00:00"
  }
}
```

### 5. 删除Markdown文档
**接口地址**: `DELETE /api/markdown-documents/{id}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 6. 获取文档内容
**接口地址**: `GET /api/markdown-documents/{id}/content`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "content": "# 销售管理系统使用指南\n\n## 概述\n本系统是一个企业级销售管理系统..."
  }
}
```

### 7. 根据用户获取文档列表
**接口地址**: `GET /api/markdown-documents/user/{userId}`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "title": "销售管理系统使用指南",
      "fileName": "user-guide.md",
      "ossUrl": "https://oss-cn-hangzhou.aliyuncs.com/monster060508/markdown/user-guide.md",
      "fileSize": 1024,
      "uploadUserId": 1,
      "createTime": "2024-01-01 10:00:00"
    }
  ]
}
```

## 错误处理

### 错误响应格式
```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": null,
  "timestamp": 1690000000000,
  "errors": [
    {
      "field": "username",
      "message": "用户名不能为空"
    }
  ]
}
```

### 常见错误码
- `400001`: 请求参数校验失败
- `401001`: 用户名或密码错误
- `401002`: Token已过期
- `401003`: Token无效
- `403001`: 权限不足
- `404001`: 资源不存在
- `500001`: 服务器内部错误

## 分页查询参数

### 通用分页参数
- `page`: 页码（从1开始）
- `size`: 每页数量（默认10，最大100）

### 分页响应格式
```json
{
  "records": [],
  "total": 100,
  "size": 10,
  "current": 1,
  "pages": 10
}
```

## 数据过滤参数

### 日期范围过滤
- `startDate`: 开始日期（格式：yyyy-MM-dd）
- `endDate`: 结束日期（格式：yyyy-MM-dd）

### 关键词搜索
- `keyword`: 搜索关键词

### 状态过滤
- `status`: 状态值

## 接口限流

### 限流规则
- 普通接口：100次/分钟
- 文件上传接口：10次/分钟
- AI接口：20次/分钟

### 限流响应
```json
{
  "code": 429,
  "message": "请求过于频繁，请稍后再试",
  "data": null,
  "timestamp": 1690000000000
}
```

## 接口版本控制

### 版本号
- 当前版本：v1
- 版本路径：`/api/v1/`

### 版本兼容性
- 新版本接口会保持向后兼容
- 旧版本接口会在新版本发布后继续支持6个月

## 接口文档更新

### 文档版本
- 文档版本：1.1.0
- 最后更新：2024-12-23

### 更新记录
1. **v1.1.0** (2024-12-23)
   - 新增JWT认证详细说明
   - 新增SSE流式聊天接口文档
   - 新增用户创建参数验证规则
   - 修复角色值说明 (ADMIN/SALES)
   - 优化错误响应格式说明

2. **v1.0.0** (2024-01-01)
   - 初始版本
   - 包含所有核心接口

---

**注意**：本文档中的示例数据仅供参考，实际响应数据可能有所不同。接口参数和返回格式可能会根据业务需求进行调整。