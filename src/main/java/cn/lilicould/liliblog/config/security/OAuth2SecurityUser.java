package cn.lilicould.liliblog.config.security;

import cn.lilicould.liliblog.pojo.entity.User;
import org.jspecify.annotations.NullUnmarked;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

/**
 * OAuth2 用户信息
 */
public class OAuth2SecurityUser extends SecurityUser implements OAuth2User {
    private final Map<String, Object> attributes;

    public OAuth2SecurityUser(User user, OAuth2User oauth2User) {
        super(user); // 调用SecurityUser的方法
        this.attributes = oauth2User.getAttributes();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    @NullUnmarked
    public String getName() {
        return getUsername();
    }
}
