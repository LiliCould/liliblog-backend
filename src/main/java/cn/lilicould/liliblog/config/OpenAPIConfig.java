package cn.lilicould.liliblog.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI liliBlogOpenAPI() {
        // 1. 服务器信息：配置你的部署环境
        Server devServer = new Server()
                .url("http://localhost:8080") // 开发环境地址
                .description("开发环境");
        Server prodServer = new Server()
                .url("https://lilicould.cn:8888") // 生产环境地址
                .description("生产环境");
        // 2. 安全方案 (Security Scheme): 为 JWT 认证做准备
        // 此处定义了一个名为 "Authorization" 的安全方案，它是一个HTTP Bearer Token，具体为JWT格式。
        SecurityScheme securityScheme = new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
        // 3. 全局安全要求 (Security Requirement): 默认应用于所有API
        // 声明所有API都需要满足上述的安全方案。
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Authorization");

        return new OpenAPI()
                .info(new Info()
                        .title("立里博客 LiliBlog API 文档")
                        .description("这是立里博客的后端接口文档，严格遵循 RESTful 规范设计。欢迎访问线上体验地址。")
                        .version("2.0.1")
                        .termsOfService("https://lilicould.cn/") // 服务条款 URL,暂无
                        .contact(new Contact()
                                .name("立里可")
                                .email("lilicould@qq.com")
                                .url("https://lilicould.cn/about"))
                        .license(new License()
                                .name("MIT 许可证")
                                .url("https://mit-license.org/"))
                )
                // 服务器列表 (Servers)
                .servers(Arrays.asList(devServer, prodServer))
                // 安全组件 (Components)
                .components(new Components()
                        .addSecuritySchemes("Authorization", securityScheme))
                // 安全要求 (Security)
                .addSecurityItem(securityRequirement)
                // 外部文档 (External Documentation)
                .externalDocs(new ExternalDocumentation()
                        .description("查看线上运行版本")
                        .url("https://lilicould.cn"));
    }

    @Bean
    public GlobalOpenApiCustomizer globalOpenApiCustomizer() {
        // 返回一个自定义器，用于修改全局 OpenAPI 文档
        return openApi -> openApi.getPaths().values().stream()         // 获取所有路径（PathItem）
                .flatMap(pathItem -> pathItem.readOperations().stream()) // 将每个路径下的所有操作（GET/POST等）展平为流
                .forEach(operation -> operation.addSecurityItem(        // 为每个操作添加安全认证项
                        new SecurityRequirement().addList("Authorization") // 要求请求携带名为 "Authorization" 的安全凭证
                ));
    }
}