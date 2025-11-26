package com.lilicould.blog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类更新DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryUpdateDTO {
    @NotNull(message = "分类ID不能为空")
    private Long id;

    private String name;

    private String slug;

    private String description;

    private Long parentId;

    private Integer sortOrder;

    private Integer status;
}
