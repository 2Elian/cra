<div align="center"> 

# CRA: Contract compliance review backend service

</div>

---

The backend service of the Contract Compliance Review Agent is a distributed microservice architecture based on Spring Cloud, designed specifically for enterprise contract lifecycle management and compliance review. This system enables functions such as contract uploading, content extraction, compliance checking, and risk assessment, helping enterprises improve contract management efficiency and reduce compliance risks.

---

### Core framework
- Spring Boot 3.2.0
- Spring Cloud 2023.0.0
- Spring Cloud Alibaba 2023.0.1.0

### DataBase
- PostgreSQL：Structured data storage (contract metadata, version information, etc.)
- MongoDB：Storage of unstructured contract content
- Elasticsearch：Full-text search and content analysis
- Redis0：Caching and session management

### MiddleWare
- RabbitMQ: Asynchronous message management
- Nacos：Service Registration and Configuration Center
- Sa-Token 1.39.0：Authentication and authorization
- Apache Tika 2.8.0：Document content extraction
- Activiti 7.1.0.M6：Workflow engine

---

## Microservice architecture

### 1. user-service
- 用户注册与登录
- 角色与权限管理
- 用户信息维护
- 认证与授权服务（基于Sa-Token实现）

### 2 contract-service
- 合同基本信息管理
- 合同版本控制
- 合同内容存储与检索（支持MongoDB和Elasticsearch）
- 合同状态管理
- 合同内容搜索（支持关键词高亮）

### 3. Planing
- 条款提取服务 (clause-extraction-service)
- 合规审查服务 (compliance-review-service)
- 报告生成服务 (report-generation-service)

---

## Quick Start

### 1. Environmental requirements
- JDK 17+
- Maven 3.9.0+
- Docker 20.10.0+

### 2. Configuration Instructions

1. **配置中心**
   - 启动 Nacos 服务（默认端口：8848）
   - 下载 Nacos: https://github.com/alibaba/nacos/releases/download/2.2.3/nacos-server-2.2.3.zip
   - 解压并启动：`cd nacos/bin && .\startup.cmd -m standalone

2. **数据库配置**
   - **PostgreSQL**: 会自动创建数据库 `contract_db` (根据配置)
   - **MongoDB**: 会自动创建数据库 `contract_content` (根据配置)
   - **Elasticsearch**: 无需提前创建索引，系统会自动初始化
   - **Redis**: 无需特殊配置，使用默认端口

3. **消息队列**
   - RabbitMQ 服务默认端口：5672

### 3. Start Step

1. **启动依赖服务**
   ```bash
   # 使用 Docker Compose 启动所有依赖服务 (推荐)
   # 创建 docker-compose.yml 文件并运行:
   docker-compose up -d
   
   # 或者手动启动各个服务:
   # 启动 PostgreSQL
   docker run --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=123456 -e POSTGRES_DB=contract_db -d postgres:15
   
   # 启动 MongoDB
   docker run --name mongodb -p 27017:27017 -d mongo:6.0
   
   # 启动 Elasticsearch
   docker run --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -e "xpack.security.enabled=false" -d elasticsearch:8.8.0
   
   # 启动 Redis
   docker run --name redis -p 6379:6379 -d redis:7.0
   
   # 启动 RabbitMQ
   docker run --name rabbitmq -p 5672:5672 -p 15672:15672 -d rabbitmq:3.12-management
   ```

2. **启动 Nacos 配置中心**
   ```bash
   # 解压并启动 Nacos
   cd nacos/bin
   .\startup.cmd -m standalone
   sh startup.sh -m standalone
   ```

3. **启动微服务**
   ```bash
   $env:JAVA_HOME = "D:\java\jdk17"
   $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
   # 先安装 common 模块到本地仓库
   cd cra-common
   mvn clean install   
   
   # 再安装父工程依赖
   cd ..
   mvn clean install

   # 编译并启动用户服务 (端口: 8081)
   cd cra-user-service
   mvn spring-boot:run
   
   # 编译并启动合同服务 (端口: 8082)
   cd cra-contract-service
   mvn spring-boot:run
   ```

### 4. 访问系统
- Nacos 控制台：http://localhost:8848/nacos (默认用户名/密码: nacos/nacos)
- 用户服务 API：http://localhost:8081
- 合同服务 API：http://localhost:8082
- RabbitMQ 管理界面：http://localhost:15672 (默认用户名/密码: guest/guest)

---

### Docker Compose 配置示例

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    ports:
      - "5432:5432"
    environment:
      - POSTGRES_PASSWORD=123456
      - POSTGRES_DB=contract_db
    volumes:
      - postgres_data:/var/lib/postgresql/data

  mongodb:
    image: mongo:6.0
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db

  elasticsearch:
    image: elasticsearch:8.8.0
    ports:
      - "9200:9200"
      - "9300:9300"
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data

  redis:
    image: redis:7.0
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  rabbitmq:
    image: rabbitmq:3.12-management
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq

volumes:
  postgres_data:
  mongodb_data:
  elasticsearch_data:
  redis_data:
  rabbitmq_data:
```

---

## API Docs

### 5.1 合同服务 API

| 接口路径 | 请求方法 | 功能描述 |
|---------|---------|---------|
| `/api/contracts` | POST | 创建合同 |
| `/api/contracts/{id}` | PUT | 更新合同基本信息 |
| `/api/contracts/{id}` | DELETE | 删除合同 |
| `/api/contracts/{id}` | GET | 获取合同详情 |
| `/api/contracts` | GET | 获取合同列表（支持分页） |
| `/api/contracts/{id}/versions` | GET | 获取合同版本列表 |
| `/api/contracts/{id}/versions/{versionId}` | GET | 获取特定版本详情 |
| `/api/contracts/{id}/content` | POST | 上传合同内容 |
| `/api/contracts/{id}/content` | GET | 获取合同内容 |
| `/api/contracts/{id}/status/{status}` | PUT | 更新合同状态 |
| `/api/contracts/{id}/export` | GET | 导出合同 |
| `/api/contracts/search` | GET | 搜索合同内容（支持关键词高亮） |
| `/api/contracts/version/compare` | GET | 比较合同版本差异 |
| `/api/contracts/{id}/approve` | PUT | 审批合同 |
| `/api/contracts/{id}/reject` | PUT | 驳回合同 |

### 5.2 用户服务 API

| 接口路径 | 请求方法 | 功能描述 |
|---------|---------|---------|
| `/api/users/register` | POST | 用户注册 |
| `/api/users/login` | POST | 用户登录 |
| `/api/users/logout` | POST | 用户登出 |
| `/api/users/refresh` | POST | 刷新令牌 |
| `/api/users/{id}` | GET | 获取用户信息 |
| `/api/users/{id}` | PUT | 更新用户信息 |
| `/api/users/{id}/password` | PUT | 修改密码 |
| `/api/roles` | GET | 获取角色列表 |
| `/api/roles/{id}` | GET | 获取角色详情 |
| `/api/permissions` | GET | 获取权限列表 |

## 6. 项目结构

```
contract-compliance-agent/
├── common/                      # 公共模块
│   ├── src/main/java/com/contractcompliance/common/
│   │   ├── exception/          # 异常处理 (BusinessException)
│   │   ├── handler/            # 全局异常处理器 (GlobalExceptionHandler)
│   │   └── model/              # 公共模型 (Response 统一响应结构)
│   └── pom.xml
├── user-service/               # 用户服务 (已实现)
│   ├── src/main/java/com/contractcompliance/user/
│   │   ├── controller/         # 控制器层
│   │   ├── service/            # 服务层（接口+实现）
│   │   ├── repository/         # 数据访问层 (JPA Repositories)
│   │   └── entity/             # 实体类 (User, Role, Permission)
│   ├── src/main/resources/
│   │   └── bootstrap.yml       # 服务配置
│   └── pom.xml
├── contract-service/           # 合同服务 (已实现)
│   ├── src/main/java/com/contractcompliance/contract/
│   │   ├── controller/         # 控制器层
│   │   ├── service/            # 服务层（接口+实现）
│   │   ├── repository/         # 数据访问层 (JPA + MongoDB Repositories)
│   │   └── entity/             # 实体类 (ContractMain, ContractContent, ContractVersion)
│   ├── src/main/resources/
│   │   └── bootstrap.yml       # 服务配置
│   └── pom.xml
├── pom.xml                     # 父项目配置
└── README.md                   # 项目说明文档
```

### 6.1 已实现功能

#### 用户服务 (user-service)
- ✅ 用户注册与登录 (基于 Sa-Token 认证)
- ✅ 角色与权限管理 (RBAC)
- ✅ 用户信息维护
- ✅ 令牌刷新机制

#### 合同服务 (contract-service)
- ✅ 合同基本信息管理
- ✅ 合同版本控制
- ✅ 合同内容存储 (MongoDB)
- ✅ 合同内容全文搜索 (Elasticsearch)
- ✅ 合同状态管理
- ✅ 合同内容关键词搜索与高亮

### 6.2 待实现功能

- 条款提取服务 (clause-extraction-service)
- 合规审查服务 (compliance-review-service)
- 报告生成服务 (report-generation-service)

## 7. 开发指南

### 7.1 代码规范
- 遵循 Spring Boot 编码规范
- 使用 Lombok 简化代码 (所有实体类和数据传输对象都应使用 Lombok)
- 统一异常处理 (BusinessException + GlobalExceptionHandler)
- 统一响应格式 (Response 类)
- 日志记录规范 (使用 SLF4J)

### 7.2 开发流程
1. **克隆代码仓库**
   ```bash
   git clone [repository-url]
   cd contract-compliance-agent
   ```

2. **编译项目**
   ```bash
   mvn clean compile
   ```

3. **运行测试**
   ```bash
   # 运行单元测试
   mvn test
   
   # 运行集成测试
   mvn integration-test
   ```

4. **打包部署**
   ```bash
   mvn clean package -DskipTests
   ```

### 7.3 常见问题与解决方案

#### 7.3.1 Maven 依赖问题
**问题**: `'dependencies.dependency.version' for cn.dev33:sa-token-spring-boot-starter:jar is missing.`
**解决方案**: 确保使用了父项目的版本属性 `${sa-token.version}`

#### 7.3.2 数据库连接失败
**问题**: PostgreSQL 连接拒绝
**解决方案**: 检查 PostgreSQL 容器是否运行，端口是否映射正确 (5432:5432)

#### 7.3.3 MongoDB 连接问题
**问题**: `MongoSocketOpenException: Exception opening socket`
**解决方案**: 确保 MongoDB 容器正在运行，端口映射正确 (27017:27017)

#### 7.3.4 Nacos 配置问题
**问题**: 服务无法注册到 Nacos
**解决方案**: 确保 Nacos 服务正在运行，检查 `bootstrap.yml` 中的 Nacos 配置

### 7.4 部署方式
- **本地开发**: 使用 `mvn spring-boot:run` 启动服务
- **Docker 容器化**: 每个服务都可以打包为独立的 Docker 镜像
- **Kubernetes 集群**: 使用 Kubernetes 部署和管理微服务
- **CI/CD 流水线**: 集成 Jenkins/GitLab CI 实现自动化构建和部署

## 8. 技术亮点

1. **微服务架构**: 基于 Spring Cloud 2022.x 的分布式微服务架构
2. **多数据源支持**: 同时支持 PostgreSQL (结构化数据)、MongoDB (非结构化数据) 和 Elasticsearch (搜索)
3. **统一认证**: 基于 Sa-Token 的统一认证和授权体系
4. **异步处理**: 使用 RabbitMQ 实现异步消息处理
5. **全文搜索**: 基于 Elasticsearch 的高效全文搜索和关键词高亮
6. **版本控制**: 完整的合同版本控制和差异比较功能
7. **统一响应**: 全局统一的 API 响应格式和异常处理

## 9. 联系方式

如有问题或建议，请联系：
- 项目负责人：[负责人姓名]
- 技术支持：[技术支持邮箱]
- GitHub 仓库：[项目 GitHub 地址]

## 10. 版本历史

| 版本 | 发布日期 | 主要功能 |
|------|---------|---------|
| 0.1.0 | 2024-05-20 | 初始开发版本 |
| 0.2.0 | 2024-05-25 | 实现用户服务 (用户注册、登录、角色权限管理) |
| 0.3.0 | 2024-05-30 | 实现合同服务 (合同管理、版本控制、内容存储) |
| 0.4.0 | 2024-06-05 | 实现合同内容搜索功能 (支持关键词高亮) |
| 1.0.0 | 2024-06-10 | 正式版本发布，包含用户服务和合同服务的完整功能 |

---

© 2024 合同合规审查 Agent 系统开发团队