package cn.lilicould.liliblog.constant;

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
    // ========== 删除状态 ==========
    public static final Integer DELETED = 1;
    public static final Integer NOT_DELETED = 0;
    // ========== 文件上传状态 ==========
    public static final Integer FILE_PENDING = 0;   // 待确认
    public static final Integer FILE_SUCCESS = 1;   // 成功
    public static final Integer FILE_FAILED = 2;    // 失败
    public static final Integer FILE_EXPIRED = 3;   // 过期
    public static final Integer FILE_REJECTED = 4;  // 拒绝
    // ========== 文件上传来源 ==========
    public static final Integer FILE_SOURCE_DIRECT = 1; // 前端直传
    public static final Integer FILE_SOURCE_PROXY = 2;  // 后端中转
    public static final Integer FILE_SOURCE_LEGACY = 3; // 历史文件导入
}
