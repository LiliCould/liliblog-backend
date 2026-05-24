package cn.lilicould.liliblog.controller.admin;

import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.common.util.PageUtil;
import cn.lilicould.liliblog.pojo.dto.query.CategoryQuery;
import cn.lilicould.liliblog.pojo.dto.response.CategoryVO;
import cn.lilicould.liliblog.pojo.dto.response.PageInfo;
import cn.lilicould.liliblog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
