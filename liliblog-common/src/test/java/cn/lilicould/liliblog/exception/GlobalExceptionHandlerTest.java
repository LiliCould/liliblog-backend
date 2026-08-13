package cn.lilicould.liliblog.exception;

import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * GlobalExceptionHandler全局异常处理测试类
 *
 * @author lilicould
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleBusinessException() {
        BusinessException e = new BusinessException(2002, "账号或密码错误");

        Result<?> result = handler.handleBusinessException(e);

        assertEquals(2002, result.getCode());
        assertEquals("账号或密码错误", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void handleNoResourceFoundException() {
        NoResourceFoundException e = new NoResourceFoundException(HttpMethod.GET, "/api/none", "");

        Result<?> result = handler.handleNoResourceFoundException(e);

        assertEquals(CodeEnum.RESOURCE_NOT_FOUND.getCode(), result.getCode());
        assertEquals(CodeEnum.RESOURCE_NOT_FOUND.getMessage(), result.getMsg());
    }

    @Test
    void handleNoSuchMethodError() {
        Result<?> result = handler.handleNoSuchMethodError(new NoSuchMethodError("POST"));

        assertEquals(CodeEnum.REQUEST_METHOD_NOT_SUPPORTED.getCode(), result.getCode());
        assertEquals(CodeEnum.REQUEST_METHOD_NOT_SUPPORTED.getMessage(), result.getMsg());
    }

    @Test
    void handleMethodArgumentNotValidWithMessage() throws Exception {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        bindingResult.addError(new FieldError("obj", "username", "用户名不能为空"));

        MethodArgumentNotValidException e = new MethodArgumentNotValidException(null, bindingResult);

        Result<?> result = handler.handleMethodArgumentNotValid(e);

        assertEquals(CodeEnum.COMMON_PARAM_ERROR.getCode(), result.getCode());
        assertEquals("用户名不能为空", result.getMsg());
    }

    @Test
    void handleMethodArgumentNotValidWithoutMessage() throws Exception {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        bindingResult.addError(new FieldError("obj", "username", null));

        MethodArgumentNotValidException e = new MethodArgumentNotValidException(null, bindingResult);

        Result<?> result = handler.handleMethodArgumentNotValid(e);

        assertEquals(CodeEnum.COMMON_PARAM_ERROR.getCode(), result.getCode());
    }

    @Test
    void handleAuthorizationDenied() {
        AuthorizationDeniedException e = new AuthorizationDeniedException("无权限");

        Result<?> result = handler.handleAuthorizationDenied(e);

        assertEquals(CodeEnum.NO_PERMISSION.getCode(), result.getCode());
        assertEquals(CodeEnum.NO_PERMISSION.getMessage(), result.getMsg());
    }

    @Test
    void handleSqlException() {
        Result<?> result = handler.handleSqlException(new SQLException("sql error"));

        assertEquals(CodeEnum.DB_ERROR.getCode(), result.getCode());
        assertEquals(CodeEnum.DB_ERROR.getMessage(), result.getMsg());
    }

    @Test
    void handleException() {
        Result<?> result = handler.handleException(new Exception("boom"));

        assertEquals(CodeEnum.SYSTEM_ERROR.getCode(), result.getCode());
        assertEquals(CodeEnum.SYSTEM_ERROR.getMessage(), result.getMsg());
    }
}