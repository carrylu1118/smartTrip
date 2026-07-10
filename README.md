# 智驾游 (Hitch-Trip)

> 一款基于 Spring Boot + Vue3 的顺风车出行 H5 端小程序，支持乘客端和司机端双角色，实现行程发布、顺路匹配、邀请同行、订单管理和微信支付等完整出行流程。

---

## 📋 目录

- [技术栈](#技术栈)
- [业务说明](#业务说明)
- [功能清单](#功能清单)
- [项目结构](#项目结构)
- [本地启动开发环境](#本地启动开发环境)
- [API 接口概览](#api-接口概览)
- [数据库表说明](#数据库表说明)

---

## 技术栈

### Backend — 后端核心（Spring Boot 3.5.3 + Java 17）

| 技术 | 版本 / 说明 |
|------|------------|
| **Java** | 17 |
| **Spring Boot** | 3.5.3 |
| **Spring AI** | 1.1.8 (BOM) |
| ├─ spring-ai-starter-model-openai | 对接千问 (DashScope OpenAI 兼容) |
| ├─ spring-ai-starter-vector-store-redis | Redis Stack 向量存储 |
| ├─ spring-ai-advisors-vector-store | 向量检索顾问 |
| └─ spring-ai-pdf-document-reader | PDF 文档解析 |
| **MyBatis** | mybatis-spring-boot-starter 3.0.3 |
| **Druid** | druid-spring-boot-3-starter 1.2.23 |
| **MySQL** | mysql-connector-j (8.0) |
| **Redis** | Spring Data Redis (Lettuce) + Session 共享 |
| **Redis Stack** | VectorStore 向量索引 (HNSW + COSINE) |
| **MongoDB** | Spring Data MongoDB — 通知消息存储 |
| **RabbitMQ** | Spring AMQP — 行程匹配 & AI 文档向量化 |
| **MinIO** | 8.5.10 — 文件/图片对象存储 |
| **WebSocket** | spring-boot-starter-websocket — 实时消息推送 |
| **百度地图 API** | 路径规划 & 距离估算 |
| **百度 AI SDK** | 4.16.2 — 车牌识别 |
| **微信支付 SDK** | red.htt:wxpay-sdk 3.0.9.1 |
| **SpringDoc OpenAPI** | 2.6.0 (替代 springfox+knife4j) |
| **Apache POI** | 5.2.5 — Word/DOCX 文档解析 |
| **Hutool** | 5.8.40 — 工具库 |
| **Fastjson** | 1.2.83 |
| **Lombok** | provided |

### Manager — 管理后台（Spring Boot 2.5.15 + Java 8）

| 技术 | 版本 / 说明 |
|------|------------|
| **Java** | 1.8 |
| **Spring Boot** | 2.5.15 |
| **若依框架 (RuoYi)** | 4.7.8 |
| **Shiro** | 1.13.0 (认证 & 授权) |
| **Thymeleaf** | 模板引擎 + thymeleaf-extras-shiro 2.1.0 |
| **MyBatis** | 原生 MyBatis + pagehelper 1.4.7 |
| **Druid** | 1.2.20 (读写分离) |
| **MySQL** | mysql-connector-java 8.0.33 |
| **RabbitMQ** | Spring AMQP — AI 文档向量化消息 |
| **MinIO** | 6.0.13 — 文件对象存储 |
| **Redis** | Spring Data Redis — 向量存储 |
| **DashScope SDK** | 2.16.7 — 千问 Embedding 向量化 |
| **Apache POI** | 4.1.2 — Excel 导入导出 |
| **Velocity** | 2.3 — 代码生成模板 |
| **Quartz** | 定时任务 |
| **Swagger (springfox)** | 3.0.0 — API 文档 |
| **Kaptcha** | 2.3.3 — 验证码 |
| **Fastjson** | 1.2.83 |
| **Lombok** | provided |

### Frontend — 前端（Vue 3 + Vite）

| 技术 | 版本 / 说明 |
|------|------------|
| **Vue** | 3.4 (Composition API) |
| **Vite** | 5.4 |
| **Vue Router** | 4.3 (Hash 模式) |
| **Pinia** | 2.1 (状态管理) |
| **Vant** | 4.8 (移动端 UI 组件库) |
| **Axios** | 1.6 (HTTP 请求) |
| **百度地图 JS SDK** | 地图选点 & 路径展示 |

### AI 能力总览

| 能力 | Backend | Manager |
|------|---------|---------|
| 对话模型 | ✅ 千问 (qwen-plus) | — |
| 流式 SSE 输出 | ✅ SseEmitter + Flux\<String\> | — |
| Embedding 向量化 | ✅ Spring AI OpenAI Embedding | ✅ DashScope SDK text-embedding-v1 |
| Redis 向量存储 | ✅ VectorStore (RediSearch KNN) | ✅ 原生 HSET + FT.SEARCH |
| 知识库检索 | ✅ KnowledgeBaseService | ✅ VectorStoreService |
| PDF 解析 | ✅ PagePdfDocumentReader | — |
| DOCX 解析 | ✅ Apache POI | — |
| 天气查询 | ✅ FunctionCallback (Open-Meteo) | — |
| Markdown 渲染 | ✅ 前端 AiRoute | — |
| 会话管理 | ✅ MySQL 持久化 + API | — |

---

## 业务说明

### 整体流程

这是一个 **顺风车拼车平台**，核心流程如下：

```
┌──────────────────────────────────────────────────────┐
│                    发布行程                           │
│  司机发布行程（起点→终点、出发时间、座位数）            │
│  乘客也可发布行程需求（起点→终点、出发时间）            │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│                    行程匹配                           │
│  系统根据出发地/目的地通过百度地图路径规划，             │
│  利用 GeoHash + RabbitMQ 匹配顺路行程                 │
│  司机可查看顺路的乘客需求，乘客可查看顺路的司机行程      │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│                    邀请 & 确认                        │
│  司机邀请乘客 → 乘客接受邀请 → 双方行程确认            │
│  乘客也可主动查看并选择合适的司机行程                  │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│                    生成订单                           │
│  双方确认后生成订单，包含行程信息、费用估算             │
└──────────────────────┬───────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────┐
│                     出行 & 支付                       │
│  司机出发 → 乘客上车 → 送达 → 支付（微信支付）         │
│  支持实时位置共享 & WebSocket 消息通知                │
└──────────────────────────────────────────────────────┘
```

### 双端角色

| 角色 | 核心能力 |
|------|---------|
| **乘客** | 发布行程需求、查看顺路司机、接受邀请、支付车费、查看订单 |
| **司机** | 发布行程、查看顺路乘客、邀请乘客、车辆认证、实名认证、收款 |

> 一个账号可以在乘客端和司机端之间切换（通过底部导航栏角色切换）。

### 状态机

行程和订单都有一套完整的状态流转：

- **行程状态**：草稿 → 已发布 → 匹配中 → 已确认 → 进行中 → 已完成
- **订单状态**：待支付 → 已支付 → 进行中 → 已完成 / 已取消
- **邀请状态**：待处理 → 已接受 / 已拒绝

---

## 功能清单

### 通用功能

| 模块 | 功能 | 说明 |
|------|------|------|
| 账户 | 注册 / 登录 | 手机号+密码注册登录，支持用户名 |
| 账户 | 修改密码 | — |
| 账户 | 修改头像 | 上传到 Minio |
| 账户 | 编辑个人资料 | 昵称等 |
| 账户 | 支付密码 | 设置/修改支付密码 |
| 账户 | 个人中心 | 查看个人信息 |
| 认证 | 实名认证 | 身份证正反面照片 + 个人信息 |
| 认证 | 车辆认证 | 车牌号 + 车辆照片（司机必填） |
| 消息 | 消息中心 | WebSocket 实时推送通知 |
| 即时通讯 | 即时消息 | 行程内聊天 |
| AI | AI 助手 | 百度文心一言智能问答 |
| 地图 | 地图选点 | 百度地图选择起点/终点 |
| 地图 | 路径规划 | 展示行程路线 |
| 地图 | 实时位置 | 查看对方实时位置 |

### 乘客端功能

| 功能 | 说明 |
|------|------|
| 乘客首页 | 查看状态概览、快捷入口 |
| 发布行程需求 | 设置起点、终点、出发时间、人数 |
| 我的行程 | 查看已发布的行程需求列表 |
| 行程详情 | 查看行程具体信息 |
| 顺路司机 | 查看与自己行程顺路的司机 |
| 接受邀请 | 司机邀请后，乘客确认接受 |
| 行程确认 | 确认行程匹配，生成订单 |
| 支付 | 微信支付车费 |
| 我的订单 | 查看历史订单 |

### 司机端功能

| 功能 | 说明 |
|------|------|
| 司机首页 | 查看状态概览、快捷入口 |
| 发布行程 | 设置起点、终点、出发时间、可载人数 |
| 我的行程 | 查看已发布的行程列表 |
| 行程详情 | 查看行程具体信息 |
| 顺路乘客 | 查看与自己行程顺路的乘客需求 |
| 邀请乘客 | 向顺路乘客发出拼车邀请 |
| 行程确认 | 乘客接受后，确认行程匹配 |
| 出发 / 送达 | 行程中状态变更（出发→送达） |
| 车辆认证 | 上传车辆信息完成认证 |
| 实名认证 | 上传身份证完成认证 |
| 我的订单 | 查看历史订单 |

---

## 项目结构

```
hitch-trip/
├── backend/                          # 后端 Spring Boot 项目
│   ├── pom.xml                       # Maven 构建文件
│   ├── docker/
│   │   └── Dockerfile                # Docker 镜像构建（Alpine + Java 8）
│   └── src/main/
│       ├── java/com/heima/
│       │   ├── AllinoneApplication.java    # Spring Boot 启动类
│       │   ├── IndexController.java        # 根路由控制器
│       │   ├── account/              # 账户模块（注册/登录/密码/AI助手）
│       │   │   ├── web/              # Controller 层
│       │   │   └── handler/          # 业务处理层
│       │   ├── stroke/               # 行程模块（发布/匹配/邀请）
│       │   │   ├── web/              # Controller 层
│       │   │   ├── handler/          # 业务处理层
│       │   │   └── rabbitmq/         # RabbitMQ 消息队列
│       │   ├── order/                # 订单模块
│       │   │   ├── web/              # Controller 层
│       │   │   └── handler/          # 业务处理层
│       │   ├── payment/              # 支付模块（微信支付）
│       │   │   ├── web/              # Controller 层
│       │   │   └── handler/          # 业务处理层
│       │   ├── notice/               # 通知模块（WebSocket + 定时任务）
│       │   │   ├── web/              # Controller 层
│       │   │   ├── websocket/        # WebSocket 配置
│       │   │   └── schedule/         # 定时任务
│       │   ├── storage/              # 数据访问层（Mapper + Service）
│       │   │   ├── web/              # Controller 层（文件上传等）
│       │   │   ├── mapper/           # MyBatis Mapper 接口
│       │   │   └── service/          # 业务 Service 层
│       │   ├── modules/              # 数据对象
│       │   │   ├── po/               # 持久化对象（实体）
│       │   │   ├── vo/               # 视图对象（API 交互）
│       │   │   └── bo/               # 业务对象
│       │   ├── commons/              # 公共组件
│       │   │   ├── ai/               # 百度 AI 集成
│       │   │   ├── constant/         # 常量定义
│       │   │   ├── enums/            # 枚举（错误码、邀请状态等）
│       │   │   ├── exception/        # 业务异常
│       │   │   ├── initial/          # 请求拦截（Token 认证、参数解析）
│       │   │   ├── state/            # 状态机
│       │   │   └── utils/            # 工具类（雪花ID、Redis、HttpClient等）
│       │   └── configuration/        # 全局配置（异常处理、Swagger、Redis、RabbitMQ等）
│       └── resources/
│           ├── application.yml       # 主配置（激活 wsl profile）
│           ├── application-dev.yml   # dev 环境（远程云服务器）
│           ├── application-vm.yml    # vm 环境（192.168.64.100）
│           ├── application-wsl.yml   # wsl 环境（本地服务，host=wsl）
│           ├── logback.xml           # 日志配置
│           └── mapper/               # MyBatis XML 映射文件
│               ├── AccountMapper.xml
│               ├── AttachmentMapper.xml
│               ├── AuthenticationMapper.xml
│               ├── OrderMapper.xml
│               ├── OrderShadowMapper.xml
│               ├── PaymentMapper.xml
│               ├── StrokeMapper.xml
│               └── VehicleMapper.xml
│
└── frontend/                         # 前端 Vue3 项目
    ├── index.html                    # 入口 HTML（引入百度地图 JS SDK）
    ├── package.json                  # Node 依赖
    ├── vite.config.js                # Vite 配置（端口 3000，API 代理到 9999）
    └── src/
        ├── main.js                   # 应用入口（挂载 Pinia + Router）
        ├── App.vue                   # 根组件（路由过渡 + WebSocket）
        ├── style.css                 # 全局样式
        ├── api/                      # API 请求模块
        │   ├── index.js              # Axios 实例（拦截器、Token 注入）
        │   ├── account.js            # 账户 API
        │   ├── stroke.js             # 行程 API
        │   ├── order.js              # 订单 API
        │   ├── payment.js            # 支付 API
        │   ├── storage.js            # 存储 API
        │   └── notice.js             # 通知 API
        ├── router/index.js           # 路由配置（29 条路由 + Token 鉴权守卫）
        ├── stores/auth.js            # Pinia 认证状态（token/user/role）
        ├── composables/
        │   └── useNotice.js          # WebSocket 通知组合函数
        ├── utils/index.js            # 工具函数
        ├── components/               # 公共组件
        │   ├── TabBar.vue            # 底部导航栏（角色切换）
        │   └── AppHeader.vue         # 顶部导航栏
        └── views/                    # 页面视图（32 个页面）
            ├── Login.vue             # 登录页
            ├── Map.vue               # 地图选点
            ├── Message.vue           # 即时消息
            ├── Point.vue             # 实时位置
            ├── Way.vue               # 路径规划
            ├── passenger/            # 乘客端（8 个页面）
            │   ├── Home.vue          # 乘客首页
            │   ├── TripList.vue      # 我的行程
            │   ├── TripDetail.vue    # 行程详情
            │   ├── TripConfirmed.vue # 行程确认
            │   ├── TripInfo.vue      # 行程信息
            │   ├── TripReq.vue       # 行程需求（顺路司机）
            │   ├── OrderList.vue     # 我的订单
            │   └── Pay.vue           # 支付页面
            ├── driver/               # 司机端（8 个页面）
            │   ├── Home.vue          # 司机首页
            │   ├── TripList.vue      # 我的行程
            │   ├── TripDetail.vue    # 行程详情
            │   ├── TripConfirmed.vue # 行程确认
            │   ├── TripInfo.vue      # 行程信息
            │   ├── TripReq.vue       # 行程需求（顺路乘客）
            │   ├── OrderList.vue     # 我的订单
            │   └── Verify.vue        # 司机认证
            └── common/               # 共用页面（11 个页面）
                ├── UserCenter.vue    # 个人中心
                ├── UserEdit.vue      # 编辑资料
                ├── AlterPwd.vue      # 修改密码
                ├── AlterPhoto.vue    # 修改头像
                ├── AlterPaycode.vue  # 支付密码
                ├── CarInfo.vue       # 车辆信息
                ├── CarVerify.vue     # 车辆认证
                ├── UserVerify.vue    # 实名认证
                ├── UserMsg.vue       # 消息中心
                ├── AccountInfo.vue   # 账户信息
                └── Ai.vue            # AI 助手
```

---

## 本地启动开发环境

### 前置依赖

本地开发需要以下中间件服务：

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL 8 | 3306 | 主数据库，数据库名 `hitch` |
| Redis | 6379 | Session 共享 & 缓存 |
| MongoDB | 27017 | 通知消息存储，数据库 `notice` |
| RabbitMQ | 5672 | 行程匹配消息队列 |
| Minio | 9005 | 文件/图片对象存储 |

> 💡 **简化方案**：所有中间件可通过 Docker Compose 一键启动，或使用已有的远程 dev 环境（`application-dev.yml` 指向 `116.62.213.90`）。

### 方式一：使用 Docker Compose 启动中间件（推荐）

在项目根目录创建 `docker-compose.yml`：

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: hitch-mysql
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: hitch
    command: --default-authentication-plugin=mysql_native_password

  redis:
    image: redis:7-alpine
    container_name: hitch-redis
    ports:
      - "6379:6379"

  mongodb:
    image: mongo:6
    container_name: hitch-mongo
    ports:
      - "27017:27017"

  rabbitmq:
    image: rabbitmq:3-management
    container_name: hitch-rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest

  minio:
    image: minio/minio:latest
    container_name: hitch-minio
    ports:
      - "9005:9000"
      - "9001:9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    command: server /data --console-address ":9001"
    volumes:
      - minio_data:/data

volumes:
  minio_data:
```

启动：

```bash
docker-compose up -d
```

### 方式二：使用已有远程环境

修改 `backend/src/main/resources/application.yml`，将 `active` 改为 `dev`：

```yaml
spring:
  profiles:
    active: dev
```

> dev 环境指向云服务器 `116.62.213.90`，所有中间件已就绪。

### 后端启动

#### 1. 初始化数据库

MySQL 中需要先创建数据库 `hitch`（若使用 Docker Compose 则会自动创建）：

```sql
CREATE DATABASE IF NOT EXISTS hitch DEFAULT CHARACTER SET utf8mb4;
```

> 表结构由 MyBatis 在应用启动时通过 Mapper XML 自动管理（如使用 MyBatis 自动建表功能），或在项目启动后通过 Swagger 接口测试触发创建。

如果需要手动建表，MongoDB 中还需创建数据库 `notice`：

```javascript
use notice;
```

#### 2. 确认配置文件

默认使用 `wsl` profile（`application-wsl.yml`），确保其中各服务的 `host` 指向正确的地址：

- 如果在 Windows 上直接运行中间件，将所有 `host: wsl` 改为 `host: localhost` / `host: 127.0.0.1`
- 或者创建新的 profile 文件 `application-local.yml`，覆盖相关配置

#### 3. 修改 hosts 或 profile

**推荐做法**：创建 `application-local.yml`（放在 `resources/` 下），内容为：

```yaml
server:
  port: 9999

spring:
  redis:
    host: localhost
    port: 6379
    jedis:
      pool:
        max-active: 8
        max-wait: 1000
        max-idle: 500
        min-idle: 0
    lettuce:
      shutdown-timeout: 0
  data:
    mongodb:
      host: localhost
      port: 27017
      database: notice
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/hitch?useUnicode=true&characterEncoding=UTF-8
    username: root
    password: root
    initialSize: 10
    minIdle: 20
    maxActive: 100
    maxWait: 60000
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    publisher-returns: true
    listener:
      simple:
        acknowledge-mode: manual
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

minio:
  url: http://localhost:9005
  host: localhost
  port: 9005
  bucket: hitch
  username: minioadmin
  password: minioadmin

mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.heima.modules.po
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

baidu:
  apikey: 3mWHEPREHzAxeCRq3z6xb8bR
  secretkey: 33fUD7ztZm1v5uBIwiKZFICPIbcXhgju
  map:
    ak: MkIZ2ME6zhPNU1o3BnIzq38Vo38WPz8X
    api: https://api.map.baidu.com/routematrix/v2/driving
```

然后将 `application.yml` 改为：

```yaml
spring:
  profiles:
    active: local
```

#### 4. 启动后端

使用 IDE（IntelliJ IDEA）直接运行 `AllinoneApplication.java` 主类，或在命令行：

```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

> 默认端口：**9999**

启动成功后访问：
- API 文档 (Swagger/Knife4j)：`http://localhost:9999/doc.html`
- 健康检查：`http://localhost:9999/actuator/health`

### 前端启动

#### 1. 安装依赖

```bash
cd frontend
npm install
```

#### 2. 启动开发服务器

```bash
npm run dev
```

> 默认端口：**3000**，已配置代理将 API 请求转发到 `http://localhost:9999`

#### 3. 访问

打开浏览器访问 `http://localhost:3000`

> 📱 建议使用 Chrome DevTools 的移动端模拟模式（iPhone/Android），或直接用手机浏览器访问（需在同一网络下）。

### 开发环境总结

| 组件 | 端口 | 访问地址 |
|------|------|---------|
| 前端 (Vite) | 3000 | `http://localhost:3000` |
| 后端 (Spring Boot) | 9999 | `http://localhost:9999` |
| Swagger 文档 | 9999 | `http://localhost:9999/doc.html` |
| Minio 控制台 | 9001 | `http://localhost:9001` |
| RabbitMQ 管理 | 15672 | `http://localhost:15672` |
| MySQL | 3306 | — |
| Redis | 6379 | — |
| MongoDB | 27017 | — |

### 快速启动检查清单

- [ ] MySQL 8 运行中，数据库 `hitch` 已创建
- [ ] Redis 运行中
- [ ] MongoDB 运行中，数据库 `notice` 已创建
- [ ] RabbitMQ 运行中
- [ ] Minio 运行中，Bucket `hitch` 已创建
- [ ] `application.yml` profile 正确配置
- [ ] 后端 `mvn spring-boot:run` 成功启动（端口 9999）
- [ ] 前端 `npm run dev` 成功启动（端口 3000）
- [ ] 浏览器访问 `http://localhost:3000` 正常显示登录页

---

## API 接口概览

| 模块 | Base URL | 主要接口 |
|------|----------|---------|
| **账户** | `/account/api/` | 注册 register、登录 login、修改密码 modifyPassword、修改支付密码 modifyPaycode、查看个人信息 viewAccount、编辑个人信息 editAccount、AI助手 ai |
| **行程** | `/stroke/api/` | 发布 publish、修改 update、列表 list、详情 detail/{id}、顺路行程 itinerary/list、邀请 invite、邀请列表 invite/list/{tripid}、接受邀请 invite/accept、出发 departure/{tripid}、上车 hitchhiker/{tripid}、下车 freeride/{tripid}、送达 delivery/{tripid} |
| **订单** | `/order/api/` | 列表 list、已支付列表 paidList、详情 view/order/{id}、同行人 fellows |
| **支付** | `/payment/api/` | 生成预支付订单 generateOrder、查询 query |
| **通知** | `/notice/api/` | 消息列表 list、消息计数 count、全部已读 markall |
| **存储** | `/storage/api/` | 文件上传 upload |

> 所有 API 均使用 **POST** 方式（除健康检查外），请求/响应均为 JSON 格式。
> 认证方式：登录后返回 Token（即用户 ID），后续请求通过自定义 Header `SESSION_TOKEN_KEY` 传递。

---

## 数据库表说明

| 表名 | 说明 | 主要字段 |
|------|------|---------|
| `t_account` | 用户账户 | id, username, useralias, password, phone, role(0=乘客/1=司机), avatar, paycode, status |
| `t_stroke` | 行程信息 | id, publisher_id, role, start_geo_lng/lat, start_addr, end_geo_lng/lat, end_addr, quantity, departure_time, quick_confirm, status |
| `t_order` | 订单信息 | id, passenger_id, passenger_stroke_id, driver_id, driver_stroke_id, distance, estimated_time, cost, status |
| `t_order_shadow` | 订单影子表 | 与 t_order 结构相同，用于订单草稿/中间态 |
| `t_payment` | 支付记录 | id, order_id, prepay_id, pay_info, amount, channel, transaction_order_num, status |
| `t_vehicle` | 车辆信息 | id, car_number, car_front_photo, car_back_photo, car_side_photo, purchase_date, phone, status |
| `t_authentication` | 实名认证 | id, useralias, phone, birth, personal_photo, card_id, card_id_front_photo, card_id_back_photo, status |
| `t_attachment` | 附件/文件 | id, name, url, lenght, ext, md5, status |

> 所有表均包含审计字段：`REVISION`, `CREATED_BY`, `CREATED_TIME`, `UPDATED_BY`, `UPDATED_TIME`。
> 主键 ID 使用雪花算法 (Snowflake) 生成。

### AI 新增表

| 表名 | 说明 | 主要字段 |
|------|------|---------|
| `t_chat_message` | AI 聊天消息记录 | id, conversation_id, user_id, role(user/assistant), content, created_time |
| `t_ai_msg` | 资讯库 | id, category, title, pic, content, create_time, update_time |
| `t_ai_files` | 文件库 | id, name, url |
| `t_ai_vector_ids` | 向量ID映射 | id, type, source_id, document_id |

---

## 智能问路（AI 聊天）

> 基于 Spring AI 1.1.8 + 通义千问 (DashScope OpenAI 兼容) 打造的全栈 AI 出行助手。

### 架构

```
Frontend (AiRoute.vue)
  │ POST /ai/chat/stream (SSE)
  ▼
AiChatController ──→ AiChatService ──→ ChatClient (Spring AI)
  │                       │
  │                       ├─ ChatModel (千问 qwen-plus)
  │                       ├─ VectorStore (Redis Stack 知识库检索)
  │                       ├─ WeatherService (Open-Meteo 天气查询)
  │                       ├─ ChatMessageMapper (MySQL 会话记忆)
  │                       └─ FunctionCallback (工具调用)
  ▼
SseEmitter → 逐 token 推送 → 前端流式渲染
```

### API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| **POST** | `/ai/chat` | 非流式对话 |
| **POST** | `/ai/chat/stream` | SSE 流式对话，逐 token 输出 |
| **GET** | `/ai/chat/history/{conversationId}` | 会话历史消息 |
| **GET** | `/ai/chat/conversations` | 用户会话列表（含 lastTime） |
| **GET** | `/ai/chat/messages` | 用户全部聊天记录（跨会话） |

### 功能特性

| 特性 | 说明 |
|------|------|
| **流式输出** | SseEmitter + Flux\<String\>，AI 回复逐字渲染 |
| **会话管理** | MySQL 持久化，支持多会话切换、历史回查 |
| **知识库检索** | Redis Stack KNN 向量搜索，自动注入 System Prompt |
| **天气查询** | FunctionCallback 自动调用 Open-Meteo API |
| **Markdown 渲染** | 前端 `renderMd()` 支持粗体/标题/列表/代码 |
| **文档向量化** | MqReceiver 消息驱动，PDF/DOCX/TXT 自动 Embedding 入库 |
| **多模型** | OpenAI 兼容协议，可切 qwen-plus/turbo/max |

### 向量检索流程

```
文档入库:
  MqReceiver → FileHandler.parseFile()
    ↓ 后缀分流
  PDF → PagePdfDocumentReader
  DOCX → XWPFWordExtractor
  TXT → TextReader
  MD → TextReader
    ↓
  TokenTextSplitter (1000 token/块)
    ↓
  VectorStore.add() → Redis Stack (HNSW + COSINE)
    ↓
  AiVectorIds.save() → 记录 sourceId ↔ documentId 映射

检索：
  AiChatService.chatStream()
    → VectorStore.similaritySearch(query, topK=3)
    → 拼接知识上下文 → 注入 System Prompt
```

### 配置要点

```yaml
spring:
  ai:
    openai:
      api-key: ${DASHSCOPE_API_KEY}
      base-url: https://dashscope.aliyuncs.com/compatible-mode   # 千问 OpenAI 兼容
      chat:
        options:
          model: qwen-plus
          temperature: 0.7
      embedding:
        options:
          model: text-embedding-v1
    vectorstore:
      redis:
        initialize-schema: true
        index: hitch-knowledge
        prefix: "hitch-doc:"
```

---

## 部署说明

### Docker 部署

后端提供 Dockerfile，基于 `anapsix/alpine-java:8_server-jre_unlimited`：

```bash
cd backend
mvn clean package -DskipTests
mvn docker:build
```

### 环境 Profile

| Profile | 说明 | 端口 |
|---------|------|------|
| `wsl` (默认) | 本地 WSL 环境，host=wsl | 9999 |
| `dev` | 远程开发服务器 116.62.213.90 | 8888 |
| `vm` | 虚拟机环境 192.168.64.100 | 8888 |
| `local` | 本地 Windows 环境（需手动创建） | 9999 |
