package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.pojo.entity.SecurityUser;
import cn.lilicould.liliblog.pojo.entity.User;
import cn.lilicould.liliblog.mapper.UserMapper;
import cn.lilicould.liliblog.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
* @author Lili_Could
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2026-05-08 16:58:41
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Override
    @NullUnmarked
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = getOne(queryWrapper);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return new SecurityUser(user);
    }
}




