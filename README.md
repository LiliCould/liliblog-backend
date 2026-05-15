# 立里博客后端 (LiliBlog Backend)

基于 Spring Boot 4.0 构建的个人博客系统后端服务，支持文章发布、评论管理、用户认证、OAuth2 登录等功能。

## 技术栈

- **框架**: Spring Boot 4.0.6
- **语言**: Java 17
- **持久层**: MyBatis Plus 3.5.15
- **数据库**: MySQL 9.7.0
- **缓存**: Redis (Lettuce)
- **安全**: Spring Security + OAuth2 Client
- **API文档**: NextDoc4j 1.3.1 (基于 SpringDoc OpenAPI 3.0.3)
- **JWT**: JJWT 0.13.0
- **对象存储**: 七牛云 SDK
- **Markdown解析**: flexmark-java 0.64.8
- **其他**: Lombok, Validation

## 项目结构

```
liliblog-backend
├── src/main/java/cn/lilicould/liliblog/
│   ├── common/              # 通用模块
│   │   ├── cache/           # Redis缓存辅助类
│   │   ├── constant/        # 常量定义
│   │   ├── context/         # 上下文管理
│   │   ├── enums/           # 枚举类
│   │   ├── exception/       # 异常处理
│   │   ├── result/          # 统一返回结果
│   │   └── util/            # 工具类(JWT, Markdown, OSS)
│   ├── config/              # 配置类
│   │   ├── mybatis/         # MyBatis配置
│   │   ├── properties/      # 属性配置
│   │   ├── security/        # 安全配置
│   │   ├── JacksonConfig.java
│   │   ├── OpenAPIConfig.java
│   │   └── RedisConfig.java
│   ├── controller/          # 控制器层
│   │   ├── ArticleController.java
│   │   ├── AuthController.java
│   │   ├── FileController.java
│   │   └── OAuth2Controller.java
│   ├── domain/security/     # 安全领域对象
│   ├── filter/              # 过滤器
│   │   ├── JwtAuthFilter.java
│   │   └── WebLogFilter.java
│   ├── mapper/              # MyBatis Mapper接口
│   ├── pojo/                # 数据对象
│   │   ├── dto/             # 数据传输对象
│   │   │   ├── query/       # 查询参数
│   │   │   ├── request/     # 请求参数
│   │   │   └── response/    # 响应数据
│   │   └── entity/          # 实体类
│   ├── service/             # 业务逻辑层
│   │   └── impl/            # 实现类
│   ├── strategy/            # 策略模式(登录策略)
│   │   └── impl/
│   └── LiliblogApplication.java  # 启动类
├── src/main/resources/
│   ├── mapper/              # MyBatis XML映射文件
│   ├── application.yml      # 主配置文件
│   ├── application-dev.yml  # 开发环境配置
│   ├── application-prod.yml# 生产环境配置
│   └── init.sql             # 数据库初始化脚本
├── uploads/                 # 本地文件上传目录(旧项目文件)
└── logs/                    # 日志目录(旧项目文件)
```

## 主要功能

### 核心功能
- ✅ 用户认证与授权（JWT + Spring Security）
- ✅ JWT令牌管理（访问令牌 + 刷新令牌）
- ✅ OAuth2 第三方登录（GitHub）
- ✅ 文章管理（发布、编辑、查询、删除）
- ✅ 分类与标签管理
- ✅ 评论系统
- ✅ 点赞功能
- ✅ 文件上传（本地存储 + 七牛云OSS）
- ✅ Markdown 文章渲染
- ✅ API 文档自动生成（NextDoc4j/Swagger UI）

### 技术特性
- 🚀 RESTful API 设计规范
- 🔒 统一的异常处理机制
- 📝 完整的参数验证
- 🔄 策略模式实现多种登录方式
- 💾 Redis 缓存支持
- 📊 MyBatis Plus 分页查询
- 🎯 逻辑删除支持

## 快速开始

### 前置要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Redis 6.0+

### 配置说明

1. **复制配置文件模板**
```bash
cp src/main/resources/application-demo.yml src/main/resources/application-demo.yml.bak
```

2. **修改 `application-dev.yml` 中的配置**

```yaml
liliblog:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/liliblog?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    hikari:
      minimum-idle: 5
      connection-test-query: SELECT 1
      initialization-fail-timeout: -1
  
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password  # 如果没有密码则留空
      database: 0
      timeout: 3000
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1
  
  github:
    client-id: your_github_client_id
    client-secret: your_github_client_secret
    redirect-uri: http://localhost:8080/auth/oauth2/callback/github
  
  jwt:
    secret: your_base64_encoded_secret_key_at_least_32_bytes
    expiration: 3600000              # 访问令牌有效期（1小时）
    refresh-expiration: 604800000    # 刷新令牌有效期（7天）
  
  oss:
    access-key: your_qiniu_access_key
    secret-key: your_qiniu_secret_key
    bucket: your_bucket_name
  
  http-only-cookies:
    ssl: false  # 开发环境false，生产环境true
  
  logging:
    level:
      root: INFO
```

3. **初始化数据库**
```bash
mysql -u root -p < src/main/resources/init.sql
```

### 运行项目

```bash
# 方式1: 使用 Maven 运行（开发推荐）
mvn spring-boot:run

# 方式2: 打包后运行
mvn clean package
java -jar liliblog.jar

# 方式3: 在 IDE 中直接运行 LiliblogApplication.java
```

项目启动成功后，访问：
- **API文档**: http://localhost:8080/swagger-ui.html
- **NextDoc4j文档**: http://localhost:8080/doc.html

## API 接口概览

### 认证相关
- `POST /auth/login/pwd` - 用户名密码登录
- `POST /auth/register` - 用户注册
- `POST /auth/refresh` - 刷新访问令牌
- `POST /auth/logout` - 用户登出
- `GET /auth/login/github` - GitHub OAuth2 登录

### 文章相关
- `GET /api/article` - 获取文章列表（支持分页、筛选）
- `GET /api/article/{id}` - 获取文章详情
- `POST /api/article` - 创建文章
- `PUT /api/article/{id}` - 更新文章
- `DELETE /api/article/{id}` - 删除文章

### 文件上传
- `POST /api/file/upload` - 上传文件

更多接口详情请查看 Swagger UI 文档。

## 测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=JwtUtilTest

# 生成测试报告
mvn surefire-report:report
```

## 常见问题

### 1. Jackson 版本冲突

Spring Boot 4 使用 Jackson 3 (`tools.jackson`)，确保排除所有 Jackson 2 (`com.fasterxml.jackson`) 依赖：

```xml
<exclusions>
    <exclusion>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </exclusion>
</exclusions>
```

### 2. GET 请求参数绑定

对于对象类型的查询参数，使用 `@ParameterObject` 和 `@ModelAttribute`：

```java
@GetMapping
public Result<?> list(@ParameterObject @ModelAttribute QueryDTO query) {
    // ...
}
```

### 3. JWT Secret 配置

JWT secret 必须是 Base64 编码的至少 32 字节的字符串：

```java
// 生成安全的 secret
String secret = Base64.getEncoder().encodeToString("your-32-byte-secret-key-here!!".getBytes());
```

## License

MIT License - [MIT](https://opensource.org/licenses/MIT)

## 作者

lilicould - lilicould@qq.com

## 地址
[线上体验地址](https://lilicould.cn)
[github仓库](https://github.com/LiliCould/liliblog-backend)