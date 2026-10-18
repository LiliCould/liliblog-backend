package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.constant.OrderConstant;
import cn.lilicould.liliblog.constant.StatusConstant;
import cn.lilicould.liliblog.context.BaseContext;
import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.enums.TargetType;
import cn.lilicould.liliblog.exception.BusinessException;
import cn.lilicould.liliblog.util.Ip2RegionUtil;
import cn.lilicould.liliblog.util.IpUtil;
import cn.lilicould.liliblog.mapper.CommentMapper;
import cn.lilicould.liliblog.mapper.LikeRecordMapper;
import cn.lilicould.liliblog.mapper.UserMapper;
import cn.lilicould.liliblog.query.CommentQuery;
import cn.lilicould.liliblog.request.CommentCreateRequest;
import cn.lilicould.liliblog.response.CommentVO;
import cn.lilicould.liliblog.response.PageInfo;
import cn.lilicould.liliblog.response.UserInfo;
import cn.lilicould.liliblog.entity.Article;
import cn.lilicould.liliblog.entity.Comment;
import cn.lilicould.liliblog.entity.LikeRecord;
import cn.lilicould.liliblog.entity.User;
import cn.lilicould.liliblog.service.ArticleService;
import cn.lilicould.liliblog.service.CommentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
     * 获取评论列表
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

        // 构建返回结果：用户、点赞数、子评论数全部批量加载，避免 N+1
        List<Comment> comments = commentPage.getRecords();
        Map<Long, UserInfo> userMap = loadUserInfoMap(comments);
        Map<Long, Integer> likeCountMap = loadLikeCountMap(ids(comments));
        Map<Long, Integer> childCountMap = loadChildCountMap(ids(comments));
        List<CommentVO> records = comments.stream()
                .map(comment -> toCommentVO(comment, userMap, likeCountMap,
                        childCountMap.getOrDefault(comment.getId(), 0), comment.getRootId()))
                .toList();

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
        // rootId且排除根评论
        queryWrapper.eq(Comment::getRootId, commentQuery.getId())
                .ne(Comment::getId, rootId);

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

        // 构建返回结果：用户、点赞数批量加载，避免 N+1
        List<Comment> comments = commentPage.getRecords();
        Map<Long, UserInfo> userMap = loadUserInfoMap(comments);
        Map<Long, Integer> likeCountMap = loadLikeCountMap(ids(comments));
        List<CommentVO> records = comments.stream()
                // 二级评论虽然会有回复,但是因为系统只做二级评论，所以子评论数量为0
                .map(comment -> toCommentVO(comment, userMap, likeCountMap, 0, rootId))
                .toList();

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

        Comment comment = this.getById(id);

        // 评论不存在
        if (comment == null) {
            throw new BusinessException(CodeEnum.COMMENT_NOT_FOUND);
        }

        // 校验权限
        if (!BaseContext.isAdmin()) {
            // 不是管理员，必须是发布者
            if (!Objects.equals(comment.getCreateBy(), BaseContext.getCurrentUserId())) {
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
            comment.setParentId(0L); // 如果父评论ID为空，则设置为0，表示是一级评论
        }

        if (comment.getParentId() != null && comment.getRootId() != null) {
            // 如果父评论和根评论都指定了，应该判断是不是非法评论
            // 比如父评论的根评论ID和当前评论的根评论ID不一致
            if (comment.getParentId() != 0) { // parentId为0的话，代表一级评论，后续会设置根评论为插入后的 ID
                Long parentRootId = commentMapper.selectOne(new LambdaQueryWrapper<Comment>()
                        .select(Comment::getRootId)
                        .eq(Comment::getId, comment.getParentId())
                ).getRootId();

                if (!Objects.equals(parentRootId, comment.getRootId())) {
                    throw new BusinessException(CodeEnum.COMMON_PARAM_ERROR);
                }
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
     * 获取所有评论列表
     * @param commentQuery 评论查询参数
     * @return 所有评论列表
     */
    @Override
    public PageInfo<CommentVO> getAllCommentList(CommentQuery commentQuery) {
        // 初始化分页信息
        Page<Comment> page = new Page<>(commentQuery.getCurrent(), commentQuery.getSize());
        // 添加排序
        page.setOrders(List.of(
                OrderItem.desc(OrderConstant.CREATE_TIME),
                OrderItem.asc(OrderConstant.ID)
        ));

        // 创建查询条件
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(commentQuery.getId() != null, Comment::getId, commentQuery.getId())
                .eq(commentQuery.getArticleId() != null, Comment::getArticleId, commentQuery.getArticleId())
                .eq(commentQuery.getParentId() != null, Comment::getParentId, commentQuery.getParentId())
                .eq(commentQuery.getRootId() != null, Comment::getRootId, commentQuery.getRootId())
                .eq(commentQuery.getStatus() != null, Comment::getStatus, commentQuery.getStatus())
                .like(commentQuery.getContent() != null, Comment::getContent, commentQuery.getContent())
                .ge(commentQuery.getStartTime() != null, Comment::getCreateTime, commentQuery.getStartTime())
                .le(commentQuery.getEndTime() != null, Comment::getCreateTime, commentQuery.getEndTime());

        // 查询
        Page<Comment> commentPage = this.page(page, queryWrapper);
        // 构建返回结果：用户、点赞数、子评论数全部批量加载，避免 N+1
        List<Comment> comments = commentPage.getRecords();
        Map<Long, UserInfo> userMap = loadUserInfoMap(comments);
        Map<Long, Integer> likeCountMap = loadLikeCountMap(ids(comments));
        Map<Long, Integer> childCountMap = loadChildCountMap(ids(comments));
        List<CommentVO> records = comments.stream()
                .map(comment -> toCommentVO(comment, userMap, likeCountMap,
                        childCountMap.getOrDefault(comment.getId(), 0), null))
                .toList();
        Page<CommentVO> voPage = Page.of(commentQuery.getCurrent(), commentQuery.getSize(), commentPage.getTotal());
        voPage.setRecords(records);

        return PageInfo.of(voPage);
    }

    /**
     * 审核评论
     * @param id 评论ID
     * @param status 审核状态
     */
    @Override
    public void auditComment(Long id, Integer status) {
        Comment comment = this.getById(id);
        if (comment == null) {
            throw new BusinessException(CodeEnum.COMMENT_NOT_FOUND);
        }

        // 如果是审核通过
        if (StatusConstant.COMMENT_PUBLISHED.equals(status)) {
            comment.setStatus(StatusConstant.COMMENT_PUBLISHED);
            this.updateById(comment);
            return;
        }

        // 未审核的评论理论上不会存在子评论，但是为了避免数据不一致，这里删除所有子评论
        this.deleteAll(id);
    }


    private Set<Long> ids(List<Comment> comments) {
        return comments.stream().map(Comment::getId).collect(Collectors.toSet());
    }

    /**
     * 把评论实体转换为VO，关联信息由调用方批量加载后传入
     */
    private CommentVO toCommentVO(Comment comment, Map<Long, UserInfo> userMap,
                                  Map<Long, Integer> likeCountMap, int childCount, Long rootId) {
        return CommentVO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .articleId(comment.getArticleId())
                .status(comment.getStatus())
                .childCount(childCount)
                .creator(comment.getCreateBy() == null ? null : userMap.get(comment.getCreateBy()))
                .likeCount(likeCountMap.getOrDefault(comment.getId(), 0))
                .parentId(comment.getParentId())
                .rootId(rootId)
                .ipAddress(comment.getIpAddress())
                .ipAddressLocation(Ip2RegionUtil.getFormattedLocation(comment.getIpAddress()))
                .createTime(comment.getCreateTime())
                .build();
    }

    /**
     * 批量加载评论发布者，一条 SQL 代替每条评论一次 selectById
     */
    private Map<Long, UserInfo> loadUserInfoMap(List<Comment> comments) {
        Set<Long> userIds = comments.stream()
                .map(Comment::getCreateBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, UserInfo::from));
    }

    /**
     * 批量统计点赞数。like_record 走 idx_target(target_id, target_type)。
     */
    private Map<Long, Integer> loadLikeCountMap(Set<Long> commentIds) {
        if (commentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        QueryWrapper<LikeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("target_id AS target_id", "COUNT(*) AS cnt")
                .eq("target_type", TargetType.COMMENT.getCode())
                .in("target_id", commentIds)
                .groupBy("target_id");
        return toCountMap(likeRecordMapper.selectMaps(queryWrapper));
    }

    /**
     * 批量统计子评论数。comment 走 idx_comment_root(root_id, deleted, id)。
     */
    private Map<Long, Integer> loadChildCountMap(Set<Long> rootIds) {
        if (rootIds.isEmpty()) {
            return Collections.emptyMap();
        }
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("root_id AS target_id", "COUNT(*) AS cnt")
                .in("root_id", rootIds)
                // 排除根评论自身
                .apply("id <> root_id")
                .groupBy("root_id");

        if (!BaseContext.isAdmin()) { // 非管理员，排除未审核的评论
            Long currentUserId = BaseContext.getCurrentUserId();
            queryWrapper.and(wrapper -> {
                wrapper.eq("status", StatusConstant.COMMENT_PUBLISHED);
                if (currentUserId != null) {
                    wrapper.or().eq("create_by", currentUserId);
                }
            });
        }
        return toCountMap(commentMapper.selectMaps(queryWrapper));
    }

    private Map<Long, Integer> toCountMap(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Integer> result = new HashMap<>(rows.size());
        for (Map<String, Object> row : rows) {
            Object targetId = row.get("target_id");
            Object count = row.get("cnt");
            if (targetId instanceof Number id && count instanceof Number value) {
                result.put(id.longValue(), value.intValue());
            }
        }
        return result;
    }
}




