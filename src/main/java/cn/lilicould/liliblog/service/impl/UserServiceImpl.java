package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.common.annotation.Audit;
import cn.lilicould.liliblog.common.constant.OrderConstant;
import cn.lilicould.liliblog.common.constant.StatusConstant;
import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.enums.RoleType;
import cn.lilicould.liliblog.common.exception.BusinessException;
import cn.lilicould.liliblog.domain.security.SecurityUser;
import cn.lilicould.liliblog.mapper.UserMapper;
import cn.lilicould.liliblog.model.dto.query.UserQuery;
import cn.lilicould.liliblog.model.dto.request.AdminUserUpdateRequest;
import cn.lilicould.liliblog.model.dto.request.UserCreateRequest;
import cn.lilicould.liliblog.model.dto.response.PageInfo;
import cn.lilicould.liliblog.model.dto.response.UserInfo;
import cn.lilicould.liliblog.model.entity.User;
import cn.lilicould.liliblog.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author Lili_Could
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2026-05-08 16:58:41
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    private final PasswordEncoder passwordEncoder;

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

    /**
     * 分页用户列表
     * @param query 查询参数
     * @return 用户列表
     */
    @Override
    public PageInfo<UserInfo> list(UserQuery query) {
        // 设置分页默认值
        Page<User> page = Page.of(query.getCurrent(), query.getSize());
        // 设置排序字段
        page.setOrders(OrderItem.descs(OrderConstant.CREATE_TIME, OrderConstant.UPDATE_TIME, OrderConstant.ID));
        // 创建查询条件
        Page<User> userPage = page(page, new LambdaQueryWrapper<User>()
                .like(query.getUsername() != null,User::getUsername, query.getUsername())
                .like(query.getNickname() != null,User::getNickname, query.getNickname())
                .like(query.getEmail() != null,User::getEmail, query.getEmail())
                .eq(query.getId() != null,User::getId, query.getId())
                .eq(query.getRole() != null,User::getRole, query.getRole())
                .eq(query.getStatus() != null,User::getStatus, query.getStatus())
                .ge(query.getCreateTimeStart() != null,User::getCreateTime, query.getCreateTimeStart())
                .le(query.getCreateTimeEnd() != null,User::getCreateTime, query.getCreateTimeEnd())
                .ge(query.getLastLoginTimeStart() != null,User::getLastLoginTime, query.getLastLoginTimeStart())
                .le(query.getLastLoginTimeEnd() != null,User::getLastLoginTime, query.getLastLoginTimeEnd())
                .ge(query.getUpdateTimeStart() != null,User::getUpdateTime, query.getUpdateTimeStart())
                .le(query.getUpdateTimeEnd() != null,User::getUpdateTime, query.getUpdateTimeEnd())
        );

        if (userPage.getTotal() == 0) {
            return PageInfo.empty(query.getCurrent(), query.getSize());
        }

        // 转换为用户信息
        List<UserInfo> records = userPage.getRecords().stream().map(UserInfo::from).toList();
        Page<UserInfo> pageInfo = Page.of(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        pageInfo.setRecords(records);
        return PageInfo.of(pageInfo);
    }

    /**
     * 更新用户信息
     *
     * @param id 用户ID
     * @param request 更新信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class,isolation = Isolation.READ_COMMITTED)
    @Audit(
            module = "user",
            operation = "UPDATE",
            description = "'更新用户:' + #id",
            targetType = "USER",
            target = "#id"
    )
    public void updateUserInfo(Long id, AdminUserUpdateRequest request) {
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException(CodeEnum.USER_NOT_FOUND);
        }

        // 修改信息
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setNickname(request.getNickname());
        user.setAvatar(request.getAvatar());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());
        // 校验邮箱是否有效
        if (request.getEmail() != null) {
            if (isEmailExists(request.getEmail(), id)) {
                throw new BusinessException(CodeEnum.EMAIL_ALREADY_EXISTS);
            }
        }
        user.setEmail(request.getEmail());
        // 校验用户名是否已存在
        if (request.getUsername() != null) {
            if (isUsernameExists(request.getUsername(), id)) {
                throw new BusinessException(CodeEnum.USERNAME_ALREADY_EXISTS);
            }
        }
        user.setUsername(request.getUsername());

        this.updateById(user);
    }

    /**
     * 创建用户
     * @param request 创建参数
     */
    @Override
    @Audit(
            module = "user",
            operation = "CREATE",
            description = "'新增用户:' + #request.getUsername()",
            targetType = "USER",
            target = "#request.getUsername()"
    )
    public void createUser(UserCreateRequest request) {
        // 验证密码
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(CodeEnum.PASSWORD_MISMATCH);
        }

        // 校验邮箱是否已经存在
        if (request.getEmail() != null && this.exists(new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail()))) {
            throw new BusinessException(CodeEnum.EMAIL_ALREADY_EXISTS);
        }

        // 校验用户名是否已经存在
        if (request.getUsername() != null && this.exists(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()))) {
            throw new BusinessException(CodeEnum.USERNAME_ALREADY_EXISTS);
        }

        // 创建用户
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .avatar(null)
                .role(RoleType.USER.getCode())
                .status(StatusConstant.ENABLED)
                .email(request.getEmail())
                .build();

        this.save(user);
    }

    /**
     * 删除用户
     * @param id 用户ID
     */
    @Override
    @Audit(
            module = "user",
            operation = "DELETE",
            description = "'删除用户:' + #id",
            targetType = "USER",
            target = "#id"
    )
    public void remove(Long id) {
        // 删除前先判断，保证代码健壮性
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException(CodeEnum.USER_NOT_FOUND);
        }
        this.removeById(id);
    }

    /**
     * 修改用户状态
     * @param id 用户ID
     */
    @Override
    @Audit(
            module = "user",
            operation = "UPDATE",
            description = "'切换用户状态:' + #id",
            targetType = "USER",
            target = "#id"
    )
    public void changeStatus(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException(CodeEnum.USER_NOT_FOUND);
        }
        // 切换状态
        Integer status = user.getStatus() == 1 ? 0 : 1;
        user.setStatus(status);
        this.updateById(user);
    }

    /**
     * 校验邮箱是否已存在
     * @param email 邮箱
     * @param id 用户ID
     * @return 是否已存在
     */
    public boolean isEmailExists(String email, Long id) {
        return this.exists(
                new LambdaQueryWrapper<User>()
                        .eq(User::getEmail, email)
                        .ne(User::getId, id)
        );
    }

    /**
     * 校验用户名是否已存在
     * @param username 用户名
     * @param id 用户ID
     * @return 是否已存在
     */
    public boolean isUsernameExists(String username, Long id) {
        return this.exists(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .ne(User::getId, id)
        );
    }
}




