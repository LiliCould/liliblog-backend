# LILIBLOG - 开发文档

## 项目概述

LILIBLOG是一个基于Spring + MyBatis基础版（非SpringBoot/PLUS）+ SpringMVC的纯后端博客系统。本项目采用经典的MVC架构，实现了用户认证、文章管理等核心功能，并为后续功能扩展提供了良好的架构基础。

## 数据库详细结构

### 完整建表语句

#### User表
```sql
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `email` varchar(100) NOT NULL COMMENT '邮箱',
  `password` varchar(100) NOT NULL COMMENT '密码（加密存储）',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `bio` varchar(500) DEFAULT NULL COMMENT '个人简介',
  `role` varchar(20) DEFAULT 'USER' COMMENT '角色',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时间',
  `login_count` int(11) DEFAULT 0 COMMENT '登录次数',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

#### Article表
```sql
CREATE TABLE `article` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` longtext NOT NULL COMMENT '内容',
  `excerpt` varchar(500) DEFAULT NULL COMMENT '摘要',
  `author_id` bigint(20) NOT NULL COMMENT '作者ID',
  `category_id` bigint(20) DEFAULT NULL COMMENT '分类ID',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态 1-已发布 0-草稿',
  `view_count` int(11) DEFAULT 0 COMMENT '浏览量',
  `comment_count` int(11) DEFAULT 0 COMMENT '评论数',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `published_at` datetime DEFAULT NULL COMMENT '发布时间',
  `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图片',
  `is_top` tinyint(4) DEFAULT 0 COMMENT '是否置顶 1-是 0-否',
  `is_recommend` tinyint(4) DEFAULT 0 COMMENT '是否推荐 1-是 0-否',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签列表（JSON格式）',
  PRIMARY KEY (`id`),
  KEY `idx_author_id` (`author_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_published_at` (`published_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';
```

#### Category表
```sql
CREATE TABLE `category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `slug` varchar(50) NOT NULL COMMENT '分类标识',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父分类ID 0-顶级分类',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  UNIQUE KEY `uk_slug` (`slug`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';
```

#### Comment表
```sql
CREATE TABLE `comment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `content` text NOT NULL COMMENT '评论内容',
  `article_id` bigint(20) NOT NULL COMMENT '文章ID',
  `author_id` bigint(20) DEFAULT NULL COMMENT '作者ID',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父评论ID 0-顶级评论',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态 1-已审核 0-待审核',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(255) DEFAULT NULL COMMENT '用户代理',
  `is_author_reply` tinyint(4) DEFAULT 0 COMMENT '是否作者回复 1-是 0-否',
  `likes` int(11) DEFAULT 0 COMMENT '点赞数',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_author_id` (`author_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';
```

#### Tag表
```sql
CREATE TABLE `tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(50) NOT NULL COMMENT '标签名称',
  `slug` varchar(50) NOT NULL COMMENT '标签标识',
  `color` varchar(20) DEFAULT '#999999' COMMENT '标签颜色',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  UNIQUE KEY `uk_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';
```

## 待实现接口

### 分类管理接口

#### GET /api/categories
- 描述: 获取分类列表
- 公开接口: 是

#### GET /api/categories/{id}
- 描述: 获取分类详情
- 公开接口: 是

#### POST /api/categories
- 描述: 创建分类
- 参数: name, slug, parentId, sortOrder, description, icon
- 需认证: 是
- 权限要求: ADMIN

#### PUT /api/categories/{id}
- 描述: 更新分类
- 参数: name, slug, parentId, sortOrder, description, icon, status
- 需认证: 是
- 权限要求: ADMIN

#### DELETE /api/categories/{id}
- 描述: 删除分类
- 需认证: 是
- 权限要求: ADMIN

### 评论管理接口

#### GET /api/comments
- 描述: 获取评论列表
- 公开接口: 否
- 需认证: 是

#### GET /api/comments/article/{articleId}
- 描述: 获取文章评论列表
- 公开接口: 是

#### POST /api/comments
- 描述: 创建评论
- 参数: articleId, content, parentId
- 需认证: 是

#### PUT /api/comments/{id}
- 描述: 更新评论
- 参数: content, status
- 需认证: 是

#### DELETE /api/comments/{id}
- 描述: 删除评论
- 需认证: 是

### 标签管理接口

#### GET /api/tags
- 描述: 获取标签列表
- 公开接口: 是

#### POST /api/tags
- 描述: 创建标签
- 参数: name, slug, color
- 需认证: 是
- 权限要求: ADMIN

#### PUT /api/tags/{id}
- 描述: 更新标签
- 参数: name, slug, color
- 需认证: 是
- 权限要求: ADMIN

#### DELETE /api/tags/{id}
- 描述: 删除标签
- 需认证: 是
- 权限要求: ADMIN

## 所需工具类

### 1. JwtUtils
- 功能: JWT令牌生成、解析和验证
- 主要方法:
  - generateToken: 根据用户信息生成JWT令牌
  - parseToken: 解析JWT令牌，获取用户信息
  - validateToken: 验证JWT令牌是否有效

### 2. PasswordUtils
- 功能: 密码加密和验证
- 主要方法:
  - encodePassword: 对密码进行加密
  - matchPassword: 验证密码是否匹配

## 功能实现方式

### 1. 用户认证实现
- 使用JWT进行身份认证
- 自定义拦截器JwtInterceptor验证请求中的token
- 密码使用BCrypt加密算法存储

### 2. 文章浏览量统计
- 使用Redis缓存记录文章浏览量，定时同步到数据库
- 防止重复计数，基于IP或用户会话进行判断

### 3. 文件上传实现
- 使用MultipartFile接收上传文件
- 按照日期和类型组织文件存储路径
- 文件存储在服务器指定目录，数据库存储访问路径

### 4. 缓存机制
- 使用Redis缓存热点数据（热门文章、分类统计等）
- 实现缓存更新策略（Cache Aside Pattern）
- 设置合理的缓存过期时间

### 5. AOP日志实现
- 自定义@Log注解标记需要记录日志的方法
- 实现LogAspect切面类拦截带有@Log注解的方法
- 记录操作人、操作时间、操作类型、请求参数等信息

### 6. 数据验证实现
- 使用Bean Validation API进行请求参数验证
- 自定义验证注解处理复杂业务规则
- 统一异常处理类捕获验证异常并返回友好提示
```

## 技术栈

- **框架**: Spring 6.1.13 + Spring MVC + MyBatis 3.5.16
- **数据库**: MySQL
- **连接池**: Druid
- **日志**: Logback
- **安全**: JWT (JSON Web Token) + Spring Security Crypto
- **验证**: Bean Validation API
- **工具库**: Lombok, JJWT
- **构建工具**: Maven
- **Java版本**: JDK 17

## 项目结构

```
src/main/java/com/lilicould/blog/
├── annotation/     # 自定义注解
├── aop/            # 面向切面编程
├── config/         # 配置类
├── controller/     # 控制器
├── dao/            # 数据访问层
├── dto/            # 数据传输对象
├── entity/         # 实体类
├── exception/      # 异常处理
├── interceptor/    # 拦截器
├── service/        # 业务逻辑层
│   └── impl/       # 业务逻辑实现
├── util/           # 工具类
└── vo/             # 视图对象
```

## 核心功能

### 已实现功能

#### 1. 用户认证与授权
- JWT令牌生成与验证
- 用户登录（密码加密验证）
- 用户注册（用户名/邮箱唯一性检查）
- 修改密码
- 获取用户信息
- 基于JWT的认证拦截器

#### 2. 文章管理
- 文章创建
- 文章更新
- 文章删除
- 文章查询（单篇和列表）

#### 3. 系统配置
- 数据库连接配置（Druid连接池）
- MyBatis配置（驼峰命名转换）
- Spring MVC配置（JSON转换、CORS跨域、拦截器）
- 全局异常处理
- 统一响应格式

#### 4. 数据模型
- User（用户）
- Article（文章）
- Category（分类）
- Comment（评论）
- Tag（标签）

#### 5. 安全特性
- 密码加密存储
- JWT令牌认证
- 请求拦截与权限控制
- CORS安全配置

### 待实现功能

#### 1. 文章管理增强
- 文章浏览量统计（incrementViewCount方法未实现）
- 文章搜索功能完整实现
- 文章标签关联管理
- 文章分类管理

#### 2. 评论系统
- 评论的增删改查功能
- 评论回复功能
- 评论审核功能

#### 3. 分类和标签管理
- 分类的增删改查功能
- 标签的增删改查功能

#### 4. 用户管理
- 用户角色权限完整实现
- 用户信息编辑
- 用户头像上传

#### 5. AOP日志
- Log注解的AOP实现（LogAspect类未找到）

#### 6. 控制器完善
- ArticleController需要解除注释并完善
- 其他资源的Controller实现（Category、Comment、Tag等）

#### 7. Web配置
- web.xml配置文件缺失（Maven已配置禁用web.xml检查）

#### 8. 其他功能
- 文件上传功能
- 缓存机制
- 定时任务
- 富文本编辑器支持

## 数据库模型

### User表
- id: 用户ID
- username: 用户名
- email: 邮箱
- password: 密码（加密存储）
- avatar: 头像
- bio: 个人简介
- role: 角色
- status: 状态
- createdAt: 创建时间
- updatedAt: 更新时间
- lastLoginAt: 最后登录时间
- loginCount: 登录次数

### Article表
- id: 文章ID
- title: 标题
- content: 内容
- excerpt: 摘要
- authorId: 作者ID
- categoryId: 分类ID
- status: 状态
- viewCount: 浏览量
- commentCount: 评论数
- createdAt: 创建时间
- updatedAt: 更新时间
- publishedAt: 发布时间
- coverImage: 封面图片
- isTop: 是否置顶
- isRecommend: 是否推荐
- tags: 标签列表（JSON格式）

### Category表
- id: 分类ID
- name: 分类名称
- slug: 分类标识
- parentId: 父分类ID
- sortOrder: 排序
- description: 描述
- createdAt: 创建时间
- updatedAt: 更新时间
- status: 状态
- icon: 图标

### Comment表
- id: 评论ID
- content: 评论内容
- articleId: 文章ID
- authorId: 作者ID
- parentId: 父评论ID
- status: 状态
- createdAt: 创建时间
- updatedAt: 更新时间
- ipAddress: IP地址
- userAgent: 用户代理
- isAuthorReply: 是否作者回复
- likes: 点赞数

### Tag表
- id: 标签ID
- name: 标签名称
- slug: 标签标识
- color: 标签颜色
- createdAt: 创建时间
- updatedAt: 更新时间

## API接口说明

### 认证接口

#### POST /api/auth/login
- 描述: 用户登录
- 参数: username, password
- 返回: JWT令牌和用户信息

#### POST /api/auth/register
- 描述: 用户注册
- 参数: username, email, password
- 返回: 注册结果

#### PUT /api/auth/password
- 描述: 修改密码
- 参数: oldPassword, newPassword
- 需认证: 是

#### GET /api/auth/profile
- 描述: 获取用户信息
- 需认证: 是

#### POST /api/auth/logout
- 描述: 用户登出
- 需认证: 是

### 文章接口

#### GET /api/articles
- 描述: 获取文章列表
- 公开接口: 是

#### GET /api/articles/{id}
- 描述: 获取单篇文章
- 公开接口: 是

#### POST /api/articles
- 描述: 创建文章
- 参数: 文章信息
- 需认证: 是

#### PUT /api/articles/{id}
- 描述: 更新文章
- 参数: 文章信息
- 需认证: 是

#### DELETE /api/articles/{id}
- 描述: 删除文章
- 需认证: 是

## 系统配置

### 数据库配置（application.properties）
```properties
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/liliblog?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=123456
```

### MyBatis配置
```properties
mybatis.mapper-locations=classpath:mapper/*.xml
mybatis.configuration.map-underscore-to-camel-case=true
### JWT配置
```properties
jwt.secret=jwt.secret=mynameisliliandiamabackenddeveloper
jwt.expire=86400000
```

### 上传配置
```properties
upload.temp.dir=C:\Users\Administrator\AppData\Local\Temp
```

## 开发规范

### 代码规范
- 使用Lombok减少模板代码
- 实体类使用@Data、@NoArgsConstructor、@AllArgsConstructor注解
- 业务方法使用@Transactional管理事务
- 关键操作使用@Log注解记录日志

### 安全规范
- 密码必须使用Spring Security Crypto加密存储
- 敏感操作必须验证用户身份
- 使用JWT进行无状态认证
- 接口参数必须进行验证

### 错误处理
- 业务逻辑错误抛出BusinessException
- 使用GlobalExceptionHandler统一处理异常
- API响应使用ResultVO统一格式

## 部署说明

1. 配置数据库：创建名为liliblog的数据库
2. 配置application.properties中的数据库连接信息
3. 使用Maven构建项目：`mvn clean package`
4. 部署生成的war包到Tomcat或其他Java Web服务器

## 开发注意事项

1. 项目是基于Spring+MyBatis基础版，非SpringBoot/PLUS
2. 所有Controller方法都应返回统一的ResultVO格式
3. 敏感操作必须添加@Log注解记录操作日志
4. 数据库操作必须在Service层通过事务管理
5. 新增实体类时，需要同时创建对应的Mapper接口和XML文件
6. 所有输入参数必须进行验证，使用Bean Validation API
7. 接口设计应遵循RESTful风格

## 后续开发计划

1. 完成文章管理增强功能
2. 实现评论系统
3. 完善分类和标签管理
4. 增强用户权限管理
5. 添加文件上传功能
6. 实现缓存机制提升性能
7. 添加富文本编辑器支持
8. 实现数据统计和报表功能
