package com.lilicould.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签创建DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagCreateDTO {
    @NotBlank(message = "标签名称不能为空")
    private String name;

    @NotBlank(message = "标签别名不能为空")
    private String slug;

    private String color = "#ff9900";
}
