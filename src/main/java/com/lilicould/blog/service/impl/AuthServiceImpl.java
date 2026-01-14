package com.lilicould.blog.service.impl;


import com.lilicould.blog.annotation.Log;
import com.lilicould.blog.dao.UserMapper;
import com.lilicould.blog.dto.LoginDTO;
import com.lilicould.blog.dto.PasswordChangeDTO;
import com.lilicould.blog.dto.RegisterDTO;
import com.lilicould.blog.entity.User;
import com.lilicould.blog.exception.BusinessException;
import com.lilicould.blog.service.AuthService;
import com.lilicould.blog.util.JwtUtil;
import com.lilicould.blog.util.PasswordUtil;
import com.lilicould.blog.vo.LoginVO;
import com.lilicould.blog.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;


import java.util.Date;

/**
 * 认证服务接口实现类
 */

@Service()
@Transactional(isolation = Isolation.READ_COMMITTED,rollbackFor = Exception.class)
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    // 密码工具类
    @Autowired
    private PasswordUtil passwordUtil;

    @Override
    @Log(value = "用户登录服务",logParams = false)
    public LoginVO login(LoginDTO loginDTO) {
        User user = userMapper.selectByUsername(loginDTO.getUsername());

        if (user == null) {
            throw new BusinessException("用户不存在或已被删除");
        }

        // 验证密码，使用密文
        if (!passwordUtil.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 更新最后登录时间
        userMapper.updateLastLoginTime(user.getId(), new Date(System.currentTimeMillis()));

        // 生成token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        return new LoginVO(
                token,
                user.getUsername(),
                user.getNickname(),
                user.getRole(),
                user.getAvatar()
        );
    }

    @Override
    @Log(value = "用户注册服务",logParams = false)
    public void register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        if (userMapper.selectByUsername(registerDTO.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userMapper.selectByEmail(registerDTO.getEmail()) != null) {
            throw new BusinessException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(passwordUtil.encode(registerDTO.getPassword())); // 加密存储
        user.setNickname(registerDTO.getNickname());
        user.setRole("VISITOR");
        user.setStatus(1);

        userMapper.insert(user);
    }

    @Override
    @Log(value = "用户修改密码服务",logParams = false)
    public void updatePassword(String username, PasswordChangeDTO passwordChangeDTO) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在或或已注销");
        }

        if (!passwordUtil.matches(passwordChangeDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 一般前端端会进行密码一致性校验，但是为了避免用户体验问题，这里再次进行一致性校验保险措施
        if (passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getOldPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }
        // 更新密码
        userMapper.updatePassword(user.getId(), passwordUtil.encode(passwordChangeDTO.getNewPassword()));
    }

    @Override
    @Log("获取用户信息服务")
    public UserVO getProfile(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在或已注销");
        }
        return new UserVO(
                user.getUsername(),
                user.getEmail(),
                user.getNickname(),
                user.getAvatar(),
                user.getLastLoginTime(),
                user.getCreateTime()
        );
    }

    @Override
    @Log("用户登出服务")
    public void logout(String token) {
        // JWT是无状态的，客户端删除token即可
        // 如果需要服务端控制，可以实现token黑名单
    }
}
