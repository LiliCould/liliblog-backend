package cn.lilicould.liliblog.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CodeEnum {
    SUCCESS(200, "成功"),
    ACCOUNT_NOT_MATCH_PASSWORD(401, "账号或密码错误"),
    ERROR(500, "失败"),
    Token_IS_NOT_IN_HEADER(100001, "请求头中不存在token"),
    TOKEN_IS_ERROR(100002, "token验证失败:token错误，可能被篡改"),
    TOKEN_IS_NONE_MATCH(100003, "token验证失败，token不匹配"),
    TOKEN_ID_NOT_EXIST(100004, "token验证失败，token不存在"),
    SAVA_ERROR(100005, "保存到数据库失败"),
    UPDATE_ERROR(100006, "更新数据库失败"),
    DELETE_ERROR(100007,"删除失败" ),
    FOREIGN_KEY_CONSTRAINT_VIOLATION(100008, "当前数据被其他记录引用，无法删除，若删除的数据为用户数据，可尝试禁用账号。"),  // 外键约束
    DUPLICATE_ENTRY_FAIL(100009, "存在重复数据"),
    DATABASE_ERROR(100010, "数据库异常"),
    SYSTEM_ERROR(100011, "系统异常"),
    NO_RESOURCE_FOUND(100012, "未找到资源"),
    QUERY_ERROR(100013,"查询失败"),
    NO_THIS_DICT_VALUE(100014, "数据库中无此字典"),
    NO_THIS_PRODUCT(100015,"数据库中无此产品"),

    THE_PHONE_IS_EXIST(100016, "手机号已存在"),
    USER_ERROR(100017, "用户异常"),
    PARAMETER_VALIDATION_ERROR(100018, "参数验证失败"),
    AUTHORIZATION_DENIED_ERROR(100019,"权限不足");

    private final Integer code;
    private final String message;
}
