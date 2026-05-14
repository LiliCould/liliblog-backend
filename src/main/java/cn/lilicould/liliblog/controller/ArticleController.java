package cn.lilicould.liliblog.controller;

import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.pojo.dto.response.ArticleVO;
import cn.lilicould.liliblog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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

    @GetMapping("/{id}")
    @Operation(summary = "根据指定ID获取文章")
    public Result<ArticleVO> getArticle(
            @PathVariable @Parameter(description = "文章ID") Long id) {

        ArticleVO articleVO = articleService.getArticle(id);

        return Result.success(articleVO);
    }
}
