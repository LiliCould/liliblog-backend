package com.lilicould.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类创建DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCreateDTO {
    @NotBlank(message = "分类名称不能为空")
    private String name;

    @NotBlank(message = "分类别名不能为空")
    private String slug;

    private String description;

    private Long parentId = 0L;

    private Integer sortOrder = 0;
}
