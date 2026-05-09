package cn.lilicould.liliblog.service;

import cn.lilicould.liliblog.pojo.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
* @author Lili_Could
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2026-05-08 16:58:41
*/
public interface UserService extends IService<User>, UserDetailsService {

}
