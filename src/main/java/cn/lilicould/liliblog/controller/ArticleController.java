package cn.lilicould.liliblog.controller;

import cn.lilicould.liliblog.common.constant.StatusConstant;
import cn.lilicould.liliblog.common.context.BaseContext;
import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.enums.TargetType;
import cn.lilicould.liliblog.common.exception.BusinessException;
import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.pojo.dto.query.ArticleQuery;
import cn.lilicould.liliblog.pojo.dto.request.ArticleCreateRequest;
import cn.lilicould.liliblog.pojo.dto.request.ArticleUpdateRequest;
import cn.lilicould.liliblog.pojo.dto.response.ArticleDetailsVO;
import cn.lilicould.liliblog.pojo.dto.response.ArticleVO;
import cn.lilicould.liliblog.pojo.dto.response.PageInfo;
import cn.lilicould.liliblog.pojo.entity.Article;
import cn.lilicould.liliblog.pojo.entity.LikeRecord;
import cn.lilicould.liliblog.service.ArticleService;
import cn.lilicould.liliblog.service.LikeRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    private final LikeRecordService likeRecordService;

    // 构造函数注入
    public ArticleController(ArticleService articleService, LikeRecordService likeRecordService) {
        this.articleService = articleService;
        this.likeRecordService = likeRecordService;
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

        if (articleDetailsVO == null) {
            return Result.error(CodeEnum.ARTICLE_NOT_FOUND);
        }

        return Result.success(articleDetailsVO);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "根据指定slug获取文章")
    public Result<ArticleDetailsVO> getArticleBySlug(@PathVariable @Parameter(description = "文章slug") String slug) {

        ArticleDetailsVO articleDetailsVO = articleService.getArticleBySlug(slug);

        if (articleDetailsVO == null) {
            return Result.error(CodeEnum.ARTICLE_NOT_FOUND);
        }

        return Result.success(articleDetailsVO);
    }

    @PostMapping
    @Operation(summary = "保存(写)文章")
    public Result<?> saveArticle(@RequestBody @Validated ArticleCreateRequest articleCreateRequest) {

        articleService.save(articleCreateRequest);

        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新文章")
    public Result<?> updateArticle(
            @PathVariable @Parameter(description = "文章ID") Long id,
            @RequestBody @Validated ArticleUpdateRequest articleUpdateRequest) {

        articleService.update(id, articleUpdateRequest);

        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文章")
    public Result<?> delete(@PathVariable @Parameter(description = "文章ID") Long id) {

        articleService.remove(id);

        return Result.success();
    }

    @PutMapping("/{id}/like")
    @Operation(summary = "文章点赞", description = "需要登录")
    public Result<?> like(@PathVariable @Parameter(description = "文章ID") Long id) {
        LikeRecord likeRecord = new LikeRecord();
        likeRecord.setUserId(BaseContext.getCurrentUserId());
        likeRecord.setTargetId(id);
        likeRecord.setTargetType(TargetType.ARTICLE.getCode());

        // 检查文章是否存在且状态符合
        if (!articleService.exists(new LambdaQueryWrapper<Article>().eq(Article::getId, id).eq(Article::getStatus, StatusConstant.ARTICLE_PUBLISHED))) {
            throw new BusinessException(CodeEnum.ARTICLE_NOT_FOUND);
        }

        // 已经点赞
        if (likeRecordService.exists(new LambdaQueryWrapper<LikeRecord>().eq(LikeRecord::getUserId, BaseContext.getCurrentUserId()).eq(LikeRecord::getTargetId, id).eq(LikeRecord::getTargetType, TargetType.ARTICLE.getCode()))) {
            throw new BusinessException(CodeEnum.REPEAT_OPERATION);
        }
        // 按理说逻辑上不存在重复点赞（因为上面处理了），只需要save就可以了，但是为了以防万一，这里还是用了saveOrUpdate
        likeRecordService.saveOrUpdate(likeRecord);

        return Result.success();
    }

    @PutMapping("/{id}/unlike")
    @Operation(summary = "文章取消点赞", description = "需要登录")
    public Result<?> unlike(@PathVariable @Parameter(description = "文章ID") Long id) {
        long targetId = id;
        LikeRecord likeRecord = likeRecordService.getOne(
                new LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getUserId, BaseContext.getCurrentUserId())
                        .eq(LikeRecord::getTargetId, targetId)
                        .eq(LikeRecord::getTargetType, TargetType.ARTICLE.getCode()));
        if (likeRecord == null) {
            log.error("未找到点赞记录");
            throw new BusinessException(CodeEnum.SYSTEM_ERROR);
        }
        likeRecordService.removeById(likeRecord);

        return Result.success();
    }

    @GetMapping("/{id}/like")
    @Operation(summary = "是否点赞", description = "查询用户对该文章的点赞状态;如果点赞或取消点赞出现异常，也可调用此接口更新状态")
    public Result<Boolean> isLiked(@PathVariable @Parameter(description = "文章ID") Long id) {
        if (BaseContext.getCurrentUserId() == null) {
            // 未登录
            return Result.success(false);
        }

        LikeRecord likeRecord = likeRecordService.getOne(
                new LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getUserId, BaseContext.getCurrentUserId())
                        .eq(LikeRecord::getTargetId, id)
                        .eq(LikeRecord::getTargetType, TargetType.ARTICLE.getCode()));

        return Result.success(likeRecord != null);
    }

}
