package com.lilicould.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论创建DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateDTO {
    @NotBlank(message = "评论内容不能为空")
    private String content;

    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    private Long parentId = 0L; // 0表示顶级评论
}
