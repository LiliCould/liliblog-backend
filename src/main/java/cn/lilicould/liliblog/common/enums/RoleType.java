package cn.lilicould.liliblog.common.enums;

import lombok.Getter;

/**
 * 用户角色类型枚举
 * 对应数据库存储的 role 字段值
 */
@Getter
public enum RoleType {

    ADMIN(0, "ROLE_ADMIN"),
    USER(1, "ROLE_USER");

    private final Integer code;
    private final String name;

    RoleType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据 code 获取角色名称（完全对应你的 case 逻辑）
     */
    public static String getRoleByCode(Integer code) {
        if (ADMIN.code.equals(code)) {
            return ADMIN.name();
        }
        return USER.name();
    }

}