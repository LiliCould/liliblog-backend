package com.lilicould.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一返回结果类
 * @param <T> T为Data类型
 */
@Data // 自动生成getter和setter方法以及toString方法
@NoArgsConstructor // 自动生成无参构造函数
@AllArgsConstructor // 自动生成所有参数的有参构造函数
public class ResultVO<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    /**
     * 成功返回结果
     * @return Result对象，200状态码，消息为success，data为null
     */
    public static <T> ResultVO<T> success() {
        return new ResultVO<>(200, "success", null, System.currentTimeMillis());
    }

    /**
     * 成功返回结果
     * @param message 消息
     * @return Result对象，200状态码
     */
    public static <T> ResultVO<T> success(String message) {
        return new ResultVO<>(200, message, null, System.currentTimeMillis());
    }

    /**
     * 成功返回结果
     * @param data 数据对象，例如User对象
     * @return Result对象，200状态码，消息为success
     */
    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<>(200, "success", data, System.currentTimeMillis());
    }

    /**
     * 成功返回结果
     * @param message 消息
     * @param data 数据
     * @param <T> T为Data类型，例如User对象
     * @return Result对象，200状态码
     */
    public static <T> ResultVO<T> success(String message, T data) {
        return new ResultVO<>(200, message, data, System.currentTimeMillis());
    }

    /**
     * 失败返回结果
     * @param message 错误消息
     * @return Result对象，500状态码，data为null
     */
    public static <T> ResultVO<T> error(String message) {
        return new ResultVO<>(500, message, null, System.currentTimeMillis());
    }

    /**
     * 失败返回结果
     * @param code 错误码
     * @param message 错误消息
     * @return Result对象，code状态码，data为null
     */
    public static <T> ResultVO<T> error(Integer code, String message) {
        return new ResultVO<>(code, message, null, System.currentTimeMillis());
    }
}