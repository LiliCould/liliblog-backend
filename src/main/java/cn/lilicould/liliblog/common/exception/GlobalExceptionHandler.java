package cn.lilicould.liliblog.common.exception;

import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.result.Result;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;


/**
 * 全局异常处理
 */
@RestControllerAdvice
@Hidden // 让knife4j不处理全局异常处理类
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理其他业务异常（如有自定义异常）
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常: code={}, msg={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 无对应资源异常异常
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> handleNoResourceFoundException(NoResourceFoundException e) {
        log.error("资源不存在 -> {}", e.getMessage());
        return Result.error(CodeEnum.RESOURCE_NOT_FOUND.getCode(),CodeEnum.RESOURCE_NOT_FOUND + "->" + e.getMessage());
    }

    @ExceptionHandler(NoSuchMethodError.class)
    public Result<?> handleNoSuchMethodError(NoSuchMethodError e) {
        log.error("请求方法 -> {} 不被支持",e.getMessage());

        return Result.error(CodeEnum.REQUEST_METHOD_NOT_SUPPORTED);
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        // 获取第一个错误消息
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> {
                    if (fieldError.getDefaultMessage() != null)
                        return fieldError.getDefaultMessage();
                    return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                })
                .findFirst()
                .orElse("参数验证失败");

        log.error("参数验证失败 -> {}", message);
        return Result.error(CodeEnum.COMMON_PARAM_ERROR.getCode(),message);
    }

    /**
     * 处理权限不足
     * @param e 权限不足异常
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public Result<?> handleAuthorizationDenied(AuthorizationDeniedException e) {
        log.error("用户权限不足 -> {}",e.getMessage());
        return Result.error(CodeEnum.NO_PERMISSION);
    }

    /**
     * 处理SQL异常
     * @param e 异常信息
     * @return
     */
    @ExceptionHandler(SQLException.class)
    public Result<?> handleSqlException (SQLException e) {
        log.error("数据库操作异常 -> {}",e.getMessage());

        return Result.error(CodeEnum.DB_ERROR);
    }

    /**
     * 兜底处理未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常 -> {},异常类名 -> {}", e.getMessage(),e.getClass());
        return Result.error(CodeEnum.SYSTEM_ERROR);
    }
}
