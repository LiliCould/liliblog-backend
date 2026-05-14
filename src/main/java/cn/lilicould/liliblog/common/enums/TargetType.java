package cn.lilicould.liliblog.common.enums;

import lombok.Getter;

/**
 * 目标类型枚举
 */
@Getter
public enum TargetType {
    ARTICLE(0, "文章"),
    COMMENT(1, "评论"),
    USER(2, "用户");

    private final Integer code;
    private final String name;

    TargetType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
