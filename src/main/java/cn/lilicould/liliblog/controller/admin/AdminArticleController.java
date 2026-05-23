package cn.lilicould.liliblog.controller.admin;

import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.pojo.dto.query.ArticleQuery;
import cn.lilicould.liliblog.pojo.dto.response.ArticleDetailsVO;
import cn.lilicould.liliblog.pojo.dto.response.ArticleVO;
import cn.lilicould.liliblog.pojo.dto.response.PageInfo;
import cn.lilicould.liliblog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/article")
@Tag(name = "文章管理接口", description = "管理员后台，管理文章,不加更新和新增文章的接口，只由用户创建和修改")
@PreAuthorize("hasRole('ADMIN')")
public class AdminArticleController {

    private final ArticleService articleService;

    @GetMapping
    @Operation(summary = "获取文章列表", description = "管理员后台，获取文章列表(其实和用户端行为一致，但为了防止混淆，单独设置接口)")
    public Result<PageInfo<ArticleVO>> getArticleList(@ParameterObject @Validated ArticleQuery articleQuery){
        // 设置默认值
        if (articleQuery.getSize() == null) {
            articleQuery.setSize(10L);
        }
        if (articleQuery.getCurrent() == null) {
            articleQuery.setCurrent(1L);
        }
        PageInfo<ArticleVO> pageInfo = articleService.getArticleList(articleQuery);

        return Result.success(pageInfo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据指定ID获取文章", description = "管理员后台，根据指定ID获取文章,用于管理员预览文章审核")
    public Result<ArticleDetailsVO> getArticle(@PathVariable @Parameter(description = "文章ID") Long id) {

        ArticleDetailsVO articleDetailsVO = articleService.getArticle(id);

        if (articleDetailsVO == null) {
            return Result.error(CodeEnum.ARTICLE_NOT_FOUND);
        }

        return Result.success(articleDetailsVO);
    }

    @GetMapping("/{id}/{status}")
    @Operation(summary = "审核文章", description = "管理员后台，审核文章")
    public Result<String> auditArticle(@PathVariable @Parameter(description = "文章ID") Long id,
                                       @PathVariable @Parameter(description = "审核结果，1-审核通过（发布），2-审核失败（草稿）") Integer status,
                                       @RequestParam(required = false) @Parameter(description = "审核结果描述,不传则使用默认值,如果不通过，建议加原因,通过可加可不加")
                                           String reason) {

        articleService.auditArticle(id, status, reason);

        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文章", description = "管理员后台，删除文章")
    public Result<String> deleteArticle(@PathVariable @Parameter(description = "文章ID") Long id) {

        articleService.remove(id);

        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "批量删除文章", description = "管理员后台，批量删除文章")
    public Result<String> deleteArticle(@RequestBody @Parameter(description = "文章ID列表")
                                        List<Long> ids) {

        articleService.removeByIds(ids);

        return Result.success();
    }
}
