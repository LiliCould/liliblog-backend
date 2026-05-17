package cn.lilicould.liliblog.controller;

import cn.lilicould.liliblog.common.result.Result;
import cn.lilicould.liliblog.pojo.dto.query.CommentQuery;
import cn.lilicould.liliblog.pojo.dto.response.CommentVO;
import cn.lilicould.liliblog.pojo.dto.response.PageInfo;
import cn.lilicould.liliblog.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comment")
@Tag(name = "评论接口")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
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


}
