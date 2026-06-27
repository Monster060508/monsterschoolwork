# 企业销售管理系统

## 项目简介
企业销售管理系统是一个基于Spring Boot + Vue的前后端分离企业级应用，旨在帮助企业高效管理销售业务。系统包含总览、商品管理、订单管理、员工管理和智能问答五大核心模块，支持数据可视化、库存管理、订单处理、员工权限控制以及基于AI的智能数据分析。

## 技术栈

### 后端技术
- **Spring Boot 3.2.0** - 核心框架
- **MyBatis Plus 3.5.5** - ORM框架
- **LangChain4j 0.25.0** - AI大模型集成
- **阿里云OSS** - 对象存储服务
- **Apache POI 5.2.5** - PDF报表生成
- **MySQL 8.0** - 业务数据库
- **Spring Security** - 权限控制
- **JWT** - Token认证

### 前端技术
- **Vue 3** - 前端框架
- **Element Plus** - UI组件库
- **Axios** - HTTP客户端
- **ECharts** - 数据可视化
- **Vite** - 构建工具

## 系统功能模块

### 1. 总览模块
- 订单状态统计（待处理、进行中、已完成、已取消）
- 销售排行榜（销冠展示）
- 热门商品TOP10
- 总销售额、月度销售额、日均销售额
- 订单趋势图表
- 支持导出PDF格式的总览报表

### 2. 商品管理模块
- 商品信息的增删改查
- 商品图片上传（阿里云OSS）
- 商品库存管理
- 商品分类和搜索
- 商品详情展示

### 3. 订单管理模块
- 新建订单（关联销售人员、商品）
- 订单状态管理（待处理→进行中→已完成/已取消）
- 订单详情查看
- 订单完成后自动扣减库存
- 订单PDF导出下载

### 4. 员工管理模块
- **管理员权限**：
  - 增删改查员工信息
  - 员工照片上传（OSS）
  - 权限分配（管理员/销售）
  - 查看所有功能模块
- **销售权限**：
  - 查看总览模块
  - 管理订单模块
  - 使用智能问答模块
  - 无商品管理和员工管理权限

### 5. 智能问答模块
- **意图分析**：自动识别用户问题类型（销售查询、商品查询、订单查询、员工查询等）
- **SQL生成**：根据问题自动生成SQL查询数据库
- **AI分析**：对查询结果进行智能分析和整合
- **流式输出**：支持SSE流式输出，实时显示AI回答
- **自然语言交互**：支持自然语言提问
- **对话管理**：支持多轮对话、历史记录查看和清除

## 项目结构

```
企业销售管理系统/
├── backend/                    # 后端Spring Boot项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/enterprise/sales/
│   │   │   │   ├── config/         # 配置类
│   │   │   │   ├── controller/     # 控制器
│   │   │   │   ├── entity/         # 实体类
│   │   │   │   ├── enums/          # 枚举类
│   │   │   │   ├── mapper/         # MyBatis Mapper接口
│   │   │   │   ├── service/        # 服务层
│   │   │   │   └── util/           # 工具类
│   │   │   └── resources/
│   │   │       └── application.yml # 配置文件
│   │   └── test/                   # 测试代码
│   └── pom.xml                     # Maven配置
├── frontend/                   # 前端Vue项目
│   ├── src/
│   │   ├── api/                # API接口
│   │   ├── assets/             # 静态资源
│   │   ├── components/         # 组件
│   │   ├── layouts/            # 布局
│   │   ├── router/             # 路由
│   │   ├── stores/             # 状态管理
│   │   └── views/              # 页面视图
│   ├── package.json
│   └── vite.config.js
├── docs/                       # 项目文档
│   ├── README.md               # 项目说明
│   ├── API文档.md              # API接口文档
│   └── 功能介绍.md              # 功能模块介绍
├── sql/                        # 数据库脚本
│   ├── init.sql                # 数据库初始化脚本
│   └── data.sql                # 初始数据脚本
├── quick-start.bat             # 快速启动脚本
├── start.bat                   # 启动脚本
└── stop.bat                    # 停止脚本
```

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- Node.js 16+
- MySQL 8.0+

### 后端启动
1. 配置数据库连接信息（见`application.yml`）
2. 执行数据库初始化脚本：`sql/init.sql`
3. 执行初始数据脚本：`sql/data.sql`
4. 进入backend目录：`cd backend`
5. 执行Maven构建：`mvn clean install`
6. 启动Spring Boot应用：`mvn spring-boot:run`

### 前端启动
1. 进入frontend目录：`cd frontend`
2. 安装依赖：`npm install`
3. 启动开发服务器：`npm run dev`
4. 访问系统：`http://localhost:5173`

### 快速启动（Windows）
```bash
# 双击运行 quick-start.bat 脚本，自动启动前后端服务
.\quick-start.bat
```

### 默认账户
- **管理员**：admin / admin123
- **销售**：sales001 / sales123

## 配置说明

### 后端配置（application.yml）
```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sales_management?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf-8
    username: root
    password: your_password

# 大模型配置
langchain4j:
  open-ai:
    chat-model:
      base-url: https://api.openai.com/v1
      api-key: your_api_key
      model-name: gpt-3.5-turbo
      temperature: 0.3
      max-tokens: 2000

# 阿里云OSS配置
aliyun:
  oss:
    endpoint: https://oss-cn-hangzhou.aliyuncs.com
    bucket-name: your_bucket
    access-key-id: your_access_key
    access-key-secret: your_access_secret
```

## API接口概览

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/me` - 获取当前用户信息

### 用户管理接口
- `GET /api/users` - 获取用户列表
- `GET /api/users/{id}` - 获取用户详情
- `POST /api/users` - 创建用户
- `PUT /api/users/{id}` - 更新用户
- `DELETE /api/users/{id}` - 删除用户
- `POST /api/users/upload-photo` - 上传用户照片

### 商品管理接口
- `GET /api/products` - 获取商品列表
- `GET /api/products/{id}` - 获取商品详情
- `POST /api/products` - 创建商品
- `PUT /api/products/{id}` - 更新商品
- `DELETE /api/products/{id}` - 删除商品
- `POST /api/products/upload-image` - 上传商品图片

### 订单管理接口
- `GET /api/orders` - 获取订单列表
- `GET /api/orders/{id}` - 获取订单详情
- `POST /api/orders` - 创建订单
- `PUT /api/orders/{id}/status` - 更新订单状态
- `PUT /api/orders/{id}` - 更新订单信息
- `DELETE /api/orders/{id}` - 删除订单
- `GET /api/orders/{id}/export-pdf` - 导出订单PDF

### 总览模块接口
- `GET /api/overview/statistics` - 获取销售统计数据
- `GET /api/overview/sales-ranking` - 获取销售排行榜
- `GET /api/overview/hot-products` - 获取热门商品
- `GET /api/overview/order-trend` - 获取订单趋势数据
- `GET /api/overview/export-pdf` - 导出总览PDF报表

### 智能问答接口
- `POST /api/ai/chat` - 发送问题
- `POST /api/ai/chat/stream` - 流式聊天（SSE）
- `GET /api/ai/conversation/{conversationId}` - 获取对话历史
- `DELETE /api/ai/conversation/{conversationId}` - 清除对话历史
- `GET /api/ai/conversations` - 获取所有对话列表
- `POST /api/ai/analyze` - 分析数据

### 文件上传接口
- `POST /api/upload/image` - 上传图片
- `POST /api/upload/file` - 上传文件

详细的API文档请参考：[API文档.md](./API文档.md)

## 开发规范

### 代码规范
- 后端遵循阿里巴巴Java开发手册
- 前端遵循Vue.js风格指南
- 使用Lombok简化代码
- 使用MyBatis Plus简化数据库操作

### 提交规范
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动

## 部署说明

### 后端部署
1. 打包应用：`mvn clean package`
2. 运行jar包：`java -jar target/sales-management-1.0.0.jar`

### 前端部署
1. 构建生产版本：`npm run build`
2. 部署dist目录到Web服务器（如Nginx）

### Nginx配置示例
```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 常见问题

### 1. 数据库连接失败
- 检查MySQL服务是否启动
- 验证数据库连接信息是否正确
- 确认数据库用户权限

### 2. OSS上传失败
- 检查阿里云OSS配置信息
- 确认AccessKey权限
- 验证Bucket是否存在

### 3. AI功能无法使用
- 检查大模型API配置
- 确认API Key是否有效
- 验证网络连接

### 4. 前端无法访问后端API
- 检查后端服务是否启动
- 确认代理配置是否正确
- 检查CORS配置

## 许可证
本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 联系方式
- 项目维护者：[您的姓名]
- 邮箱：[您的邮箱]
- 项目链接：[项目Git地址]

---

**注意**：本系统仅供学习和参考使用，生产环境部署前请进行安全审查和性能测试。
