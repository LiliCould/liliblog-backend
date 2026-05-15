package cn.lilicould.liliblog.controller;

import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.pojo.dto.query.ArticleQuery;
import cn.lilicould.liliblog.pojo.dto.request.ArticleRequest;
import cn.lilicould.liliblog.pojo.dto.response.ArticleDetailsVO;
import cn.lilicould.liliblog.pojo.dto.response.ArticleVO;
import cn.lilicould.liliblog.pojo.dto.response.PageInfo;
import cn.lilicould.liliblog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/article")
@Tag(name = "文章接口")
public class ArticleController {

    private final ArticleService articleService;

    // 构造函数注入
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    @Operation(summary = "获取文章列表")
    public Result<PageInfo<ArticleVO>> getArticleList(@ParameterObject @Validated ArticleQuery articleQuery){
        log.error(articleQuery.toString());
        // 设置默认值
        if (articleQuery.getCurrent() == null) {
            articleQuery.setCurrent(1L);
        }
        if (articleQuery.getSize() == null) {
            articleQuery.setSize(10L);
        }

        PageInfo<ArticleVO> pageInfo = articleService.getArticleList(articleQuery);

        return Result.success(pageInfo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据指定ID获取文章")
    public Result<ArticleDetailsVO> getArticle(@PathVariable @Parameter(description = "文章ID") Long id) {

        ArticleDetailsVO articleDetailsVO = articleService.getArticle(id);

        return Result.success(articleDetailsVO);
    }

    @PostMapping
    @Operation(summary = "保存(写)文章")
    public Result<?> saveArticle(@RequestBody @Validated ArticleRequest articleRequest) {

        articleService.save(articleRequest);

        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文章")
    Result<?> delete(@PathVariable @Parameter(description = "文章ID") Long id) {

        articleService.remove(id);

        return Result.success();
    }

}
