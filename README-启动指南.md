# 企业销售管理系统 - 启动指南

## 快速开始

### 1. 一键启动
双击运行 `quick-start.bat`，系统将自动启动：
- 后端服务 (http://localhost:8080)
- 前端服务 (http://localhost:5173)
- 自动打开浏览器

### 2. 测试账号
| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 销售 | sales001 | sales123 |

## 详细启动方式

### 方式一：完整启动脚本
运行 `start.bat`，选择启动方式：
```
1. 启动完整系统（后端 + 前端）
2. 仅启动后端
3. 仅启动前端
4. 初始化数据库
5. 运行测试
6. 退出
```

### 方式二：手动启动

#### 启动后端
```bash
cd backend
mvn spring-boot:run
```

#### 启动前端
```bash
cd frontend
npm install  # 首次运行
npm run dev
```

## 首次运行配置

### 1. 环境要求
- Java 17+
- Node.js 18+
- Maven 3.8+
- MySQL 8.0+

### 2. 配置文件
复制配置模板并填入真实信息：
```bash
cp 配置信息.md.example 配置信息.md
cp docs/配置文档.md.example docs/配置文档.md
```

编辑配置文件，填入：
- 数据库连接信息
- OSS配置
- 大模型API配置

### 3. 数据库初始化
```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE sales_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 初始化表结构
mysql -u root -p sales_management < sql/init.sql

# 插入初始数据
mysql -u root -p sales_management < sql/data.sql
```

或使用启动脚本的选项4。

## 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://localhost:5173 | Vue开发服务器 |
| 后端 | http://localhost:8080 | Spring Boot应用 |
| API | http://localhost:8080/api | REST API接口 |

## 功能模块

### 1. 总览模块
- 销售数据统计
- 图表展示
- 报表导出

### 2. 商品管理
- 商品CRUD
- 图片上传
- 库存管理

### 3. 订单管理
- 订单创建
- 状态流转
- PDF导出

### 4. 员工管理
- 员工CRUD
- 角色权限
- 照片上传

### 5. 智能问答
- AI对话
- 数据查询
- 流式输出

## 停止服务

运行 `stop.bat` 停止所有服务，或手动关闭对应的命令行窗口。

## 常见问题

### Q1: 端口被占用
```bash
# 查找占用端口的进程
netstat -ano | findstr :8080
netstat -ano | findstr :5173

# 终止进程
taskkill /PID <进程ID> /F
```

### Q2: 数据库连接失败
1. 检查MySQL服务是否启动
2. 验证配置文件中的数据库信息
3. 确保数据库已创建

### Q3: 前端启动失败
```bash
cd frontend
npm cache clean --force
rm -rf node_modules
npm install
```

### Q4: 后端启动失败
1. 检查Java版本：`java -version`
2. 检查Maven版本：`mvn -version`
3. 查看错误日志

## 开发指南

### 项目结构
```
企业销售管理系统/
├── backend/          # Spring Boot后端
├── frontend/         # Vue前端
├── sql/              # 数据库脚本
├── docs/             # 项目文档
├── start.bat         # 完整启动脚本
├── quick-start.bat   # 快速启动脚本
└── stop.bat          # 停止服务脚本
```

### 开发流程
1. 启动后端：`cd backend && mvn spring-boot:run`
2. 启动前端：`cd frontend && npm run dev`
3. 访问 http://localhost:5173
4. 修改代码后自动热重载

### 测试
```bash
# 运行后端单元测试
cd backend && mvn test

# 运行API集成测试
python test_all_api.py

# 运行RAG测试
python test_rag_pipeline.py
```

## 部署

### 生产环境部署
```bash
# 后端打包
cd backend
mvn clean package -Pprod

# 运行
java -jar target/sales-management-1.0.0.jar --spring.profiles.active=prod

# 前端构建
cd frontend
npm run build

# 部署dist目录到Web服务器
```

## 技术支持

如遇到问题：
1. 查看本文档的常见问题部分
2. 检查控制台错误信息
3. 查看日志文件
4. 联系开发团队

---

**项目版本**: 1.0.0  
**最后更新**: 2026-06-23