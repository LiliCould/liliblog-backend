package cn.lilicould.liliblog.service;

import cn.lilicould.liliblog.pojo.dto.query.CommentQuery;
import cn.lilicould.liliblog.pojo.dto.request.CommentCreateRequest;
import cn.lilicould.liliblog.pojo.dto.response.CommentVO;
import cn.lilicould.liliblog.pojo.dto.response.PageInfo;
import cn.lilicould.liliblog.pojo.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author Lili_Could
* @description 针对表【comment(评论表)】的数据库操作Service
* @createDate 2026-05-08 16:58:41
*/
public interface CommentService extends IService<Comment> {

    /**
     * 获取评论列表
     * @param commentQuery 查询参数
     * @return 评论列表
     */
    PageInfo<CommentVO> getCommentList(CommentQuery commentQuery);

    /**
     * 获取二级评论列表
     * @param commentQuery 评论查询参数
     * @return 二级评论列表
     */
    PageInfo<CommentVO> getChildCommentList(CommentQuery commentQuery);

    /**
     * 删除所评论及其子评论
     * @param id 评论id
     */
    void deleteAll(Long id);

    /**
     * 发布评论
     * @param commentCreateRequest 创建参数
     * @param request 请求
     */
    void createComment(CommentCreateRequest commentCreateRequest, HttpServletRequest request);
}
