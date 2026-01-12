package com.lilicould.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lilicould.blog.dao")
public class LiliBlogApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(LiliBlogApplication.class, args);
    }
}
