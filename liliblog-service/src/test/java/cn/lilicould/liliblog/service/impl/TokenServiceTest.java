package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.cache.RedisHelper;
import cn.lilicould.liliblog.config.properties.HttpOnlyCookiesProperties;
import cn.lilicould.liliblog.constant.RedisPrefixConstant;
import cn.lilicould.liliblog.entity.User;
import cn.lilicould.liliblog.response.LoginVO;
import cn.lilicould.liliblog.service.UserService;
import cn.lilicould.liliblog.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TokenService测试类
 *
 * @author lilicould
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TokenServiceTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RedisHelper redisHelper;
    @Mock
    private UserService userService;
    @Mock
    private HttpServletResponse response;

    private final HttpOnlyCookiesProperties httpOnlyCookiesProperties = new HttpOnlyCookiesProperties();

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        httpOnlyCookiesProperties.setSsl(true);
        httpOnlyCookiesProperties.setDomain(".lilicould.cn");
        tokenService = new TokenService(jwtUtil, redisHelper, userService, httpOnlyCookiesProperties);
    }

    private User buildUser() {
        return User.builder().id(1L).username("testuser").nickname("测试用户")
                .email("test@example.com").role(1).status(1).build();
    }

    @Test
    void createLoginResponseSuccess() {
        User user = buildUser();
        when(jwtUtil.generateToken("testuser", user)).thenReturn("access-token");
        when(jwtUtil.extractExpiresIn("access-token")).thenReturn(3600L);
        when(jwtUtil.generateRefreshToken("testuser", user)).thenReturn("refresh-token");
        when(jwtUtil.extractExpiresIn("refresh-token")).thenReturn(604800L);

        LoginVO vo = tokenService.createLoginResponse(user, response);

        assertEquals("access-token", vo.getAccessToken());
        assertEquals(3600L, vo.getExpiresIn());
        assertNotNull(vo.getUserInfo());
        assertEquals("testuser", vo.getUserInfo().getUsername());
        assertEquals("测试用户", vo.getUserInfo().getNickname());

        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq("Set-Cookie"), headerCaptor.capture());
        String cookie = headerCaptor.getValue();
        assertTrue(cookie.contains("refresh_token=refresh-token"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("SameSite=None"));
        assertTrue(cookie.contains("Secure"));
        assertTrue(cookie.contains(".lilicould.cn"));

        verify(redisHelper).set(eq(RedisPrefixConstant.AUTH_REFRESH_TOKEN + "testuser"),
                eq("refresh-token"), eq(604800L * 1000));
        verify(userService).updateById(user);
        assertNotNull(user.getLastLoginTime());
    }

    @Test
    void createLoginResponseWithoutSslDoesNotSetSecureCookie() {
        httpOnlyCookiesProperties.setSsl(false);
        User user = buildUser();
        when(jwtUtil.generateToken("testuser", user)).thenReturn("access-token");
        when(jwtUtil.extractExpiresIn("access-token")).thenReturn(3600L);
        when(jwtUtil.generateRefreshToken("testuser", user)).thenReturn("refresh-token");
        when(jwtUtil.extractExpiresIn("refresh-token")).thenReturn(3600L);

        tokenService.createLoginResponse(user, response);

        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq("Set-Cookie"), headerCaptor.capture());
        assertFalse(headerCaptor.getValue().contains("Secure"));
        verify(redisHelper).set(anyString(), anyString(), eq(3600L * 1000));
    }
}