package com.lilicould.blog.service.impl;
import com.lilicould.blog.annotation.Log;
import com.lilicould.blog.dao.CategoryMapper;
import com.lilicould.blog.dto.CategoryCreateDTO;
import com.lilicould.blog.dto.CategoryUpdateDTO;
import com.lilicould.blog.entity.Category;
import com.lilicould.blog.exception.BusinessException;
import com.lilicould.blog.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分类服务实现类
 */
@Service
@Transactional(isolation = Isolation.READ_COMMITTED,rollbackFor = Exception.class)
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    @Log(value = "获取所有分类服务")
    public List<Category> list() {
        List<Category> categories = categoryMapper.selectAll();
        if (categories != null) {
            return categories;
        } else {
            throw new BusinessException("暂无分类数据", 500);
        }
    }

    @Override
    @Log(value = "根据ID获取分类服务")
    public Category get(Long id) {
        if (id == null) {
            throw new BusinessException("参数错误", 400);
        }

        Category category = categoryMapper.selectById(id);

        // 如果分类不存在，则抛出异常
        if (category == null) {
            throw new BusinessException("分类不存在", 404);
        }

        return category;
    }

    @Override
    @Log(value = "删除分类服务")
    public void delete(Long id) {
        if (id == null) {
            throw new BusinessException("参数错误", 400);
        }

        // 如果分类不存在，则抛出异常
        if (categoryMapper.selectById(id) == null) {
            throw new BusinessException("分类不存在", 404);
        }

        categoryMapper.deleteById(id);
    }

    @Override
    @Log(value = "添加分类服务")
    public void add(CategoryCreateDTO category) {
        if (category == null) {
            throw new BusinessException("参数错误", 400);
        }

        categoryMapper.insert(category);
    }

    @Override
    @Log(value = "更新分类服务")
    public void update(CategoryUpdateDTO category) {
        if (category == null) {
            throw new BusinessException("参数错误", 400);
        }

        // 如果分类不存在，则抛出异常
        if (categoryMapper.selectById(category.getId()) == null) {
            throw new BusinessException("分类不存在", 404);
        }

        categoryMapper.update(category);
    }
}
