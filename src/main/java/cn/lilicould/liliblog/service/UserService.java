package cn.lilicould.liliblog.service;

import cn.lilicould.liliblog.pojo.dto.request.LoginRequest;
import cn.lilicould.liliblog.pojo.dto.response.LoginVO;
import cn.lilicould.liliblog.pojo.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
* @author Lili_Could
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2026-05-08 16:58:41
*/
public interface UserService extends IService<User>, UserDetailsService {

    /**
     * 登录服务接口
     * @param request 登录参数
     * @param response HttpServletResponse,用于设置Cookie
     * @return LoginVO 登录成功响应
     */
    LoginVO login(@Valid LoginRequest request, HttpServletResponse response);
}
