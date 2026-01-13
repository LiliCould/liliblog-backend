package com.lilicould.blog.service;

import com.lilicould.blog.dto.CategoryCreateDTO;
import com.lilicould.blog.dto.CategoryUpdateDTO;
import com.lilicould.blog.entity.Category;

import java.util.List;

/**
 * 分类服务
 */
public interface CategoryService {
    List<Category> list();
    Category get(Long id);
    void delete(Long id);
    void add(CategoryCreateDTO category);
    void update(CategoryUpdateDTO category);
}
