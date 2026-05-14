package cn.lilicould.liliblog.common.context;

import cn.lilicould.liliblog.domain.security.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

public class BaseContext {

    public static Long getCurrentUserId() {
        // 从SpringContext中获取当前登录用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 如果当前用户未认证，返回null
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser user) {
            return user.getId();
        }
        return null;
    }

    public static boolean isAdmin() {
        // 判断当前用户是否是管理员
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser user) {
            return user.getAuthorities().stream()
                    .anyMatch(authority -> Objects.equals(authority.getAuthority(), "ROLE_ADMIN"));
        }
        return false;
    }
}
