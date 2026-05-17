package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.common.constant.OrderConstant;
import cn.lilicould.liliblog.common.constant.StatusConstant;
import cn.lilicould.liliblog.common.context.BaseContext;
import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.enums.TargetType;
import cn.lilicould.liliblog.common.exception.BusinessException;
import cn.lilicould.liliblog.common.util.IpUtil;
import cn.lilicould.liliblog.mapper.CommentMapper;
import cn.lilicould.liliblog.mapper.LikeRecordMapper;
import cn.lilicould.liliblog.mapper.UserMapper;
import cn.lilicould.liliblog.pojo.dto.query.CommentQuery;
import cn.lilicould.liliblog.pojo.dto.request.CommentCreateRequest;
import cn.lilicould.liliblog.pojo.dto.response.CommentVO;
import cn.lilicould.liliblog.pojo.dto.response.PageInfo;
import cn.lilicould.liliblog.pojo.dto.response.UserInfo;
import cn.lilicould.liliblog.pojo.entity.Article;
import cn.lilicould.liliblog.pojo.entity.Comment;
import cn.lilicould.liliblog.pojo.entity.LikeRecord;
import cn.lilicould.liliblog.pojo.entity.User;
import cn.lilicould.liliblog.service.ArticleService;
import cn.lilicould.liliblog.service.CommentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
* @author Lili_Could
* @description 针对表【comment(评论表)】的数据库操作Service实现
* @createDate 2026-05-08 16:58:41
*/
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService{


    private final LikeRecordMapper likeRecordMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final ArticleService articleService;
    private final IpUtil ipUtil;

    public CommentServiceImpl(LikeRecordMapper likeRecordMapper, UserMapper userMapper, CommentMapper commentMapper, ArticleService articleService, IpUtil ipUtil) {
        this.likeRecordMapper = likeRecordMapper;
        this.userMapper = userMapper;
        this.commentMapper = commentMapper;
        this.articleService = articleService;
        this.ipUtil = ipUtil;
    }

    /**
     * 获取评论列表（level为0的）
     * @param commentQuery 查询参数
     * @return 评论列表
     */
    @Override
    public PageInfo<CommentVO> getCommentList(CommentQuery commentQuery) {
        // 初始化分页参数
        Page<Comment> page = new Page<>(commentQuery.getCurrent(), commentQuery.getSize());
        page.setOrders(List.of(
                OrderItem.asc(OrderConstant.ID),
                OrderItem.desc(OrderConstant.CREATE_TIME)
        ));

        // 构造查询条件
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(Comment::getArticleId, commentQuery.getId())
                .eq(Comment::getParentId, 0);
        if (!BaseContext.isAdmin()) {
            // 非管理员只能查询已发布的评论或自己的评论
            queryWrapper.and(wrapper -> wrapper
                    .eq(Comment::getStatus, StatusConstant.ENABLED)
                    .or()
                    .eq(BaseContext.getCurrentUserId() != null,Comment::getCreateBy, BaseContext.getCurrentUserId())
            );
        }
        // 查询
        Page<Comment> commentPage = this.page(page, queryWrapper);

        // 构建返回结果
        List<CommentVO> records = commentPage.getRecords().stream().map(comment -> CommentVO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .articleId(comment.getArticleId())
                .childCount(getChildCount(comment.getId())) // 获取子评论数
                .creator(buildUserInfo(comment.getCreateBy())) // 构建发布者信息
                .likeCount(getLikeCount(comment.getId()))
                .parentId(comment.getParentId())
                .ipAddress(comment.getIpAddress())
                .createTime(comment.getCreateTime())
                .build()
        ).toList();

        // 空结果
        if (records.isEmpty()) {
            return PageInfo.empty(commentQuery.getCurrent(), commentQuery.getSize());
        }

        // 封装返回
        Page<CommentVO> voPage = Page.of(commentQuery.getCurrent(), commentQuery.getSize(), commentPage.getTotal());
        voPage.setRecords(records);
        return PageInfo.of(voPage);
    }

    /**
     * 获取二级评论列表
     * @param commentQuery 评论查询参数
     * @return 二级评论列表
     */
    @Override
    public PageInfo<CommentVO> getChildCommentList(CommentQuery commentQuery) {

        // 根评论ID
        Long rootId = commentQuery.getId();

        // 初始化分页参数
        Page<Comment> page = new Page<>(commentQuery.getCurrent(), commentQuery.getSize());
        page.setOrders(List.of(
                OrderItem.asc(OrderConstant.ID),
                OrderItem.desc(OrderConstant.CREATE_TIME)
        ));

        // 构造查询条件
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getRootId, commentQuery.getId());
        if (!BaseContext.isAdmin()) {
            // 非管理员只能查询已发布的评论或自己的评论
            queryWrapper.and(wrapper -> wrapper
                    .eq(Comment::getStatus, StatusConstant.ENABLED)
                    .or()
                    .eq(BaseContext.getCurrentUserId() != null,Comment::getCreateBy, BaseContext.getCurrentUserId())
            );
        }
        // 查询
        Page<Comment> commentPage = this.page(page, queryWrapper);

        // 构建返回结果
        List<CommentVO> records = commentPage.getRecords().stream().map(comment -> CommentVO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .articleId(comment.getArticleId())
                .childCount(0) // 获取子评论数(二级评论虽然会有回复,但是因为系统只做二级评论，所以子评论数量为0)
                .creator(buildUserInfo(comment.getCreateBy())) // 构建发布者信息
                .likeCount(getLikeCount(comment.getId()))
                .parentId(comment.getParentId())
                .ipAddress(comment.getIpAddress())
                .createTime(comment.getCreateTime())
                .build()
        ).toList();

        // 封装返回
        Page<CommentVO> voPage = Page.of(commentQuery.getCurrent(), commentQuery.getSize(), commentPage.getTotal());

        // 空结果
        if (records.isEmpty()) {
            return PageInfo.empty(commentQuery.getCurrent(), commentQuery.getSize());
        }

        voPage.setRecords(records);
        return PageInfo.of(voPage);
    }

    /**
     * 删除评论及其子评论
     * @param id 评论id
     */
    @Override
    public void deleteAll(Long id) {

        // 校验权限
        if (!BaseContext.isAdmin()) {
            // 不是管理员，必须是发布者
            if (!Objects.equals(id, BaseContext.getCurrentUserId())) {
                throw new BusinessException(CodeEnum.NO_PERMISSION);
            }
        }

        // 删除
        commentMapper.delete(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getId, id)
                .or()
                .eq(Comment::getRootId, id)
        );
    }

    /**
     * 创建评论
     * @param commentCreateRequest 创建参数
     * @param request 请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void createComment(CommentCreateRequest commentCreateRequest, HttpServletRequest request) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentCreateRequest, comment);

        // 判断文章是否存在
        if (!articleService.exists(new LambdaQueryWrapper<Article>()
                .eq(Article::getId, comment.getArticleId())
                .eq(Article::getStatus, StatusConstant.ARTICLE_PUBLISHED)
        )) {
            throw new BusinessException(CodeEnum.ARTICLE_NOT_FOUND);
        }

        // 设置默认值
        comment.setStatus(StatusConstant.COMMENT_PENDING);
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }

        if (comment.getParentId() != null && comment.getRootId() != null) {
            // 如果父评论和根评论都指定了，应该判断是不是非法评论
            // 比如父评论的根评论ID和当前评论的根评论ID不一致
            Long parentRootId = commentMapper.selectOne(new LambdaQueryWrapper<Comment>()
                    .select(Comment::getRootId)
                    .eq(Comment::getId, comment.getParentId())
            ).getRootId();
            if (!Objects.equals(parentRootId, comment.getRootId())) {
                throw new BusinessException(CodeEnum.COMMON_PARAM_ERROR);
            }
        }

        // 获取ip和代理
        comment.setIpAddress(ipUtil.getIpAddress(request));
        comment.setUserAgent(ipUtil.getUserAgent(request));

        // 创建评论
        this.save(comment);

        // 如果是一级评论，则设置根评论ID为自身ID
        if (comment.getParentId() == 0) {
            comment.setRootId(comment.getId());
            this.updateById(comment);
        }
    }


    /**
     * 计算点赞数
     * @param commentId 评论列表
     * @return 计算后的评论列表
     */
    private int getLikeCount(Long commentId) {
        LambdaQueryWrapper<LikeRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LikeRecord::getTargetId, commentId)
                .eq(LikeRecord::getTargetType, TargetType.COMMENT.getCode());
        return likeRecordMapper.selectCount(queryWrapper).intValue();
    }

    /**
     * 构建发布者信息
     * @param userId 发布者ID
     * @return 发布者信息
     */
    private UserInfo buildUserInfo(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userMapper.selectById(userId);
        return user != null ? UserInfo.from(user) : null;
    }

    /**
     * 获取子评论数
     * @param commentId 评论ID
     * @return 子评论数
     */
    private int getChildCount(Long commentId) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getParentId, commentId);
        if (!BaseContext.isAdmin()) {
            queryWrapper.and(wrapper -> wrapper
                    .eq(Comment::getStatus, StatusConstant.ENABLED)
                    .or()
                    .eq(BaseContext.getCurrentUserId() != null,Comment::getCreateBy, BaseContext.getCurrentUserId())
            );
        }
        return commentMapper.selectCount(queryWrapper).intValue();
    }
}




