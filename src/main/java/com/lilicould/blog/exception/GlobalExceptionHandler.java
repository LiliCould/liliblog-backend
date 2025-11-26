package com.lilicould.blog.exception;

import com.lilicould.blog.vo.ResultVO;

import org.apache.ibatis.exceptions.PersistenceException;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;


import java.sql.SQLException;
import java.util.stream.Collectors;

import static org.springframework.core.NestedExceptionUtils.getRootCause;

/**
 * 全局异常处理器
 * @author LiliCould
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * @param e 业务异常，继承自RuntimeException，包含错误码和错误信息
     * @return 错误结果
     */
    @ExceptionHandler(BusinessException.class)
    public ResultVO<Void> handleBusinessException(BusinessException e) {
        return ResultVO.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数验证异常
     * @param e 参数验证异常，继承自MethodArgumentNotValidException
     * @return 错误结果，例如{"code": 400, "message": "参数错误: username不能为空"}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultVO<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult() // 获取参数验证结果
                .getFieldErrors() // 获取字段错误
                .stream() // 转换成流
                .map(FieldError::getDefaultMessage) // 获取错误信息
                .collect(Collectors.joining(", ")); // 转换成字符串
        return ResultVO.error(400, message);
    }


    /**
     * MyBatis系统异常处理
     * 处理MyBatisSystemException等系统级异常
     */
    @ExceptionHandler(MyBatisSystemException.class)
    public ResultVO<Void> handleMyBatisSystemException(MyBatisSystemException e) {
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            message = "MyBatis系统异常：";
        }

        // 获取cause中的详细信息
        Throwable cause = e.getCause();
        if (cause != null && cause.getMessage() != null && !cause.getMessage().isEmpty()) {
            message += " (原因 -> " + cause.getMessage() + ")";
        }

        return ResultVO.error(500, "MyBatis系统异常: " + message);
    }

    /**
     * 处理数据库完整性异常
     * @param e 数据库完整性异常
     * @return 错误结果，例如{"code": 400, "message": "数据格式错误: 字段值超出允许范围"}
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResultVO<Void> handleSQLException(DataIntegrityViolationException e) {
        String message = e.getMessage();
        Throwable rootCause = getRootCause(e);

        // 针对不同的数据完整性错误提供友好提示
        if (rootCause instanceof SQLException sqlEx) {
            String sqlMessage = sqlEx.getMessage();
            if (sqlMessage.contains("Data truncated")) {
                return ResultVO.error(400, "数据格式错误: 字段值超出允许范围，具体原因 -> ("+ message +")");
            } else if (sqlMessage.contains("Duplicate entry")) {
                return ResultVO.error(400, "数据重复: 已存在相同记录，具体原因 -> ("+ message +")");
            } else if (sqlMessage.contains("cannot be null")) {
                return ResultVO.error(400, "数据错误: 必填字段不能为空，具体原因 -> ("+ message +")");
            } else if (sqlMessage.contains("foreign key constraint")) {
                return ResultVO.error(400, "数据关联错误: 关联数据不存在，具体原因 -> ("+ message +")");
            }
        }
        // 通用数据完整性错误
        String userMessage = "数据完整性错误";
        if (rootCause != null && rootCause.getMessage() != null) {
            userMessage += ": " + rootCause.getMessage();
        }
        return ResultVO.error(400, userMessage);
    }

    /**
     * 处理SQL语法错误
     * @param e SQL语法错误
     * @return 错误结果，例如{"code": 400, "message": "SQL语法错误: 字段名错误"}
     */
    @ExceptionHandler(BadSqlGrammarException.class)
    public ResultVO<Void> handleBadSqlGrammarException(BadSqlGrammarException e) {
        String message = "SQL语法错误:";
        Throwable rootCause = getRootCause(e);
        if (rootCause != null && rootCause.getMessage() != null) {
            message += " (原因 -> " + rootCause.getMessage() + ")";
        }
        return ResultVO.error(500, message);
    }


    /**
     * 处理其他异常
     * @param e 其他异常
     * @return 错误结果，例如{"code": 500, "message": "系统异常: java.lang.NullPointerException"}
     */
    @ExceptionHandler(Exception.class)
    public ResultVO<Void> handleException(Exception e) {
        String exceptionName = e.getClass().getName();
        String message = e.getMessage();
        Throwable cause = e.getCause();
        if (cause != null && cause.getMessage() != null && !cause.getMessage().isEmpty()) {
            message += " (原因 -> " + cause.getMessage() + ")";
        }
        if (message == null || message.isEmpty()) {
            // 如果message为null，则返回异常类名
            message = e.getClass().getName();
            return ResultVO.error(500, "系统异常: 异常类名->" + message);
        }
        return ResultVO.error(500, "系统异常" +
                "异常类名 " + "-> (" + exceptionName + ")," +
                "异常信息 " + "-> (" + message+ ")," +
                "具体信息请查看日志");
    }
}