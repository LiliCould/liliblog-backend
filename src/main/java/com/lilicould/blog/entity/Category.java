package com.lilicould.blog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 分类实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Integer status;
    private Date createTime;
    private Date updateTime;

}
