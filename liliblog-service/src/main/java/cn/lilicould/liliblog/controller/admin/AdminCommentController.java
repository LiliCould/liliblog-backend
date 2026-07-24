package cn.lilicould.liliblog.controller.admin;

import cn.lilicould.liliblog.result.Result;
import cn.lilicould.liliblog.util.PageUtil;
import cn.lilicould.liliblog.query.CommentQuery;
import cn.lilicould.liliblog.response.CommentVO;
import cn.lilicould.liliblog.response.PageInfo;
import cn.lilicould.liliblog.entity.Comment;
import cn.lilicould.liliblog.service.CommentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
@RequestMapping("/api/admin/comment")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "评论管理接口")
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping
    @Operation(summary = "获取评论列表", description = "管理员后台，获取评论列表")
    public Result<PageInfo<CommentVO>> list(@Validated @ParameterObject CommentQuery commentQuery) {
        // 设置分页默认值
        PageUtil.setDefault(commentQuery);

        PageInfo<CommentVO> pageInfo = commentService.getAllCommentList(commentQuery);

        return Result.success(pageInfo);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评论", description = "管理员后台，删除评论,前端也可直接使用批量删除的接口")
    public Result<?> delete(@PathVariable @Parameter(description = "评论ID") Long id) {
        // 构造删除条件,同时删除子评论
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(Comment::getId, id).or().eq(Comment::getParentId, id);
        commentService.remove(queryWrapper);
        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "批量删除评论", description = "管理员后台，批量删除评论")
    public Result<?> delete(@RequestBody List<Long> ids) {
        // 构造批量删除条件,同时删除子评论
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .in(Comment::getId, ids)
                .or()
                .in(Comment::getParentId, ids);
        commentService.remove(queryWrapper);
        return Result.success();
    }

    @PutMapping("/{id}/{status}")
    @Operation(summary = "审核评论", description = "管理员后台，审核评论")
    public Result<?> audit(@PathVariable @Parameter(description = "评论ID") Long id,
                                @PathVariable @Parameter(description = "审核结果，1-审核通过（发布），2或其他-审核不通过（直接删除）") Integer status)
    {
        // 评论审核结果不发送邮件提醒
        commentService.auditComment(id, status);
        return Result.success();
    }
}
