package com.lilicould.blog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lilicould.blog.interceptor.ControllerLogInterceptor;
import com.lilicould.blog.interceptor.JwtAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Spring MVC配置类
 * 配置控制器、视图解析器、拦截器等web相关组件
 */

@Configuration
@EnableWebMvc
@ComponentScan("com.lilicould.blog.controller")
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    /**
     * 配置JSON消息转换器
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        converter.setObjectMapper(objectMapper);
        converters.add(converter);
    }

    /**
     * 配置静态资源处理
     */
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        // 配置静态资源处理器
        configurer.enable();
    }

    /**
     * 配置CORS跨域支持
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 控制器日志拦截器
        registry.addInterceptor(new ControllerLogInterceptor())
                .addPathPatterns("/api/**")      // 拦截所有路径
                .excludePathPatterns(
                        "/static/**",          // 排除静态资源
                        "/error",              // 排除错误页面
                        "/favicon.ico"         // 排除网站图标
                );

        // JWT认证拦截器
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login", "/api/auth/register");


    }

    /**
     * 配置视图解析器（虽然前后端分离，但保留以防万一）
     */
    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        return resolver;
    }
}
