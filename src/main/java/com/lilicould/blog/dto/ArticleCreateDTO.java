package com.lilicould.blog.dto;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文章创建DTO
 * @author lilicould
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleCreateDTO {
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "别名不能为空")
    private String slug;
    private String summary;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String contentHtml =  "";

    private String coverImage = "http://demo";

    private String status = "DRAFT"; // DRAFT, PUBLISHED, HIDDEN，DELETED

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    private List<Long> tagIds;

}
