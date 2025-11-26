package com.lilicould.blog.exception;

/**
 * 自定义业务异常
 * @author LiliCould
 */
public class BusinessException extends RuntimeException{
    private Integer code;

    /**
     * 构造方法,错误码默认500
     * @param message 异常信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 构造方法
     * @param message 错误信息
     * @param code 错误码
     */
    public BusinessException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    /**
     * 获取错误码
     * @return 错误码
     */
    public Integer getCode() {
        return code;
    }
}
