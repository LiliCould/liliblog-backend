package com.lilicould.blog.config;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * Web应用初始化类 - 替代web.xml
 * 这是项目的"启动类"，Servlet 3.0+容器会自动检测并加载这个类
 */
public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer{

    /**
     * 指定根配置类 - 对应Spring的ApplicationContext
     * 这些配置类用于配置业务层、数据层等非web相关的Bean
     */

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        System.out.println("=== LILI博客系统启动中 ===");
        super.onStartup(servletContext);
        System.out.println("Servlet容器初始化完成！");
    }

    /**
     * 配置Spring容器 - 根容器，这些配置类用于配置Spring容器，如数据源、事务管理器、缓存、日志等
     */
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{
                RootConfig.class,
                DataSourceConfig.class,
                MyBatisConfig.class,
                TransactionConfig.class};
    }

    /**
     * 指定Servlet配置类 - 对应Spring MVC的WebApplicationContext
     * 这些配置类用于配置控制器、视图解析器等web相关的Bean
     */
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebConfig.class};
    }

    /**
     * 指定DispatcherServlet的映射路径
     * 将所有请求交给Spring MVC处理
     */
    @Override
    protected String[] getServletMappings() {
        return new String[] {"/"};
    }

    /**
     * 配置Multipart文件上传
     */
    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        // 文件上传配置
        MultipartConfigElement multipartConfig = new MultipartConfigElement(
                "/tmp/blog/uploads",   // 临时目录
                2097152000,              // 最大文件大小 20MB
                41943040,              // 最大请求大小 40MB
                0                      // 文件大小阈值
        );
        registration.setMultipartConfig(multipartConfig);

        // 设置启动优先级
        registration.setLoadOnStartup(1);
    }

}