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
- **MySQL** - 业务数据库
- **PostgreSQL** - 向量数据库（用于RAG）
- **Spring Security** - 权限控制

### 前端技术
- **Vue 3** - 前端框架
- **Element Plus** - UI组件库
- **Axios** - HTTP客户端
- **ECharts** - 数据可视化

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
- **意图分析**：自动识别用户问题类型
- **传统RAG（仅支持Markdown文件）**：基于Markdown文档检索的问答系统
- **SQL生成**：根据问题自动生成SQL查询数据库
- **AI分析**：对查询结果进行智能分析和整合
- **自然语言交互**：支持自然语言提问
- **文档管理**：支持上传、编辑、删除Markdown文档，通过OSS存储

## 项目结构

```
企业销售管理系统/
├── backend/                    # 后端Spring Boot项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/enterprise/sales/
│   │   │   │   ├── config/         # 配置类
│   │   │   │   ├── controller/     # 控制器
│   │   │   │   ├── dto/            # 数据传输对象
│   │   │   │   ├── entity/         # 实体类
│   │   │   │   ├── enums/          # 枚举类
│   │   │   │   ├── mapper/         # MyBatis Mapper接口
│   │   │   │   ├── service/        # 服务层
│   │   │   │   └── vo/             # 视图对象
│   │   │   └── resources/
│   │   │       ├── mapper/         # MyBatis XML映射文件
│   │   │       └── application.yml # 配置文件
│   │   └── test/                   # 测试代码
│   └── pom.xml                     # Maven配置
├── frontend/                   # 前端Vue项目
├── docs/                       # 项目文档
│   ├── README.md               # 项目说明
│   ├── 配置文档.md              # 配置说明
│   ├── API文档.md              # API接口文档
│   └── 功能介绍.md              # 功能模块介绍
└── sql/                        # 数据库脚本
    ├── init.sql                # 数据库初始化脚本
    └── data.sql                # 初始数据脚本
```

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- Node.js 16+
- MySQL 8.0+
- PostgreSQL 12+（用于向量存储）

### 后端启动
1. 配置数据库连接信息（见`配置文档.md`）
2. 执行数据库初始化脚本：`sql/init.sql`
3. 进入backend目录：`cd backend`
4. 执行Maven构建：`mvn clean install`
5. 启动Spring Boot应用：`mvn spring-boot:run`

### 前端启动
1. 进入frontend目录：`cd frontend`
2. 安装依赖：`npm install`
3. 启动开发服务器：`npm run dev`
4. 访问系统：`http://localhost:5173`

### 默认账户
- **管理员**：admin / admin123
- **销售**：sales / sales123

## 配置说明
详细的配置信息请参考：
- [配置文档.md](./配置文档.md) - 系统配置详细说明
- [API文档.md](./API文档.md) - 后端API接口文档
- [功能介绍.md](./功能介绍.md) - 功能模块详细介绍

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

### Docker部署（可选）
```bash
# 构建后端镜像
docker build -t sales-backend ./backend

# 构建前端镜像
docker build -t sales-frontend ./frontend

# 使用docker-compose启动
docker-compose up -d
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

## 贡献指南
1. Fork 本仓库
2. 创建功能分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -m 'Add some feature'`
4. 推送到分支：`git push origin feature/your-feature`
5. 创建Pull Request

## 许可证
本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 联系方式
- 项目维护者：[您的姓名]
- 邮箱：[您的邮箱]
- 项目链接：[项目Git地址]

---

**注意**：本系统仅供学习和参考使用，生产环境部署前请进行安全审查和性能测试。