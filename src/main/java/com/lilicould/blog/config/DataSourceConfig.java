package com.lilicould.blog.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * 数据源配置类
 */

@Configuration
@PropertySource("classpath:application.properties")
public class DataSourceConfig {
    /**
     * 获取环境变量
     */
    @Autowired
    private Environment env;

    @Bean()
    public DataSource dataSource() {

        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
        dataSource.setUsername(env.getProperty("spring.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.password"));
        dataSource.setUrl(env.getProperty("spring.datasource.url"));

        dataSource.setInitialSize(5); // 初始化连接数
        dataSource.setAsyncInit(true); // 异步初始化
        dataSource.setMaxActive(20); // 最大连接数
        dataSource.setMinIdle(5); // 最小连接数
        
        // 初始化连接池
        try {
            dataSource.init();
        } catch (Exception e) {
            throw new RuntimeException("Druid数据源初始化失败", e);
        }
        return dataSource;
    }

}