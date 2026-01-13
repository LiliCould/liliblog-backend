package com.lilicould.blog.dao;

import com.lilicould.blog.dto.CategoryCreateDTO;
import com.lilicould.blog.dto.CategoryUpdateDTO;
import com.lilicould.blog.entity.Category;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 分类数据访问接口
 */
public interface CategoryMapper {
    List<Category> selectAll();
    Category selectById(@Param("id") Long id);
    void insert(CategoryCreateDTO category);
    void update(CategoryUpdateDTO category);
    void deleteById(@Param("id") Long id);
}
