# LiliBlog 立里博客系统

## 项目简介

LiliBlog 是一个基于 **Spring Boot 4.0** + **MyBatis** 的现代化博客后端系统，采用经典 MVC 架构设计。系统集成了用户认证、文章管理、分类标签、实时聊天室等完整功能，支持 Markdown 渲染、Redis 缓存、WebSocket 即时通讯等特性，为个人博客或小型社区提供完善的后端解决方案。

## 前端项目地址
[前端项目地址](https://github.com/LiliCould/liliblog-frontend "前端地址")

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 核心框架 | Spring Boot | 4.0.1 |
| ORM框架 | MyBatis | 4.0.1 |
| 数据库 | MySQL | 8.x |
| 缓存 | Redis | - |
| 认证方式 | JWT (JJWT) | 0.12.3 |
| 密码加密 | Spring Security Crypto (BCrypt) | 5.8.1 |
| 即时通讯 | WebSocket (Jakarta) | - |
| 邮件服务 | JavaMail (Spring Mail) | - |
| Markdown渲染 | CommonMark | 0.21.0 |
| HTML解析 | Jsoup | 1.14.3 |
| AOP编程 | AspectJ | 1.9.21 |
| 文件上传 | Commons FileUpload | 1.5 |
| 工具库 | Lombok | 1.18.42 |
| Java版本 | JDK | 21 |
| 构建工具 | Maven | 3.x |

## 核心功能

### ✅ 已实现功能

#### 1. 用户认证系统
- 用户注册（邮箱验证码验证）
- 用户登录（JWT令牌颁发）
- 修改密码
- 获取/更新用户信息
- 用户登出
- JWT拦截器自动认证

#### 2. 文章管理系统
- 文章创建（支持Markdown内容）
- 文章更新与删除
- 文章分页查询
- 通过ID或Slug获取文章
- 浏览量统计（Redis缓存）
- 文章搜索（模糊匹配）
- 置顶/推荐标记
- 公开文章接口（无需认证）

#### 3. 分类管理
- 分类CRUD操作
- 支持层级分类结构
- 自定义排序和图标

#### 4. 标签管理
- 标签CRUD操作
- 自定义颜色标识
- 文章-标签多对多关联

#### 5. 评论系统
- 评论创建（记录IP和User-Agent）
- 评论列表查询
- 支持回复评论

#### 6. 实时聊天室 ⭐
- WebSocket长连接通信
- 实时消息广播
- 在线用户列表展示
- 消息持久化存储
- 心跳检测机制
- 系统通知（进入/离开提示）
- 聊天历史记录查询

#### 7. 文件上传
- 多类型文件上传支持
- 按日期组织存储路径
- 文件大小限制（10MB）

#### 8. 缓存与性能优化
- Redis缓存热点数据
- 验证码临时存储（5分钟过期）
- 浏览量异步更新
- 连接池配置优化

#### 9. 安全特性
- BCrypt密码加密
- JWT无状态认证
- CORS跨域配置
- 请求参数校验（Bean Validation）
- IP地址记录
- 统一异常处理

#### 10. 运维与监控
- AOP操作日志记录（自定义@Log注解）
- 请求日志拦截器
- Spring Actuator健康检查
- Logback日志配置

## 项目结构

```
src/main/java/com/lilicould/blog/
├── LiliBlogApplication.java        # 启动类
├── annotation/                     # 自定义注解
│   └── Log.java                    # 日志记录注解
├── aop/                            # 切面编程
│   └── ServiceLogAspect.java       # 服务日志切面
├── config/                         # 配置类
│   ├── JwtConfig.java              # JWT配置
│   ├── RedisConfig.java            # Redis配置
│   ├── WebMvcConfig.java           # Web MVC配置
│   └── WebSocketConfig.java        # WebSocket配置
├── controller/                     # 控制器层
│   ├── AuthController.java         # 认证控制器
│   ├── ArticleController.java      # 文章控制器
│   ├── CategoryController.java     # 分类控制器
│   ├── TagController.java          # 标签控制器
│   ├── CommentController.java      # 评论控制器
│   ├── ChatMessageController.java  # 聊天消息控制器
│   ├── FileUploadController.java   # 文件上传控制器
│   └── PublicController.java       # 公开接口控制器
├── dao/                            # 数据访问层（Mapper接口）
│   ├── UserMapper.java
│   ├── ArticleMapper.java
│   ├── CategoryMapper.java
│   ├── TagMapper.java
│   ├── CommentMapper.java
│   ├── ArticleTagMapper.java
│   └── ChatMessageMapper.java
├── dto/                            # 数据传输对象
│   ├── LoginDTO.java / RegisterDTO.java
│   ├── ArticleCreateDTO.java / ArticleUpdateDTO.java
│   ├── CategoryCreateDTO.java / CategoryUpdateDTO.java
│   ├── TagCreateDTO.java / TagUpdateDTO.java
│   ├── CommentCreateDTO.java / CommentUpdateDTO.java
│   ├── ChatMessageDTO.java
│   └── PasswordChangeDTO.java / UserUpdateDTO.java
├── entity/                         # 实体类
│   ├── User.java / Article.java
│   ├── Category.java / Tag.java
│   ├── Comment.java / LikeRecord.java
│   ├── ChatMessage.java / MailBean.java
├── exception/                      # 异常处理
│   ├── BusinessException.java      # 业务异常
│   └── GlobalExceptionHandler.java # 全局异常处理器
├── interceptor/                    # 拦截器
│   ├── JwtAuthenticationInterceptor.java  # JWT认证拦截器
│   └── ControllerLogInterceptor.java     # 控制器日志拦截器
├── service/                        # 服务接口层
│   ├── AuthService.java
│   ├── ArticleService.java
│   ├── CategoryService.java
│   ├── TagService.java
│   ├── ChatMessageService.java
│   └── FileUploadService.java
├── service/impl/                   # 服务实现层
│   ├── AuthServiceImpl.java
│   ├── ArticleServiceImpl.java
│   ├── CategoryServiceImpl.java
│   ├── TagServiceImpl.java
│   ├── ChatMessageServiceImpl.java
│   └── FileUploadServiceImpl.java
├── socket/                         # WebSocket
│   └── ChatWebSocketEndpoint.java  # 聊天端点
├── util/                           # 工具类
│   ├── JwtUtil.java                # JWT工具
│   ├── PasswordUtil.java           # 密码工具
│   ├── RedisUtil.java              # Redis工具
│   ├── MailUtil.java               # 邮件工具
│   ├── MarkdownUtil.java           # Markdown工具
│   ├── RequestUtil.java            # 请求工具
│   └── BaseContextUtil.java        # 上下文工具
└── vo/                             # 视图对象
    ├── ResultVO.java               # 统一响应格式
    ├── LoginVO.java                # 登录响应VO
    └── UserVO.java                 # 用户信息VO

src/main/resources/
├── application.yml                 # 主配置文件
├── application-dev.yml             # 开发环境配置
├── BlogInit.sql                    # 数据库初始化脚本
├── logback-spring.xml              # 日志配置
├── mapper/                         # MyBatis XML映射文件
│   ├── UserMapper.xml
│   ├── ArticleMapper.xml
│   ├── CategoryMapper.xml
│   ├── TagMapper.xml
│   ├── CommentMapper.xml
│   ├── ArticleTagMapper.xml
│   └── ChatMessageMapper.xml
└── static/avatar/                  # 默认头像资源
```

## 数据库设计

### ER关系图

```
user_tb (用户)
    │
    ├────< article (文章)
    │         │
    │         ├─┼─< comment (评论)
    │         │
    │         └──┼──< article_tag >──┼── tag (标签)
    │
    ├────< like_record (点赞记录)
    │
    └────< chat_message (聊天消息)

category (分类) ──< article (文章)
```

### 核心数据表

#### user_tb 用户表
```sql
CREATE TABLE `user_tb` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) UNIQUE NOT NULL,
  `email` VARCHAR(100) UNIQUE NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `nickname` VARCHAR(50) NOT NULL,
  `avatar` VARCHAR(255),
  `role` ENUM('ADMIN','VISITOR') DEFAULT 'VISITOR',
  `status` TINYINT DEFAULT 1,
  `last_login_time` DATETIME,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### article 文章表
```sql
CREATE TABLE `article` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `slug` VARCHAR(200) UNIQUE,
  `summary` VARCHAR(500),
  `content` LONGTEXT NOT NULL,
  `content_html` LONGTEXT,
  `cover_image` VARCHAR(255),
  `status` ENUM('DRAFT','PUBLISHED','HIDDEN','DELETED') DEFAULT 'DRAFT',
  `view_count` INT DEFAULT 0,
  `like_count` INT DEFAULT 0,
  `comment_count` INT DEFAULT 0,
  `is_top` TINYINT DEFAULT 0,
  `is_recommend` TINYINT DEFAULT 0,
  `author_id` BIGINT NOT NULL,
  `category_id` BIGINT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `publish_time` DATETIME,
  FULLTEXT INDEX `ft_title_content` (`title`, `content`)
);
```

#### 其他表
- **category**: 分类表（支持层级结构）
- **tag**: 标签表
- **article_tag**: 文章-标签关联表（多对多）
- **comment**: 评论表（支持回复）
- **like_record**: 点赞记录表
- **chat_message**: 聊天消息表

> 📄 完整建表语句请查看 [BlogInit.sql](src/main/resources/BlogInit.sql)

## API接口文档

### 基础信息
- **Base URL**: `http://localhost:8080`
- **认证方式**: Bearer Token (JWT)
- **统一响应格式**: `ResultVO<T>`

### 认证模块 `/api/auth`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/auth/login` | 用户登录 | ❌ |
| POST | `/api/auth/register` | 用户注册 | ❌ |
| GET | `/api/auth/captcha?email=xxx` | 获取邮箱验证码 | ❌ |
| PUT | `/api/auth/password` | 修改密码 | ✅ |
| GET | `/api/auth/profile` | 获取当前用户信息 | ✅ |
| PUT | `/api/auth/update` | 更新用户信息 | ✅ |
| POST | `/api/auth/logout` | 用户登出 | ✅ |

### 文章模块 `/api/article`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/article` | 获取所有文章（分页） | ❌ |
| GET | `/api/article/{id}` | 根据ID获取文章 | ❌ |
| GET | `/api/article/slug/{slug}` | 根据Slug获取文章 | ❌ |
| POST | `/api/article` | 创建文章 | ✅ |
| PUT | `/api/article/{id}` | 更新文章 | ✅ |
| DELETE | `/api/article/{id}` | 删除文章 | ✅ |

### 公开接口 `/api/public`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/public/articles` | 获取公开文章列表 | ❌ |
| GET | `/api/public/article/{id}` | 获取公开文章详情 | ❌ |
| GET | `/api/public/article/slug/{slug}` | 根据Slug获取文章 | ❌ |
| GET | `/api/public/search?keyword=xxx` | 搜索文章 | ❌ |

### 分类模块 `/api/categories`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/categories` | 获取分类列表 | ❌ |
| GET | `/api/categories/{id}` | 获取分类详情 | ❌ |
| POST | `/api/categories` | 创建分类 | ✅ |
| PUT | `/api/categories` | 更新分类 | ✅ |
| DELETE | `/api/categories/{id}` | 删除分类 | ✅ |

### 标签模块 `/api/tag`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/tag/list` | 获取所有标签 | ❌ |
| GET | `/api/tag/{tagName}` | 获取标签详情 | ❌ |
| POST | `/api/tag` | 创建标签 | ✅ |
| PUT | `/api/tag` | 更新标签 | ✅ |
| DELETE | `/api/tag/{tagName}` | 删除标签 | ✅ |

### 评论模块 `/api/comment`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/comment` | 获取评论列表 | ❌ |
| POST | `/api/comment` | 创建评论 | ✅ |

### 聊天模块 `/api/chat` & WebSocket

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| WS | `/ws/chat` | WebSocket聊天连接 | ✅ |
| GET | `/api/chat/messages` | 获取聊天历史记录 | ✅ |

### 文件上传 `/api/file`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/file?type=xxx` | 上传文件 | ✅ |

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 安装步骤

#### 1. 克隆项目
```bash
git clone <repository-url>
cd liliblog-backend
```

#### 2. 配置数据库
```bash
# 创建数据库
mysql -u root -p < src/main/resources/BlogInit.sql
```

#### 3. 修改配置文件
编辑 `src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/liliblog?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
  mail:
    host: smtp.qq.com
    port: 587
    username: your_email@qq.com
    password: your_email_auth_code
```

#### 4. 启动项目
```bash
# 方式一：Maven命令行
mvn spring-boot:run

# 方式二：IDEA直接运行LiliBlogApplication.main()
```

#### 5. 验证启动
访问：http://localhost:8080

### Docker部署（可选）

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/liliblog.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# 构建并运行
docker build -t liliblog .
docker run -p 8080:8080 liliblog
```

## 配置说明

### application.yml 主配置
```yaml
spring:
  profiles:
    default: dev  # 默认使用dev环境
```

### application-dev.yml 开发环境配置

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| spring.datasource.* | 数据库连接配置 | - |
| spring.data.redis.* | Redis连接配置 | localhost:6379 |
| spring.mail.* | 邮件服务配置（QQ邮箱） | - |
| mybatis.* | MyBatis配置 | 驼峰命名转换 |
| jwt.secret | JWT密钥 | - |
| jwt.expire | JWT过期时间(ms) | 604800000 (7天) |
| server.base-url | 服务基础URL | http://localhost:8080 |
| file.upload.path | 文件上传路径 | ./uploads/ |
| spring.servlet.multipart.max-file-size | 单文件大小限制 | 10MB |

## 开发规范

### 代码风格
- 使用 **Lombok** 减少样板代码（@Data, @Slf4j, @Builder等）
- 实体类统一使用 **驼峰命名**
- Service层添加 **@Transactional** 事务管理
- 关键业务方法添加 **@Log** 注解记录操作日志
- DTO用于接收参数，VO用于返回数据

### 分层架构规范
```
Controller → Service → Mapper → Database
    ↓           ↓
   DTO        Entity
    ↓           ↓
   VO    ←  ResultVO<T>
```

### 安全规范
- 所有密码必须使用 **BCrypt** 加密存储
- 敏感接口必须通过 **JWT拦截器** 认证
- 输入参数使用 **@Valid** + Bean Validation 校验
- 业务异常统一抛出 **BusinessException**

### API设计规范
- 遵循 **RESTful** 风格设计接口
- 统一使用 **ResultVO<T>** 包装响应
- 认证接口通过请求头传递Token：`Authorization: Bearer {token}`
- 分页参数：`pageNum`(页码), `pageSize`(每页数量)

## 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        Client                               │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTP/WebSocket
┌──────────────────────▼──────────────────────────────────────┐
│                    Controller Layer                         │
│  (AuthController, ArticleController, ...)                   │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│              Interceptor Layer                              │
│  (JwtAuthenticationInterceptor, ControllerLogInterceptor)   │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                   Service Layer                             │
│  (AuthService, ArticleService, ...)                         │
│         ↓                                                   │
│  ┌─────────────────┐                                        │
│  │ AOP Log Aspect  │ ← @Log 注解                           │
│  └─────────────────┘                                        │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                    DAO Layer                                │
│  (UserMapper, ArticleMapper, ...)                           │
│         ↓                                                   │
│  ┌─────────────────┐    ┌─────────────────┐                │
│  │    MySQL        │    │     Redis       │                │
│  └─────────────────┘    └─────────────────┘                │
└─────────────────────────────────────────────────────────────┘
                        │
┌──────────────────────▼──────────────────────────────────────┐
│              External Services                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │ SMTP Mail   │  │ WebSocket   │  │ File System │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
```

## 特色功能详解

### 🔐 JWT认证流程
```
客户端 → 登录请求 → 服务端验证 → 生成JWT Token → 返回Token
                                                    ↓
后续请求 → 携带Token → JwtInterceptor解析 → 注入用户信息 → 处理请求
```

### 💬 WebSocket聊天室
- **端点**: `/ws/chat`
- **认证**: 通过HandshakeInterceptor从Token中提取userId
- **特性**:
  - 实时消息广播
  - 在线用户列表同步
  - 心跳保活（PING/PONG）
  - 消息持久化到数据库
  - 系统事件通知（进入/离开）

### 📧 邮箱验证码
- 使用QQ邮箱SMTP服务发送
- 验证码存储在Redis，5分钟过期
- 异步发送，不阻塞主线程
- 防止重复发送（频率限制）

### 📝 Markdown渲染
- 使用CommonMark库解析Markdown
- 自动生成HTML摘要
- 支持代码高亮扩展

## 常见问题

### Q: 如何重置管理员密码？
A: 直接通过数据库更新user_tb表的password字段（BCrypt加密后的值）。

### Q: 如何修改JWT过期时间？
A: 在`application-dev.yml`中修改`jwt.expire`配置项（单位：毫秒）。

### Q: 文件上传到哪里了？
A: 默认存储在项目根目录的`./uploads/`文件夹下，可通过`file.upload.path`配置。

### Q: Redis连接失败怎么办？
A: 检查Redis服务是否启动，确认application-dev.yml中的Redis配置正确。

## 后续规划

- [ ] 引入Spring Security完善权限控制
- [ ] 添加文章点赞/收藏功能
- [ ] 实现评论审核工作流
- [ ] 集成Elasticsearch全文搜索
- [ ] 添加定时任务（文章归档、数据统计）
- [ ] 实现文件云存储（OSS/COS）
- [ ] 添加API限流和防刷机制
- [ ] 完善单元测试和集成测试
- [ ] 添加Swagger/Knife4j接口文档
- [ ] 支持多主题切换

## License

MIT License

## 作者

**lilicould** - [GitHub](https://github.com/lilicould)

---

> 💡 如果这个项目对你有帮助，欢迎给个 Star ⭐
