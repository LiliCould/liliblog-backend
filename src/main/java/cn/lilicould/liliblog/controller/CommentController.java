package cn.lilicould.liliblog.controller;

import cn.lilicould.liliblog.common.constant.StatusConstant;
import cn.lilicould.liliblog.common.context.BaseContext;
import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.enums.TargetType;
import cn.lilicould.liliblog.common.exception.BusinessException;
import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.common.util.IpUtil;
import cn.lilicould.liliblog.pojo.dto.query.CommentQuery;
import cn.lilicould.liliblog.pojo.dto.request.CommentCreateRequest;
import cn.lilicould.liliblog.pojo.dto.response.CommentVO;
import cn.lilicould.liliblog.pojo.dto.response.PageInfo;
import cn.lilicould.liliblog.pojo.entity.Comment;
import cn.lilicould.liliblog.pojo.entity.LikeRecord;
import cn.lilicould.liliblog.service.ArticleService;
import cn.lilicould.liliblog.service.CommentService;
import cn.lilicould.liliblog.service.LikeRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comment")
@Slf4j
@Tag(name = "评论接口")
public class CommentController {

    private final CommentService commentService;
    private final IpUtil ipUtil;
    private final ArticleService articleService;
    private final LikeRecordService likeRecordService;

    public CommentController(CommentService commentService, IpUtil ipUtil, ArticleService articleService, LikeRecordService likeRecordService) {
        this.commentService = commentService;
        this.ipUtil = ipUtil;
        this.articleService = articleService;
        this.likeRecordService = likeRecordService;
    }

    @GetMapping
    @Operation(summary = "分页获取评论列表(一级评论)")
    public Result<PageInfo<CommentVO>> list(@ParameterObject @Validated CommentQuery commentQuery) {
        // 设置分页默认值
        if (commentQuery.getCurrent() == null) {
            commentQuery.setCurrent(1L);
        }
        if (commentQuery.getSize() == null) {
            commentQuery.setSize(10L);
        }
        PageInfo<CommentVO> pageInfo = commentService.getCommentList(commentQuery);

        return Result.success(pageInfo);
    }

    @GetMapping("/child")
    @Operation(summary = "分页获取评论列表(二级评论)")
    public Result<PageInfo<CommentVO>> childList(@ParameterObject @Validated CommentQuery commentQuery) {

        // 设置分页默认值
        if (commentQuery.getCurrent() == null) {
            commentQuery.setCurrent(1L);
        }
        if (commentQuery.getSize() == null) {
            commentQuery.setSize(10L);
        }

        // 获取对应评论的二级评论列表
        PageInfo<CommentVO> pageInfo = commentService.getChildCommentList(commentQuery);

        return Result.success(pageInfo);
    }

    @PostMapping
    @Operation(summary = "发布评论")
    public Result<Void> create(
            @Validated @RequestBody CommentCreateRequest commentCreateRequest,
            HttpServletRequest request) {

        commentService.createComment(commentCreateRequest,request);

        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评论", description = "删除所评论及其子评论(如果有)")
    public Result<Void> delete(@Parameter(description = "评论ID") @PathVariable Long id) {

        // 删除评论及其子评论
        commentService.deleteAll(id);

        return Result.success();
    }

    @PutMapping("/{id}/like")
    @Operation(summary = "评论点赞", description = "需要登录")
    public Result<?> like(@PathVariable @Parameter(description = "评论ID") Long id) {
        LikeRecord likeRecord = new LikeRecord();
        likeRecord.setUserId(BaseContext.getCurrentUserId());
        likeRecord.setTargetId(id);
        likeRecord.setTargetType(TargetType.COMMENT.getCode());

        // 检查评论是否存在且状态符合
        if (!commentService.exists(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getId, id)
                .eq(Comment::getStatus, StatusConstant.COMMENT_PUBLISHED))) {
            throw new BusinessException(CodeEnum.COMMENT_NOT_FOUND);
        }

        // 已经点赞
        if (likeRecordService.exists(new LambdaQueryWrapper<LikeRecord>()
                .eq(LikeRecord::getUserId, BaseContext.getCurrentUserId())
                .eq(LikeRecord::getTargetId, id)
                .eq(LikeRecord::getTargetType, TargetType.COMMENT.getCode()))) {
            throw new BusinessException(CodeEnum.REPEAT_OPERATION);
        }
        // 按理说逻辑上不存在重复点赞（因为上面处理了），只需要save就可以了，但是为了以防万一，这里还是用了saveOrUpdate
        likeRecordService.saveOrUpdate(likeRecord);

        return Result.success();
    }

    @PutMapping("/{id}/unlike")
    @Operation(summary = "评论取消点赞", description = "需要登录")
    public Result<?> unlike(@PathVariable @Parameter(description = "评论ID") Long id) {
        long targetId = id;
        LikeRecord likeRecord = likeRecordService.getOne(
                new LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getUserId, BaseContext.getCurrentUserId())
                        .eq(LikeRecord::getTargetId, targetId)
                        .eq(LikeRecord::getTargetType, TargetType.COMMENT.getCode()));
        if (likeRecord == null) {
            log.error("未找到点赞记录");
            throw new BusinessException(CodeEnum.SYSTEM_ERROR);
        }
        likeRecordService.removeById(likeRecord);

        return Result.success();
    }

    @GetMapping("/{id}/like")
    @Operation(summary = "是否点赞", description = "查询用户对该评论的点赞状态;如果点赞或取消点赞出现异常，也可调用此接口更新状态")
    public Result<Boolean> isLiked(@PathVariable @Parameter(description = "评论ID") Long id) {
        if (BaseContext.getCurrentUserId() == null) {
            // 未登录
            return Result.success(false);
        }

        LikeRecord likeRecord = likeRecordService.getOne(
                new LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getUserId, BaseContext.getCurrentUserId())
                        .eq(LikeRecord::getTargetId, id)
                        .eq(LikeRecord::getTargetType, TargetType.COMMENT.getCode()));

        return Result.success(likeRecord != null);
    }

}
