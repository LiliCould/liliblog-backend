package com.lilicould.blog.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 文章实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Article {
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String contentHtml;
    private String coverImage;
    private String status; // DRAFT, PUBLISHED, HIDDEN，DELETE
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer isTop;
    private Integer isRecommend;
    private Long authorId;
    private Long categoryId;
    private Date createTime;
    private Date updateTime;
    private Date publishTime;

    // 关联字段
    private String authorName; // 作者名称
    private String categoryName; // 分类名称
    private List<Tag> tags; // 标签列表
}
