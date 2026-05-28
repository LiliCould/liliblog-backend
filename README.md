# 立里博客后端 (LiliBlog Backend)

基于 Spring Boot 4.0 + Java 17 构建的个人博客系统后端服务，支持文章发布、评论管理、用户认证、OAuth2 登录、七牛云 OSS 文件存储等功能。

## 技术栈

| 类别 | 技术 |
|------|------|
| **框架** | Spring Boot 4.0.6 |
| **语言** | Java 17 |
| **持久层** | MyBatis Plus 3.5.15 + MySQL 8.0.26 |
| **缓存** | Redis (Lettuce) |
| **安全** | Spring Security + OAuth2 Client (GitHub) |
| **JWT** | JJWT 0.13.0 |
| **对象存储** | 七牛云 Kodo (Java SDK 7.19.0) |
| **Markdown** | flexmark-java 0.64.8 (表格/删除线/自动链接/任务列表/目录) |
| **邮件** | Jakarta Mail |
| **API文档** | NextDoc4j 1.3.1 (SpringDoc OpenAPI) |
| **构建** | Maven + mvnw |
| **其他** | Lombok, Jakarta Validation, AOP (AspectJ), Jackson 3 |

## 项目结构

```
liliblog-backend
└── src/main/java/cn/lilicould/liliblog/
    ├── common/                  # 通用模块
    │   ├── annotation/          # @Audit 操作审计注解
    │   ├── aspect/              # AuditLogAspect AOP切面
    │   ├── cache/               # RedisHelper 缓存工具类
    │   ├── constant/            # 常量定义（状态、Redis前缀、登录类型等）
    │   ├── context/             # BaseContext 安全上下文
    │   ├── enums/               # 枚举（错误码、角色类型、目标类型）
    │   ├── exception/           # BusinessException + 全局异常处理
    │   ├── result/              # 统一响应结果 Result<T>
    │   └── util/                # 工具类（JWT/Markdown/OSS/邮件/IP/分页）
    ├── config/                  # 配置类
    │   ├── cache/               # RedisConfig (RedisTemplate + CacheManager)
    │   ├── mybatis/             # MyBatis Plus 分页 + 自动填充处理器
    │   ├── properties/          # @ConfigurationProperties（JWT/OSS/CORS/Cookie/IP信任/App）
    │   ├── security/            # SecurityConfig + OAuth2 成功/失败处理器
    │   ├── JacksonConfig.java   # Jackson 时间序列化格式
    │   └── OpenAPIConfig.java   # API 文档配置
    ├── controller/
    │   ├── admin/               # 管理员控制器（6个：文章/评论/分类/标签/用户/审计日志）
    │   └── user/                # 用户端控制器（7个：认证/文章/评论/分类/标签/用户/文件）
    ├── domain/security/         # SecurityUser + OAuth2SecurityUser
    ├── filter/                  # 过滤器链（IP限流 → 请求日志 → JWT认证）
    ├── mapper/                  # MyBatis Plus Mapper 接口（8个）
    ├── model/
    │   ├── dto/query/           # 查询参数DTO（7个，继承 BaseQuery 分页基类）
    │   ├── dto/request/         # 请求参数DTO（12个，含 Jakarta Validation）
    │   └── dto/response/        # 响应VO（8个）
    │   └── entity/              # 实体（User/Article/Comment/Category/Tag/ArticleTag/LikeRecord/ChatMessage/AuditLog）
    ├── service/
    │   ├── impl/                # 服务实现（10个）
    │   └── *Service.java        # 服务接口（8个）
    ├── strategy/                # 策略模式：登录策略（接口 + 工厂 + 密码/邮箱实现）
    ├── task/CleanTask.java      # 定时任务（日志清理 + 评论审核提醒）
    └── LiliblogApplication.java # 启动类
```

## 主要功能

### 用户系统
- ✅ 用户名密码登录（BCrypt 加密）
- ✅ 邮箱验证码登录（6位验证码，5分钟有效期，Redis 存储）
- ✅ OAuth2 第三方登录（GitHub）
- ✅ 用户注册
- ✅ JWT 双令牌机制（Access Token + Refresh Token，httpOnly Cookie）
- ✅ 用户信息查看与修改（昵称/邮箱/头像/密码）
- ✅ 策略模式实现多种登录方式，易于扩展

### 文章管理
- ✅ 文章发布、编辑、查询、删除（支持 slug URL 别名）
- ✅ Markdown 内容渲染为 HTML（flexmark 解析，防 XSS）
- ✅ 文章分类与标签关联
- ✅ 文章点赞/取消点赞
- ✅ 文章审核流程（待审核 → 发布/驳回），审核结果邮件通知
- ✅ 文章状态：待审核 / 已发布 / 草稿
- ✅ 全文检索（MySQL FullText Index）
- ✅ 多种查询条件：标题、分类、标签、状态、时间范围

### 评论系统
- ✅ 文章评论（支持多级嵌套）
- ✅ 评论审核机制
- ✅ 评论点赞/取消点赞
- ✅ 评论 IP 地址与 User-Agent 记录
- ✅ 子评论分页查询

### 分类与标签
- ✅ 分类管理（支持排序、开启/关闭）
- ✅ 标签管理（支持颜色标识）
- ✅ 管理员 CRUD + 批量删除

### 文件上传
- ✅ 七牛云 OSS 对象存储
- ✅ 按类型分类存储（封面/头像/图片/文件）
- ✅ UUID 重命名防冲突

### 安全与防护
- ✅ Spring Security 认证授权
- ✅ JWT 无状态认证
- ✅ CSRF 保护（已禁用，适合 API 服务）
- ✅ CORS 跨域配置（可配白名单）
- ✅ IP 限流（Redis 滑动窗口，3秒内最多100次，超限封禁1小时）
- ✅ Markdown XSS 防护（禁止原始 HTML）
- ✅ 请求参数校验（Jakarta Validation）
- ✅ 逻辑删除保护

### 运维与可观测性
- ✅ 操作审计日志（AOP 切面记录所有重要操作）
- ✅ 请求响应日志（含耗时统计，慢请求告警 >1s）
- ✅ 日志滚动记录（按天分割，30天保留，单文件最大100MB）
- ✅ 定时清理过期日志
- ✅ 统一的异常处理与响应格式
- ✅ Spring Boot Actuator 健康检查

### 邮件通知
- ✅ 邮箱验证码发送
- ✅ 文章审核结果通知
- ✅ 评论审核提醒

## API 接口概览

### 认证 `/auth`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/auth/login/pwd` | 密码登录 | 公开 |
| POST | `/auth/login/email` | 邮箱验证码登录 | 公开 |
| GET | `/auth/login/email/code` | 获取邮箱验证码 | 公开 |
| POST | `/auth/register` | 用户注册 | ADMIN |
| POST | `/auth/logout` | 登出 | JWT |
| POST | `/auth/refresh` | 刷新令牌 | Cookie |

### 文章 `/api/article`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/article` | 文章列表（分页+筛选） | 公开 |
| GET | `/api/article/{id}` | 文章详情 | 公开 |
| GET | `/api/article/slug/{slug}` | 按 slug 查询 | 公开 |
| POST | `/api/article` | 创建文章 | JWT |
| PUT | `/api/article/{id}` | 更新文章 | JWT |
| DELETE | `/api/article/{id}` | 删除文章 | JWT |
| PUT | `/api/article/{id}/like` | 点赞 | JWT |
| PUT | `/api/article/{id}/unlike` | 取消点赞 | JWT |
| GET | `/api/article/{id}/like` | 查询是否已点赞 | JWT |

### 评论 `/api/comment`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/comment` | 根评论列表 | 公开 |
| GET | `/api/comment/child` | 子评论列表 | 公开 |
| POST | `/api/comment` | 发表评论 | JWT |
| DELETE | `/api/comment/{id}` | 删除评论（含子评论） | JWT |
| PUT | `/api/comment/{id}/like` | 点赞评论 | JWT |
| PUT | `/api/comment/{id}/unlike` | 取消点赞 | JWT |
| GET | `/api/comment/{id}/like` | 查询是否已点赞 | JWT |

### 分类与标签

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/category` | 分类列表 | 公开 |
| GET | `/api/category/{id}` | 分类详情 | 公开 |
| POST | `/api/category` | 创建分类 | ADMIN |
| PUT | `/api/category/{id}` | 更新分类 | ADMIN |
| DELETE | `/api/category/{id}` | 删除分类 | ADMIN |
| GET | `/api/tag` | 标签列表 | 公开 |
| GET | `/api/tag/{id}` | 标签详情 | 公开 |

### 用户 `/api/user`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/user/{id}` | 用户信息 | 公开 |
| GET | `/api/user/me` | 当前用户信息 | JWT |
| PUT | `/api/user` | 更新个人信息/密码 | JWT |

### 文件 `/file`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/file/upload` | 上传文件（封面/头像/图片/文件） | JWT |

### 管理员接口（全部需要 ADMIN 角色）

| 路径 | 功能 |
|------|------|
| `/api/admin/article` | 文章列表/详情/审核/删除/批量删除 |
| `/api/admin/comment` | 评论列表/删除/批量删除/审核 |
| `/api/admin/category` | 分类 CRUD + 批量删除 |
| `/api/admin/tag` | 标签 CRUD + 批量删除 |
| `/api/admin/user` | 用户 CRUD + 状态切换 |
| `/api/admin/auditLog` | 审计日志查询/删除/批量删除 |

## 数据库

- **数据库类型**: MySQL 8.x
- **初始化脚本**: `src/main/resources/init.sql`
- **包含表**: user, article, category, tag, article_tag, comment, like_record, chat_message, audit_log
- **种子数据**: 管理员用户、3个分类、31个标签、约20篇文章
- **逻辑删除**: 全局 `deleted` 字段（0-未删除, 1-已删除）
- **自动填充**: 创建/更新时间、创建/更新人

## 快速开始

### 前置要求

- JDK 17+
- Maven 3.9+（或使用 `mvnw`）
- MySQL 8.0+
- Redis 6.0+

### 配置说明

1. **复制配置文件**
```bash
cp src/main/resources/application-demo.yml src/main/resources/application-dev.yml
```

2. **修改 `application-dev.yml`**，填入数据库、Redis、JWT、七牛云、邮箱等配置

3. **初始化数据库**
```bash
mysql -u root -p < src/main/resources/init.sql
```

### 运行项目

```bash
# 方式1: Maven 直接运行（开发推荐）
mvn spring-boot:run

# 方式2: 打包后运行
mvn clean package -DskipTests
java -jar target/liliblog-2.0.1.jar

# 方式3: IDE 中直接运行 LiliblogApplication.java
```

项目启动后访问：
- **API文档**: http://localhost:8080/swagger-ui.html
- **NextDoc4j**: http://localhost:8080/doc.html

## 测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=JwtUtilTest

# 跳过测试构建
mvn package -DskipTests
```

## License

MIT License

## 作者

lilicould - lilicould@qq.com

## 链接

- [线上体验](https://lilicould.cn)
- [GitHub 仓库](https://github.com/LiliCould/liliblog-backend)
