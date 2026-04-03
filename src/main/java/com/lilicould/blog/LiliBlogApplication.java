package com.lilicould.blog;

import com.lilicould.blog.util.MailUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@MapperScan("com.lilicould.blog.dao")
public class LiliBlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(LiliBlogApplication.class, args);
    }
}
