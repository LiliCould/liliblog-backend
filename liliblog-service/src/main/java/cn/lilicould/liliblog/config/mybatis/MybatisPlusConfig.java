package cn.lilicould.liliblog.config.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis plus 配置
 * @author lilicould
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 配置mybatisplus插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setMaxLimit(50L); // 单页限制50条
        paginationInnerInterceptor.setOverflow(true); // 溢出总页数后是否进行处理

        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        return interceptor;
    }
}
