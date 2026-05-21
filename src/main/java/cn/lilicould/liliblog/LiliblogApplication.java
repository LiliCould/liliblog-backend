package cn.lilicould.liliblog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableScheduling
@EnableMethodSecurity
@MapperScan("cn.lilicould.liliblog.mapper")
public class LiliblogApplication {

	public static void main(String[] args) {
		SpringApplication.run(LiliblogApplication.class, args);

		System.out.println("""
				////////////////////////////////////////////////////////////////////
				//                          _ooOoo_                               //
				//                         o8888888o                              //
				//                         88" . "88                              //
				//                         (| ^_^ |)                              //
				//                         O\\  =  /O                              //
				//                      ____/`---'\\____                           //
				//                    .'  \\\\|     |//  `.                         //
				//                   /  \\\\|||  :  |||//  \\                        //
				//                  /  _||||| -:- |||||-  \\                       //
				//                  |   | \\\\\\  -  /// |   |                       //
				//                  | \\_|  ''\\---/''  |   |                       //
				//                  \\  .-\\__  `-`  ___/-. /                       //
				//                ___`. .'  /--.--\\  `. . ___                     //
				//              ."" '<  `.___\\_<|>_/___.'  >'"".                  //
				//            | | :  `- \\`.;`\\ _ /`;.`/ - ` : | |                 //
				//            \\  \\ `-.   \\_ __\\ /__ _/   .-` /  /                 //
				//      ========`-.____`-.___\\_____/___.-`____.-'========         //
				//                           `=---='                              //
				//      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^        //
				//            佛祖保佑       永不宕机     永无BUG                     //
				////////////////////////////////////////////////////////////////////
				""");
	}

}
