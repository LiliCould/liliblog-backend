package cn.lilicould.liliblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.lilicould.liliblog.entity.User;
import cn.lilicould.liliblog.service.UserService;
import cn.lilicould.liliblog.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author Lili_Could
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2026-05-08 16:58:41
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




