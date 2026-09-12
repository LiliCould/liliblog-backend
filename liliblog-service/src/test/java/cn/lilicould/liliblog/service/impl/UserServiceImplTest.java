package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.constant.StatusConstant;
import cn.lilicould.liliblog.entity.User;
import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.enums.RoleType;
import cn.lilicould.liliblog.exception.BusinessException;
import cn.lilicould.liliblog.mapper.UserMapper;
import cn.lilicould.liliblog.query.UserQuery;
import cn.lilicould.liliblog.request.AdminUserUpdateRequest;
import cn.lilicould.liliblog.request.UserCreateRequest;
import cn.lilicould.liliblog.response.PageInfo;
import cn.lilicould.liliblog.response.UserInfo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserServiceImpl测试类
 *
 * @author lilicould
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(passwordEncoder);
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
    }

    private User buildUser(Long id, String username, Integer status) {
        return User.builder().id(id).username(username).password("pwd").nickname("昵称")
                .email(username + "@test.com").role(RoleType.USER.getCode()).status(status).build();
    }

    @Test
    void loadUserByUsernameSuccess() {
        User user = buildUser(1L, "zhangsan", StatusConstant.ENABLED);
        when(userMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(user);

        UserDetails details = userService.loadUserByUsername("zhangsan");

        assertNotNull(details);
        assertEquals("zhangsan", details.getUsername());
        assertTrue(details.isEnabled());
    }

    @Test
    void loadUserByUsernameNotFoundThrowsUsernameNotFoundException() {
        when(userMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("nobody"));
    }

    @Test
    void listWithEmptyResultReturnsEmptyPageInfo() {
        UserQuery query = new UserQuery();
        query.setCurrent(1L);
        query.setSize(10L);
        query.setUsername("张");
        when(userMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(new Page<>(1, 10, 0));

        PageInfo<UserInfo> result = userService.list(query);

        assertEquals(0L, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    void listSuccess() {
        UserQuery query = new UserQuery();
        query.setCurrent(1L);
        query.setSize(10L);
        User user = buildUser(1L, "zhangsan", StatusConstant.ENABLED);
        Page<User> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(user));
        when(userMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        PageInfo<UserInfo> result = userService.list(query);

        assertEquals(1, result.getRecords().size());
        assertEquals("zhangsan", result.getRecords().get(0).getUsername());
    }

    @Test
    void updateUserInfoWhenNotExistThrowsUserNotFound() {
        when(userMapper.selectById(1L)).thenReturn(null);
        AdminUserUpdateRequest request = new AdminUserUpdateRequest();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.updateUserInfo(1L, request));
        assertEquals(CodeEnum.USER_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void updateUserInfoWhenEmailExistsThrowsEmailAlreadyExists() {
        User user = buildUser(1L, "zhangsan", StatusConstant.ENABLED);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.exists(any(Wrapper.class))).thenReturn(true);
        AdminUserUpdateRequest request = new AdminUserUpdateRequest();
        request.setEmail("dup@test.com");
        request.setNewPassword("123456");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.updateUserInfo(1L, request));
        assertEquals(CodeEnum.EMAIL_ALREADY_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void updateUserInfoWhenUsernameExistsThrowsUsernameAlreadyExists() {
        User user = buildUser(1L, "zhangsan", StatusConstant.ENABLED);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.exists(any(Wrapper.class))).thenReturn(false, true);
        when(passwordEncoder.encode("123456")).thenReturn("hash");
        AdminUserUpdateRequest request = new AdminUserUpdateRequest();
        request.setNewPassword("123456");
        request.setEmail("new@test.com");
        request.setUsername("someone");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.updateUserInfo(1L, request));
        assertEquals(CodeEnum.USERNAME_ALREADY_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void updateUserInfoSuccess() {
        User user = buildUser(1L, "zhangsan", StatusConstant.ENABLED);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(user)).thenReturn(1);
        when(passwordEncoder.encode("new-pwd")).thenReturn("new-hash");
        AdminUserUpdateRequest request = new AdminUserUpdateRequest();
        request.setNewPassword("new-pwd");
        request.setNickname("新昵称");
        request.setRole(RoleType.ADMIN.getCode());
        request.setStatus(StatusConstant.DISABLED);

        userService.updateUserInfo(1L, request);

        assertEquals("new-hash", user.getPassword());
        assertEquals("新昵称", user.getNickname());
        assertEquals(RoleType.ADMIN.getCode(), user.getRole());
        assertEquals(StatusConstant.DISABLED, user.getStatus());
        verify(userMapper).updateById(user);
    }

    @Test
    void createUserWithPasswordMismatchThrows() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("user1");
        request.setPassword("123456");
        request.setConfirmPassword("654321");
        request.setEmail("user1@test.com");
        request.setNickname("用户");

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.createUser(request));
        assertEquals(CodeEnum.PASSWORD_MISMATCH.getCode(), ex.getCode());
    }

    @Test
    void createUserWhenEmailExistsThrows() {
        when(userMapper.exists(any(Wrapper.class))).thenReturn(true);
        UserCreateRequest request = buildCreateRequest();

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.createUser(request));
        assertEquals(CodeEnum.EMAIL_ALREADY_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void createUserWhenUsernameExistsThrows() {
        when(userMapper.exists(any(Wrapper.class))).thenReturn(false, true);
        UserCreateRequest request = buildCreateRequest();

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.createUser(request));
        assertEquals(CodeEnum.USERNAME_ALREADY_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void createUserSuccess() {
        when(userMapper.exists(any(Wrapper.class))).thenReturn(false, false);
        when(passwordEncoder.encode("123456")).thenReturn("hash");
        UserCreateRequest request = buildCreateRequest();

        userService.createUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        User saved = captor.getValue();
        assertEquals("user1", saved.getUsername());
        assertEquals("hash", saved.getPassword());
        assertEquals(RoleType.USER.getCode(), saved.getRole());
        assertEquals(StatusConstant.ENABLED, saved.getStatus());
        assertEquals("user1@test.com", saved.getEmail());
    }

    @Test
    void removeWhenNotExistThrowsUserNotFound() {
        when(userMapper.selectById(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.remove(1L));
        assertEquals(CodeEnum.USER_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void removeSuccess() {
        when(userMapper.selectById(1L)).thenReturn(buildUser(1L, "zhangsan", StatusConstant.ENABLED));
        when(userMapper.deleteById(1L)).thenReturn(1);

        userService.remove(1L);

        verify(userMapper).deleteById(1L);
    }

    @Test
    void changeStatusWhenNotExistThrowsUserNotFound() {
        when(userMapper.selectById(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.changeStatus(1L));
        assertEquals(CodeEnum.USER_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void changeStatusFromEnabledToDisabled() {
        User user = buildUser(1L, "zhangsan", StatusConstant.ENABLED);
        when(userMapper.selectById(1L)).thenReturn(user);

        userService.changeStatus(1L);

        assertEquals(StatusConstant.DISABLED, user.getStatus());
        verify(userMapper).updateById(user);
    }

    @Test
    void changeStatusFromDisabledToEnabled() {
        User user = buildUser(1L, "zhangsan", StatusConstant.DISABLED);
        when(userMapper.selectById(1L)).thenReturn(user);

        userService.changeStatus(1L);

        assertEquals(StatusConstant.ENABLED, user.getStatus());
    }

    @Test
    void isEmailExistsReturnsMapperResult() {
        when(userMapper.exists(any(Wrapper.class))).thenReturn(true);
        assertTrue(userService.isEmailExists("dup@test.com", 2L));
        when(userMapper.exists(any(Wrapper.class))).thenReturn(false);
        assertFalse(userService.isEmailExists("new@test.com", 2L));
    }

    @Test
    void isUsernameExistsReturnsMapperResult() {
        when(userMapper.exists(any(Wrapper.class))).thenReturn(true);
        assertTrue(userService.isUsernameExists("zhangsan", 2L));
        when(userMapper.exists(any(Wrapper.class))).thenReturn(false);
        assertFalse(userService.isUsernameExists("newuser", 2L));
    }

    private UserCreateRequest buildCreateRequest() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("user1");
        request.setPassword("123456");
        request.setConfirmPassword("123456");
        request.setEmail("user1@test.com");
        request.setNickname("用户一");
        return request;
    }
}