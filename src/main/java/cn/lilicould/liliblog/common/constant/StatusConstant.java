package cn.lilicould.liliblog.common.constant;

public class StatusConstant {
    // ========== 通用状态 ==========
    public static final Integer DISABLED = 0;
    public static final Integer ENABLED = 1;
    // ========== 用户角色 ========== 更推荐RoleType枚举
    public static final Integer ROLE_ADMIN = 0;
    public static final Integer ROLE_USER = 1;
    // ========== 文章状态 ==========
    public static final Integer ARTICLE_PENDING = 0;   // 审核中
    public static final Integer ARTICLE_PUBLISHED = 1; // 已发布
    public static final Integer ARTICLE_DRAFT = 2;     // 草稿
    // ========== 评论状态 ==========
    public static final Integer COMMENT_PENDING = 0;    // 审核中
    public static final Integer COMMENT_PUBLISHED = 1;  // 已发布
    // ========== 点赞目标类型 ==========
    public static final Integer TARGET_ARTICLE = 0;  // 文章
    public static final Integer TARGET_COMMENT = 1;  // 评论
}
