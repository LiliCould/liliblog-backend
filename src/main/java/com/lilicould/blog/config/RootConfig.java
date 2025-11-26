package com.lilicould.blog.config;

import org.springframework.context.annotation.*;
import org.springframework.stereotype.Controller;

/**
 * Spring根配置类
 * 配置Service、DAO、事务管理等非web组件
 */

@Configuration
@ComponentScan(
        basePackages = "com.lilicould.blog",
        // 扫描除了Controller以外的所有组件
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION,
                value = Controller.class
        )
)
@EnableAspectJAutoProxy // 开启AOP自动代理
public class RootConfig {
    // 这里主要配置非web相关的Bean，例如Service、DAO、事务管理等，当然，由于开启了组件扫描，所以这里很少有代码
    // 具体的Bean配置在各自的配置类中（如DataSourceConfig、MyBatisConfig等）
}
