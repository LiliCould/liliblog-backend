package com.lilicould.blog.controller;

import com.lilicould.blog.dto.CommentCreateDTO;
import com.lilicould.blog.entity.Comment;
import com.lilicould.blog.vo.ResultVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import com.lilicould.blog.util.RequestUtil;

import java.util.List;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @GetMapping
    public ResultVO<List<Comment>> list(HttpServletRequest request) {
        return ResultVO.success("213");
    }

    @PostMapping
    public ResultVO<Void> add(HttpServletRequest request, @RequestBody CommentCreateDTO comment) {
        String ip = RequestUtil.getClientIP(request);
        String userAgent = RequestUtil.getShortUserAgent(request);
        comment.setIpAddress(ip);
        comment.setUserAgent(userAgent);

        // TODO: 添加评论

        return ResultVO.success();
    }
}
