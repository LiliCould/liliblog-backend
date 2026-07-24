package cn.lilicould.liliblog.service;

import cn.lilicould.liliblog.query.UserQuery;
import cn.lilicould.liliblog.request.AdminUserUpdateRequest;
import cn.lilicould.liliblog.request.UserCreateRequest;
import cn.lilicould.liliblog.response.PageInfo;
import cn.lilicould.liliblog.response.UserInfo;
import cn.lilicould.liliblog.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
* @author Lili_Could
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2026-05-08 16:58:41
*/
public interface UserService extends IService<User>, UserDetailsService {


    /**
     * 分页用户列表
     * @param query 查询参数
     * @return 分页用户列表
     */
    PageInfo<UserInfo> list(UserQuery query);

    /**
     * 修改用户信息
     * @param id 用户ID
     * @param request 修改参数
     */
    void updateUserInfo(Long id, AdminUserUpdateRequest request);

    /**
     * 添加用户
     * @param request 添加参数
     */
    void createUser(UserCreateRequest request);

    /**
     * 删除用户
     * @param id 用户ID
     */
    void remove(Long id);

    /**
     * 切换用户状态
     * @param id 用户ID
     */
    void changeStatus(Long id);
}
