package cn.lilicould.liliblog.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CodeEnum {
    // 成功
    SUCCESS(0, "成功"),

    // 通用错误 1xxx
    COMMON_PARAM_ERROR(1000,"参数异常"),
    PARAM_MISSING(1001, "缺少必要参数"),
    PARAM_FORMAT_ERROR(1002, "参数格式错误"),
    REQUEST_METHOD_NOT_SUPPORTED(1003, "请求方法不支持"),

    // 认证与用户 2xxx
    LOGIN_ERROR(2000, "登录失败"),
    NOT_LOGGED_IN(2001, "请先登录"),
    ACCOUNT_OR_PASSWORD_ERROR(2002, "账号或密码错误"),
    USER_NOT_FOUND(2003, "用户不存在"),
    NO_PERMISSION(2004, "无权限执行此操作"),
    TOKEN_EXPIRED(2005, "登录已过期，请重新登录"),
    ACCOUNT_DISABLED(2006,"账号被禁用，请联系管理员"),
    LOGIN_TOO_FREQUENT(2007, "登录过于频繁，请稍后重试"),
    PASSWORD_MISMATCH(2008, "两次密码不一致"),
    USERNAME_ALREADY_EXISTS(2009, "用户名已存在"),
    EMAIL_ALREADY_EXISTS(2010, "邮箱已被注册"),
    NOT_SUPPORTED(2011, "不支持的登录类型"),
    NO_REFRESH_TOKEN(2012, "未找到刷新令牌"),
    OLD_PASSWORD_ERROR(2013, "旧密码错误"),
    PASSWORD_NOT_MATCH(2014, "密码不匹配"),

    // 资源操作错误 3xxx
    RESOURCE_NOT_FOUND(3000, "资源不存在"),
    ARTICLE_ALREADY_EXISTS(3001, "文章已存在"),
    ARTICLE_NOT_FOUND(3002, "文章不存在"),
    CATEGORY_ALREADY_EXISTS(3003, "分类已存在"),
    CATEGORY_NOT_FOUND(3004, "分类不存在"),
    TAG_ALREADY_EXISTS(3005, "标签已存在"),
    TAG_NOT_FOUND(3006, "标签不存在"),
    SLUG_ALREADY_EXISTS(3007, "别名已存在"),
    SLUG_NOT_FOUND(3008, "别名不存在"),
    REPEAT_OPERATION(3009,"请勿重复操作"),

    // 系统错误 5xxx
    SYSTEM_ERROR(5000, "系统异常，请稍后重试"),
    DB_ERROR(5001, "数据库错误"),
    FILE_UPLOAD_FAIL(5002, "文件上传失败");

    private final Integer code;
    private final String message;
}
