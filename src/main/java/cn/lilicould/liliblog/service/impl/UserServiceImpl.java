package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.common.constant.OrderConstant;
import cn.lilicould.liliblog.domain.security.SecurityUser;
import cn.lilicould.liliblog.mapper.UserMapper;
import cn.lilicould.liliblog.pojo.dto.query.UserQuery;
import cn.lilicould.liliblog.pojo.dto.response.PageInfo;
import cn.lilicould.liliblog.pojo.dto.response.UserInfo;
import cn.lilicould.liliblog.pojo.entity.User;
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
import org.springframework.stereotype.Service;

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
}




