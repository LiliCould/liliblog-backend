package cn.lilicould.liliblog.config.security;

import cn.lilicould.liliblog.common.constant.StatusConstant;
import cn.lilicould.liliblog.common.enums.RoleType;
import cn.lilicould.liliblog.pojo.entity.User;
import cn.lilicould.liliblog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("OAuth2 回调处理开始...");

        OAuth2User oauth2User = super.loadUser(userRequest);

        log.info("GitHub 用户信息: {}",oauth2User.getAttributes());

        // 获取GitHub用户信息
        Integer githubId = oauth2User.getAttribute("id");
        String username = oauth2User.getAttribute("login");
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String avatarUrl = oauth2User.getAttribute("avatar_url");

        if (githubId == null) {
            throw new OAuth2AuthenticationException("获取githubId失败");
        }

        // 查找或创建用户
        User user = userService.lambdaQuery()
                .eq(User::getGithubId, githubId)
                .one();

        if (user == null) {
            // 首次登录，创建新用户

            User existUser = userService.lambdaQuery()
                    .eq(User::getEmail, email)
                    .one();

            if (existUser != null) { // 如果已存在同邮箱用户，更新githubId,因为邮箱是需要验证所有权的，更具保障性
                existUser.setGithubId(Long.valueOf(githubId));
                userService.updateById(existUser);
                return new OAuth2SecurityUser(existUser, oauth2User);
            }

            // 重新根据用户名查一遍
            existUser = userService.lambdaQuery()
                    .eq(User::getUsername, username)
                    .one();

            if (existUser != null) {
                username = UUID.randomUUID().toString(); // 不能保证这个用户名是否为用户的，生成一个随机数作为username
            }

            user = User.builder()
                    .githubId(Long.valueOf(githubId))
                    .username(username)
                    .email(email != null ? email : username + "@github.local")
                    .nickname(name != null ? name : username)
                    .avatar(avatarUrl)
                    .password(UUID.randomUUID().toString())
                    .role(RoleType.USER.getCode()) // 普通用户
                    .status(StatusConstant.ENABLED) // 启用
                    .build();
            userService.save(user);
        } else {
            // 更新用户信息
            user.setAvatar(avatarUrl);
            if (name != null) user.setNickname(name);
            userService.updateById(user);
        }

        return new OAuth2SecurityUser(user, oauth2User);
    }


}
