package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.cache.RedisHelper;
import cn.lilicould.liliblog.constant.RedisPrefixConstant;
import cn.lilicould.liliblog.constant.StatusConstant;
import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.enums.RoleType;
import cn.lilicould.liliblog.exception.BusinessException;
import cn.lilicould.liliblog.request.LoginRequest;
import cn.lilicould.liliblog.request.RegisterRequest;
import cn.lilicould.liliblog.response.LoginVO;
import cn.lilicould.liliblog.service.UserService;
import cn.lilicould.liliblog.strategy.LoginStrategy;
import cn.lilicould.liliblog.strategy.LoginStrategyFactory;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AuthServiceImpl测试类
 *
 * @author lilicould
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserService userService;
    @Mock
    private LoginStrategyFactory factory;
    @Mock
    private RedisHelper redisHelper;
    @Mock
    private EmailTemplateService emailTemplateService;
    @Mock
    private LoginStrategy strategy;
    @Mock
    private HttpServletResponse response;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(passwordEncoder, userService, factory, redisHelper, emailTemplateService);
    }

    @Test
    void loginDelegatesToStrategy() {
        when(factory.getStrategy("pwd")).thenReturn(strategy);
        LoginRequest request = new LoginRequest();
        request.setLoginType("pwd");
        LoginVO expected = LoginVO.builder().accessToken("token").build();
        when(strategy.login(request, response)).thenReturn(expected);

        LoginVO result = authService.login(request, response);

        assertEquals(expected, result);
        verify(factory).getStrategy("pwd");
        verify(strategy).login(request, response);
    }

    @Test
    void loginWithUnsupportedTypeThrowsNotSupported() {
        when(factory.getStrategy("wechat")).thenThrow(new BusinessException(CodeEnum.NOT_SUPPORTED));
        LoginRequest request = new LoginRequest();
        request.setLoginType("wechat");

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(request, response));
        assertEquals(CodeEnum.NOT_SUPPORTED.getCode(), ex.getCode());
    }

    @Test
    void registerWithPasswordMismatchThrows() {
        RegisterRequest request = new RegisterRequest();
        request.setPassword("123456");
        request.setConfirmPassword("654321");
        request.setUsername("user1");
        request.setEmail("user1@test.com");
        request.setNickname("用户");

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(request));
        assertEquals(CodeEnum.PASSWORD_MISMATCH.getCode(), ex.getCode());
    }

    @Test
    void registerWithUsernameExistsThrows() {
        RegisterRequest request = buildRegisterRequest();
        when(userService.exists(any(Wrapper.class))).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(request));
        assertEquals(CodeEnum.USERNAME_ALREADY_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void registerWithEmailExistsThrows() {
        RegisterRequest request = buildRegisterRequest();
        when(userService.exists(any(Wrapper.class))).thenReturn(false, true);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(request));
        assertEquals(CodeEnum.EMAIL_ALREADY_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void registerSuccessEncodesPasswordAndSavesUser() {
        RegisterRequest request = buildRegisterRequest();
        when(userService.exists(any(Wrapper.class))).thenReturn(false, false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-hash");

        authService.register(request);

        ArgumentCaptor<cn.lilicould.liliblog.entity.User> captor =
                ArgumentCaptor.forClass(cn.lilicould.liliblog.entity.User.class);
        verify(userService).save(captor.capture());
        cn.lilicould.liliblog.entity.User saved = captor.getValue();
        assertEquals("user1", saved.getUsername());
        assertEquals("encoded-hash", saved.getPassword());
        assertEquals(RoleType.USER.getCode(), saved.getRole());
        assertEquals(StatusConstant.ENABLED, saved.getStatus());
        assertEquals("user1@test.com", saved.getEmail());
    }

    @Test
    void getEmailCodeWhenAlreadyExistsThrowsRepeatOperation() {
        when(redisHelper.exists(RedisPrefixConstant.AUTH_EMAIL_CODE + "a@b.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.getEmailCode("a@b.com"));
        assertEquals(CodeEnum.REPEAT_OPERATION.getCode(), ex.getCode());
    }

    @Test
    void getEmailCodeSuccessStoresCodeAndSendsEmail() {
        when(redisHelper.exists(RedisPrefixConstant.AUTH_EMAIL_CODE + "a@b.com")).thenReturn(false);

        authService.getEmailCode("a@b.com");

        verify(redisHelper).set(eq(RedisPrefixConstant.AUTH_EMAIL_CODE + "a@b.com"), anyString(), eq(5 * 60 * 1000L));
        verify(emailTemplateService).sendVerificationCodeEmail(eq("a@b.com"), anyString());
    }

    private RegisterRequest buildRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user1");
        request.setPassword("123456");
        request.setConfirmPassword("123456");
        request.setEmail("user1@test.com");
        request.setNickname("用户一");
        return request;
    }
}