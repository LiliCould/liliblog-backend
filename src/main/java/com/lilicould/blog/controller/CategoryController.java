package com.lilicould.blog.controller;

import com.lilicould.blog.dto.CategoryCreateDTO;
import com.lilicould.blog.dto.CategoryUpdateDTO;
import com.lilicould.blog.entity.Category;
import com.lilicould.blog.service.CategoryService;
import com.lilicould.blog.vo.ResultVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 */
@RequestMapping("/api/categories")
@RestController
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 获取分类列表
     * @return 分类列表
     */
    @GetMapping
    public ResultVO<List<Category>> list() {
        return ResultVO.success(categoryService.list());
    }

    /**
     * 根据ID获取分类
     * @return 分类
     */
    @GetMapping("/{id}")
    public ResultVO<Category> get(@PathVariable("id") Long id) {
        return ResultVO.success(categoryService.get(id));
    }

    /**
     * 添加分类
     * @param category 分类
     * @return 分类
     */
    @PostMapping()
    public ResultVO<Void> add(@Valid @RequestBody CategoryCreateDTO category) {
        categoryService.add(category);
        return ResultVO.success("添加分类成功");
    }

    /**
     * 更新分类
     * @param category 分类
     * @return 分类
     */
    @PutMapping()
    public ResultVO<Void> update(@Valid @RequestBody CategoryUpdateDTO category) {
        categoryService.update(category);
        return ResultVO.success("更新分类成功");
    }

    /**
     * 删除分类
     * @param id 分类ID
     * @return 分类
     */
    @DeleteMapping("/{id}")
    public ResultVO<Void> delete(@PathVariable("id") Long id) {
        categoryService.delete(id);
        return ResultVO.success("删除分类成功");
    }
}
