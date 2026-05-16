package cn.lilicould.liliblog.controller;

import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.pojo.dto.request.CategoryCreateRequest;
import cn.lilicould.liliblog.pojo.entity.Category;
import cn.lilicould.liliblog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/category")
@Tag(name = "分类接口")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "添加分类")
    public Result<?> addCategory(@RequestBody @Validated CategoryCreateRequest categoryCreateRequest) {

        // 设置排序字段默认值
        if (categoryCreateRequest.getSortOrder() == null) {
            categoryCreateRequest.setSortOrder(1);
        }
        // 拷贝参数
        Category category = new Category();
        BeanUtils.copyProperties(categoryCreateRequest, category);
        categoryService.save(category); // 保存分类

        return Result.success();
    }
}
