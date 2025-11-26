package com.lilicould.blog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论更新DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateDTO {
    @NotNull(message = "评论ID不能为空")
    private Long id;

    private String status; // PENDING, APPROVED, REJECTED
}
