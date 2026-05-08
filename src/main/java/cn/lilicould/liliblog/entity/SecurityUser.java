package cn.lilicould.liliblog.entity;

import cn.lilicould.liliblog.constant.StatusConstant;
import cn.lilicould.liliblog.enums.RoleType;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import org.jspecify.annotations.NullUnmarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * UserDetails的实现，包装基础的User
 */
@Getter
public class SecurityUser implements UserDetails, Serializable {

    private final Long id;
    private final String username;
    private final String password;
    private final String email;
    private final String nickname;
    private final String avatar;
    private final Integer role;
    private final Integer status;
    private final Collection<? extends GrantedAuthority> authorities;

    public SecurityUser(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.avatar = user.getAvatar();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + getRoleName(user.getRole())));
    }

    private String getRoleName(Integer role) {
        return RoleType.getRoleByCode(role);
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
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return status != null && status.equals(StatusConstant.ENABLED);
    }

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
