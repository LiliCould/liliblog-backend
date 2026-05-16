package cn.lilicould.liliblog.controller;

import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.pojo.dto.request.CategoryCreateRequest;
import cn.lilicould.liliblog.pojo.dto.response.CategoryVO;
import cn.lilicould.liliblog.pojo.entity.Category;
import cn.lilicould.liliblog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/category")
@Tag(name = "分类接口")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    @GetMapping("/{id}")
    @Operation(summary = "获取指定ID的分类")
    public Result<CategoryVO> getCategory(@PathVariable @Parameter(description = "分类ID") Long id) {

        Category category = categoryService.getById(id);

        if (category == null) {
            return Result.error(CodeEnum.RESOURCE_NOT_FOUND);
        }
        CategoryVO categoryVO = new CategoryVO();
        BeanUtils.copyProperties(category, categoryVO);

        return Result.success(categoryVO);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "添加分类", description = "只有管理员才能添加分类")
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
