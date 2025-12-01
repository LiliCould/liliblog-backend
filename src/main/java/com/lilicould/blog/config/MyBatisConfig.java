package com.lilicould.blog.config;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * MyBatis配置类
 */
@Configuration
@MapperScan("com.lilicould.blog.dao")
@PropertySource("classpath:application.properties")
public class MyBatisConfig {

    // 配置在DataSourceConfig中
    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment env;

    @Bean
    public SqlSessionFactoryBean sqlSessionFactoryBean() throws Exception{
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(dataSource);

        // 配置实体类别名
        sqlSessionFactory.setTypeAliasesPackage("com.lilicould.blog.entity");

        //配置MyBatis的XML文件目录
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        String mapperLocation = env.getProperty("mybatis.mapper-locations");
        sqlSessionFactory.setMapperLocations(resolver.getResources(Objects.requireNonNullElse(mapperLocation, "classpath:mapper/*.xml")));

        // MyBatis配置
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        // 驼峰命名
        configuration.setMapUnderscoreToCamelCase(Boolean.parseBoolean(env.getProperty("mybatis.configuration.map-underscore-to-camel-case")));
        // 配置别名
        sqlSessionFactory.setConfiguration(configuration);

        return sqlSessionFactory;
    }
}
