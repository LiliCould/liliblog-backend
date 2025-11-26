package com.lilicould.blog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签更新DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagUpdateDTO {
    @NotNull(message = "标签ID不能为空")
    private Long id;

    private String name;

    private String slug;

    private String color;
}
