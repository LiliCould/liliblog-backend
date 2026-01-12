package com.lilicould.blog.entity;


import java.util.Date;

public class Comment {
    private Long id;
    private String content;
    private Integer AuthorId; // 评论者ID
    private Integer articleId;
    private Integer parentId;
    private String status;
    private Integer likeCount;
    private String ipAddress;
    private String userAgent;
    private Date createTime;
    private Date updateTime;

    // 关联字段
    private String authorName; // 评论者名称
    private String articleTitle; // 文章标题
}
