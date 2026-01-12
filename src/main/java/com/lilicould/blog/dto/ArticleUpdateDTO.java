package com.lilicould.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文章更新DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleUpdateDTO {
    private Long id;

    private String title;

    private String slug;

    private String summary;

    private String content;

    private String contentHtml;

    private String coverImage;

    private String status;

    private Long categoryId;

    private List<Long> tagIds;

}
