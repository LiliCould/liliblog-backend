package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.constant.StatusConstant;
import cn.lilicould.liliblog.domain.security.OAuth2SecurityUser;
import cn.lilicould.liliblog.entity.User;
import cn.lilicould.liliblog.enums.RoleType;
import cn.lilicould.liliblog.mapper.UserMapper;
import cn.lilicould.liliblog.service.UserService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestOperations;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CustomOAuth2UserService测试类
 *
 * @author lilicould
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private RestOperations restOperations;

    private CustomOAuth2UserService customOAuth2UserService;
    private OAuth2UserRequest userRequest;

    @BeforeEach
    void setUp() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("github")
                .clientId("client-id")
                .clientSecret("client-secret")
                .redirectUri("{baseUrl}/login/oauth2/code/github")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("read:user")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName("id")
                .clientName("GitHub")
                .build();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, "access-token",
                Instant.now(), Instant.now().plusSeconds(3600), Set.of("read:user"));
        userRequest = new OAuth2UserRequest(registration, accessToken);
        when(userService.lambdaQuery()).thenReturn(new LambdaQueryChainWrapper<>(userMapper, User.class));
        customOAuth2UserService = new CustomOAuth2UserService(userService);
        customOAuth2UserService.setRestOperations(restOperations);
    }

    private void stubUserInfoResponse(Map<String, Object> attributes) {
        when(restOperations.exchange(any(RequestEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(attributes, HttpStatus.OK));
    }

    private Map<String, Object> buildAttributes(Object githubId, String login, String email, String name) {
        Map<String, Object> attributes = new java.util.HashMap<>();
        attributes.put("id", githubId);
        attributes.put("login", login);
        attributes.put("email", email);
        attributes.put("name", name);
        attributes.put("avatar_url", "https://avatars.githubusercontent.com/u/1");
        return attributes;
    }

    @Test
    void loadUserWhenGithubIdMissingThrowsOAuth2AuthenticationException() {
        stubUserInfoResponse(buildAttributes(null, "octocat", "octo@example.com", "Octo"));

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> customOAuth2UserService.loadUser(userRequest));
        assertTrue(ex.getMessage().contains("获取githubId失败"));
    }

    @Test
    void loadUserWhenUserExistsByGithubIdUpdatesUser() {
        stubUserInfoResponse(buildAttributes(123, "octocat", "octo@example.com", "新昵称"));
        User existing = User.builder().id(1L).githubId(123L).username("octocat")
                .email("octo@example.com").role(RoleType.USER.getCode()).status(StatusConstant.ENABLED).build();
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(existing);

        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        assertInstanceOf(OAuth2SecurityUser.class, result);
        OAuth2SecurityUser securityUser = (OAuth2SecurityUser) result;
        assertEquals("octocat", securityUser.getUsername());
        assertEquals(123L, securityUser.getGithubId());
        assertEquals("新昵称", existing.getNickname());
        assertEquals("https://avatars.githubusercontent.com/u/1", existing.getAvatar());
        verify(userService).updateById(existing);
        verify(userService, never()).save(any());
    }

    @Test
    void loadUserWhenEmailExistsBindsGithubIdToExistingUser() {
        stubUserInfoResponse(buildAttributes(456, "newuser", "same@example.com", "Same"));
        User byEmail = User.builder().id(2L).username("olduser").email("same@example.com")
                .role(RoleType.USER.getCode()).status(StatusConstant.ENABLED).build();
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(null, byEmail);

        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        OAuth2SecurityUser securityUser = (OAuth2SecurityUser) result;
        assertEquals("olduser", securityUser.getUsername());
        assertEquals(456L, byEmail.getGithubId());
        verify(userService).updateById(byEmail);
        verify(userService, never()).save(any());
    }

    @Test
    void loadUserWhenUsernameCollidesUsesRandomUsername() {
        stubUserInfoResponse(buildAttributes(789, "octocat", null, null));
        User byName = User.builder().id(3L).username("octocat")
                .role(RoleType.USER.getCode()).status(StatusConstant.ENABLED).build();
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(null, null, byName);

        customOAuth2UserService.loadUser(userRequest);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).save(captor.capture());
        User saved = captor.getValue();
        assertNotEquals("octocat", saved.getUsername());
        assertEquals(789L, saved.getGithubId());
        assertEquals(saved.getUsername() + "@github.local", saved.getEmail());
        assertEquals(saved.getUsername(), saved.getNickname());
        assertEquals(RoleType.USER.getCode(), saved.getRole());
        assertEquals(StatusConstant.ENABLED, saved.getStatus());
    }

    @Test
    void loadUserCreatesNewUserWithDefaults() {
        stubUserInfoResponse(buildAttributes(101, "freshman", "fresh@example.com", null));
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(null, null, null);

        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).save(captor.capture());
        User saved = captor.getValue();
        assertEquals(101L, saved.getGithubId());
        assertEquals("freshman", saved.getUsername());
        assertEquals("fresh@example.com", saved.getEmail());
        assertEquals("freshman", saved.getNickname());
        assertEquals(StatusConstant.ENABLED, saved.getStatus());
        OAuth2SecurityUser securityUser = (OAuth2SecurityUser) result;
        assertEquals("freshman", securityUser.getUsername());
        assertEquals(101L, securityUser.getGithubId());
    }
}