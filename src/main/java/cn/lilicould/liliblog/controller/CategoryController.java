package cn.lilicould.liliblog.controller;

import cn.lilicould.liliblog.common.constant.StatusConstant;
import cn.lilicould.liliblog.common.context.BaseContext;
import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.exception.BusinessException;
import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.pojo.dto.query.CategoryQuery;
import cn.lilicould.liliblog.pojo.dto.request.CategoryCreateRequest;
import cn.lilicould.liliblog.pojo.dto.request.CategoryUpdateRequest;
import cn.lilicould.liliblog.pojo.dto.response.CategoryVO;
import cn.lilicould.liliblog.pojo.dto.response.PageInfo;
import cn.lilicould.liliblog.pojo.entity.Category;
import cn.lilicould.liliblog.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
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
        // 判断分类是否可用
        if (!StatusConstant.ENABLED.equals(category.getStatus()) && !BaseContext.isAdmin()) {
            return Result.error(CodeEnum.RESOURCE_NOT_FOUND);
        }
        CategoryVO categoryVO = new CategoryVO();
        BeanUtils.copyProperties(category, categoryVO);

        return Result.success(categoryVO);
    }

    @GetMapping
    @Operation(summary = "分页获取分类列表")
    public Result<PageInfo<CategoryVO>> getCategoryList(@ParameterObject CategoryQuery categoryQuery) {
        // 设置分页默认值
        if (categoryQuery.getCurrent() == null) {
            categoryQuery.setCurrent(1L);
        }
        if (categoryQuery.getSize() == null) {
            categoryQuery.setSize(10L);
        }

        PageInfo<CategoryVO> pageInfo = categoryService.getCategoryList(categoryQuery);

        return Result.success(pageInfo);
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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "修改分类", description = "只有管理员才能修改分类")
    public Result<?> updateCategory(
            @PathVariable @Parameter(description = "分类ID") Long id,
            @RequestBody @Validated CategoryUpdateRequest categoryCreateRequest) {

        categoryService.update(id, categoryCreateRequest);

        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除分类", description = "只有管理员才能删除分类")
    public Result<?> deleteCategory(@PathVariable @Parameter(description = "分类ID") Long id) {

        if(!categoryService.exists(new LambdaQueryWrapper<Category>().eq(Category::getId, id))) {
            throw new BusinessException(CodeEnum.CATEGORY_NOT_FOUND);
        }

        categoryService.removeById(id);

        return Result.success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "切换分类状态", description = "只有管理员才能切换分类状态")
    public Result<?> toggleStatus(@PathVariable @Parameter(description = "分类ID") Long id) {

        // 判断分类是否存在
        if(!categoryService.exists(new LambdaQueryWrapper<Category>().eq(Category::getId, id))) {
            throw new BusinessException(CodeEnum.CATEGORY_NOT_FOUND);
        }

        Category category = categoryService.getById(id);

        if (category.getStatus() == null) { // 一般不会出现这种情况
            category.setStatus(StatusConstant.ENABLED);
        }

        category.setStatus(StatusConstant.ENABLED.equals(category.getStatus()) ? StatusConstant.DISABLED : StatusConstant.ENABLED);
        categoryService.updateById(category);

        return Result.success();
    }
}
