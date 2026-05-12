package cn.lilicould.liliblog.config.security;

import cn.lilicould.liliblog.common.constant.StatusConstant;
import cn.lilicould.liliblog.common.enums.RoleType;
import cn.lilicould.liliblog.pojo.entity.User;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import org.jspecify.annotations.NullUnmarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * UserDetails的实现，包装基础的User
 */
@Getter
public class SecurityUser implements UserDetails, Serializable {

    private final Long id;
    private final Long githubId;
    private final String username;
    private final String password;
    private final String email;
    private final String nickname;
    private final String avatar;
    private final Integer role;
    private final Integer status;
    private final LocalDateTime lastLoginTime;
    private final Collection<? extends GrantedAuthority> authorities;

    public SecurityUser(User user) {
        this.id = user.getId();
        this.githubId = user.getGithubId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.avatar = user.getAvatar();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.lastLoginTime = user.getLastLoginTime();
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + getRoleName(user.getRole())));
    }

    private String getRoleName(Integer role) {
        return RoleType.getRoleByCode(role);
    }

    public User toUser() {
        return User.builder()
                .id(this.id)
                .githubId(this.githubId)
                .username(this.username)
                .password(this.password)
                .email(this.email)
                .nickname(this.nickname)
                .avatar(this.avatar)
                .role(this.role)
                .status(this.status)
                .lastLoginTime(this.lastLoginTime)
                .build();
    }

    @Override
    @NullUnmarked
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    @NullUnmarked
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return status != null && status.equals(StatusConstant.ENABLED);
    }

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
