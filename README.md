# 立里博客后端

基于 Spring Boot 4.0 构建的个人博客系统后端服务，支持文章发布、评论管理、用户认证等功能。

## 技术栈

- **框架**: Spring Boot 4.0.6
- **语言**: Java 17
- **持久层**: MyBatis Plus 3.5.15
- **数据库**: MySQL
- **缓存**: Redis
- **安全**: Spring Security
- **API文档**: SpringDoc OpenAPI (Swagger UI)
- **JWT**: JJWT 0.13.0
- **其他**: Lombok

## 项目结构

```
cn.lilicould.liliblog
├── config          # 配置类
│   └── properties  # 属性配置
├── util            # 工具类
│   └── JwtUtil     # JWT工具类
└── LiliblogApplication.java  # 启动类
```

## 主要功能

- 用户认证与授权
- JWT令牌管理（访问令牌 + 刷新令牌）
- 接口文档自动生成

## 配置说明

在 `application.yml` 中配置以下参数：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/liliblog
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: your_base64_secret_key
  expiration: 3600000      # 访问令牌有效期（毫秒）
  refreshExpiration: 604800000  # 刷新令牌有效期（7天）
```

## 运行项目

```bash
# 打包
mvn clean package

# 运行
mvn spring-boot:run
```

## API文档

项目启动后访问：`http://localhost:8080/swagger-ui.html`

## 测试

```bash
mvn test
```

## License

MIT License - [https://opensource.org/licenses/MIT](https://opensource.org/licenses/MIT)

## 作者

lilicould - lilicould@qq.com