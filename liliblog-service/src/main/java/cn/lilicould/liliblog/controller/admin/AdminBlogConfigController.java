package cn.lilicould.liliblog.controller.admin;

import cn.lilicould.liliblog.response.BlogConfigSectionVO;
import cn.lilicould.liliblog.result.Result;
import cn.lilicould.liliblog.service.BlogConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

import java.util.List;

/**
 * 站点配置管理接口
 */
@RestController
@RequestMapping("/api/admin/blog-config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "站点配置管理接口")
public class AdminBlogConfigController {

    private final BlogConfigService blogConfigService;

    @GetMapping
    @Operation(summary = "获取全部分组", description = "包含未启用的分组，供管理端编辑")
    public Result<List<BlogConfigSectionVO>> list() {
        return Result.success(blogConfigService.listSections());
    }

    @GetMapping("/{key}")
    @Operation(summary = "获取指定分组")
    public Result<BlogConfigSectionVO> get(
            @Parameter(description = "分组键", required = true) @PathVariable String key) {
        return Result.success(blogConfigService.getSection(key));
    }

    @PutMapping("/{key}")
    @Operation(summary = "新增或更新分组", description = "分组键不存在时新增，存在时整体替换该分组内容")
    public Result<?> upsert(
            @Parameter(description = "分组键", required = true) @PathVariable String key,
            @Parameter(description = "是否启用：0-禁用，1-启用，不传表示不修改") @RequestParam(required = false) Integer enabled,
            @Parameter(description = "备注，不传表示不修改") @RequestParam(required = false) String remark,
            @RequestBody JsonNode value) {

        blogConfigService.upsertSection(key, value, enabled, remark);

        return Result.success();
    }

    @DeleteMapping("/{key}")
    @Operation(summary = "删除分组")
    public Result<?> delete(
            @Parameter(description = "分组键", required = true) @PathVariable String key) {
        blogConfigService.deleteSection(key);
        return Result.success();
    }
}
