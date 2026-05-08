package cn.lilicould.liliblog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.lilicould.liliblog.mapper")
public class LiliblogApplication {

	public static void main(String[] args) {
		SpringApplication.run(LiliblogApplication.class, args);
	}

}
