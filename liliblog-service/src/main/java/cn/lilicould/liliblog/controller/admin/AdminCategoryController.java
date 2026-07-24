package cn.lilicould.liliblog.controller.admin;

import cn.lilicould.enums.CodeEnum;
import cn.lilicould.exception.BusinessException;
import cn.lilicould.result.Result;
import cn.lilicould.liliblog.util.PageUtil;
import cn.lilicould.query.CategoryQuery;
import cn.lilicould.request.CategoryCreateRequest;
import cn.lilicould.request.CategoryUpdateRequest;
import cn.lilicould.response.CategoryVO;
import cn.lilicould.response.PageInfo;
import cn.lilicould.entity.Category;
import cn.lilicould.liliblog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/category")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "分类管理接口")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "获取分类列表", description = "管理员后台，获取分类列表")
    public Result<PageInfo<CategoryVO>> list(@ParameterObject CategoryQuery categoryQuery) {

        // 设置分页默认值
        PageUtil.setDefault(categoryQuery);

        PageInfo<CategoryVO> pageInfo = categoryService.getCategoryList(categoryQuery);

        return Result.success(pageInfo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情", description = "管理员后台，获取分类详情")
    public Result<CategoryVO> detail(@Parameter(description = "分类ID", example = "1") @PathVariable Long id) {
        Category category = categoryService.getById(id);

        if (category == null) {
            throw new BusinessException(CodeEnum.CATEGORY_NOT_FOUND);
        }

        CategoryVO categoryVO = new CategoryVO();

        BeanUtils.copyProperties(category, categoryVO);
        return Result.success(categoryVO);
    }

    @PostMapping
    @Operation(summary = "创建分类", description = "管理员后台，创建分类")
    public Result<String> create(@RequestBody CategoryCreateRequest categoryCreateRequest) {

        categoryService.save(categoryCreateRequest);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新分类", description = "管理员后台，更新分类")
    public Result<String> update(@PathVariable @Parameter(description = "分类ID", example = "1") Long id,
                                 @RequestBody CategoryUpdateRequest categoryCreateRequest) {
        categoryService.update(id, categoryCreateRequest);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类", description = "管理员后台，删除分类，删除单个分类也可使用批量删除，只传一个id即可")
    public Result<String> delete(@Parameter(description = "分类ID", example = "1") @PathVariable Long id) {

        categoryService.remove(id);

        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "批量删除分类", description = "管理员后台，批量删除分类")
    public Result<String> delete(@RequestBody List<Long> ids) {

        categoryService.remove(ids);

        return Result.success();
    }
}
