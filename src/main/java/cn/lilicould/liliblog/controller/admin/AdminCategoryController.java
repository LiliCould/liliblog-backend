package cn.lilicould.liliblog.controller.admin;

import cn.lilicould.liliblog.common.constant.StatusConstant;
import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.exception.BusinessException;
import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.common.util.PageUtil;
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
        Category category = new Category();
        BeanUtils.copyProperties(categoryCreateRequest, category);
        category.setStatus(StatusConstant.ENABLED);

        // 检查别名和分类名是否已存在
        if (categoryService.exists(new LambdaQueryWrapper<Category>().eq(Category::getSlug, category.getSlug()))
        || categoryService.exists(new LambdaQueryWrapper<Category>().eq(Category::getName, category.getName())) ) {
            throw new BusinessException(CodeEnum.CATEGORY_ALREADY_EXISTS);
        }

        categoryService.save(category);
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
        Category category = categoryService.getById(id);

        if (category == null) {
            throw new BusinessException(CodeEnum.CATEGORY_NOT_FOUND);
        }
        categoryService.removeById(id);
        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "批量删除分类", description = "管理员后台，批量删除分类")
    public Result<String> delete(@RequestBody List<Long> ids) {
        categoryService.removeByIds(ids);
        return Result.success();
    }
}
