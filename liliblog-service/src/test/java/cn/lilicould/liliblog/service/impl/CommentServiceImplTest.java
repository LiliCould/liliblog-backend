package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.constant.StatusConstant;
import cn.lilicould.liliblog.context.BaseContext;
import cn.lilicould.liliblog.domain.security.SecurityUser;
import cn.lilicould.liliblog.entity.Article;
import cn.lilicould.liliblog.entity.Comment;
import cn.lilicould.liliblog.entity.User;
import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.exception.BusinessException;
import cn.lilicould.liliblog.mapper.CommentMapper;
import cn.lilicould.liliblog.mapper.LikeRecordMapper;
import cn.lilicould.liliblog.mapper.UserMapper;
import cn.lilicould.liliblog.query.CommentQuery;
import cn.lilicould.liliblog.request.CommentCreateRequest;
import cn.lilicould.liliblog.response.CommentVO;
import cn.lilicould.liliblog.response.PageInfo;
import cn.lilicould.liliblog.service.ArticleService;
import cn.lilicould.liliblog.util.IpUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CommentServiceImpl测试类
 *
 * @author lilicould
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommentServiceImplTest {

    @Mock
    private LikeRecordMapper likeRecordMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private ArticleService articleService;
    @Mock
    private IpUtil ipUtil;
    @Mock
    private HttpServletRequest request;

    private CommentServiceImpl commentService;

    @BeforeAll
    static void initTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, Article.class);
        TableInfoHelper.initTableInfo(assistant, Comment.class);
        TableInfoHelper.initTableInfo(assistant, cn.lilicould.liliblog.entity.LikeRecord.class);
        TableInfoHelper.initTableInfo(assistant, User.class);
    }

    @BeforeEach
    void setUp() {
        commentService = new CommentServiceImpl(likeRecordMapper, userMapper, commentMapper, articleService, ipUtil);
        ReflectionTestUtils.setField(commentService, "baseMapper", commentMapper);
    }

    private void loginAs(Long userId, Integer role) {
        User user = User.builder().id(userId).username("u" + userId).role(role).status(1).build();
        SecurityUser securityUser = new SecurityUser(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Comment buildComment(Long id, Long createBy) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setContent("不错的文章");
        comment.setArticleId(1L);
        comment.setParentId(0L);
        comment.setRootId(id);
        comment.setStatus(StatusConstant.COMMENT_PUBLISHED);
        comment.setIpAddress("127.0.0.1");
        comment.setCreateBy(createBy);
        return comment;
    }

    private CommentQuery buildQuery(Long id) {
        CommentQuery query = new CommentQuery();
        query.setCurrent(1L);
        query.setSize(10L);
        query.setId(id);
        return query;
    }

    private void stubListDependencies() {
        when(commentMapper.selectCount(any(Wrapper.class))).thenReturn(2L);
        when(likeRecordMapper.selectCount(any(Wrapper.class))).thenReturn(3L);
        when(userMapper.selectById(1L)).thenReturn(buildUser(1L));
        when(userMapper.selectById(2L)).thenReturn(buildUser(2L));
    }

    private User buildUser(Long id) {
        return User.builder().id(id).username("user" + id).nickname("昵称" + id).email("u" + id + "@t.com").role(1).status(1).build();
    }

    @Test
    void getCommentListWithEmptyResultReturnsEmptyPageInfo() {
        loginAs(2L, 1);
        when(commentMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(new Page<>(1, 10, 0));

        PageInfo<CommentVO> result = commentService.getCommentList(buildQuery(1L));

        assertEquals(0L, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    void getCommentListSuccessForNormalUser() {
        loginAs(2L, 1);
        Comment comment = buildComment(1L, 1L);
        Page<Comment> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(comment));
        when(commentMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);
        stubListDependencies();

        PageInfo<CommentVO> result = commentService.getCommentList(buildQuery(1L));

        assertEquals(1, result.getRecords().size());
        CommentVO vo = result.getRecords().get(0);
        assertEquals("不错的文章", vo.getContent());
        assertEquals(2, vo.getChildCount());
        assertEquals(3, vo.getLikeCount());
        assertEquals("user1", vo.getCreator().getUsername());
    }

    @Test
    void getCommentListSuccessForAdmin() {
        loginAs(100L, 0);
        Comment comment = buildComment(1L, 1L);
        Page<Comment> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(comment));
        when(commentMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);
        stubListDependencies();

        PageInfo<CommentVO> result = commentService.getCommentList(buildQuery(1L));

        assertEquals(1, result.getRecords().size());
    }

    @Test
    void getChildCommentListSuccess() {
        loginAs(2L, 1);
        Comment child = buildComment(5L, 2L);
        child.setParentId(1L);
        child.setRootId(1L);
        Page<Comment> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(child));
        when(commentMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);
        stubListDependencies();

        PageInfo<CommentVO> result = commentService.getChildCommentList(buildQuery(1L));

        assertEquals(1, result.getRecords().size());
        CommentVO vo = result.getRecords().get(0);
        assertEquals(0, vo.getChildCount());
        assertEquals(1L, vo.getRootId());
        assertEquals("user2", vo.getCreator().getUsername());
    }

    @Test
    void deleteAllWhenCommentNotExistThrowsCommentNotFound() {
        loginAs(100L, 0);
        when(commentMapper.selectById(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> commentService.deleteAll(1L));
        assertEquals(CodeEnum.COMMENT_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void deleteAllWithoutPermissionThrowsNoPermission() {
        loginAs(2L, 1);
        when(commentMapper.selectById(1L)).thenReturn(buildComment(1L, 1L));

        BusinessException ex = assertThrows(BusinessException.class, () -> commentService.deleteAll(1L));
        assertEquals(CodeEnum.NO_PERMISSION.getCode(), ex.getCode());
    }

    @Test
    void deleteAllSuccessForAdmin() {
        loginAs(100L, 0);
        when(commentMapper.selectById(1L)).thenReturn(buildComment(1L, 1L));

        commentService.deleteAll(1L);

        verify(commentMapper).delete(any(Wrapper.class));
    }

    @Test
    void deleteAllSuccessForCreator() {
        loginAs(1L, 1);
        when(commentMapper.selectById(1L)).thenReturn(buildComment(1L, 1L));

        commentService.deleteAll(1L);

        verify(commentMapper).delete(any(Wrapper.class));
    }

    @Test
    void createCommentWhenArticleNotExistThrowsArticleNotFound() {
        loginAs(1L, 1);
        when(articleService.exists(any(Wrapper.class))).thenReturn(false);
        CommentCreateRequest createRequest = new CommentCreateRequest();
        createRequest.setArticleId(99L);
        createRequest.setContent("评论内容");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> commentService.createComment(createRequest, request));
        assertEquals(CodeEnum.ARTICLE_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void createChildCommentWithMismatchedRootThrowsParamError() {
        loginAs(1L, 1);
        when(articleService.exists(any(Wrapper.class))).thenReturn(true);
        CommentCreateRequest createRequest = new CommentCreateRequest();
        createRequest.setArticleId(1L);
        createRequest.setContent("回复内容");
        createRequest.setParentId(5L);
        createRequest.setRootId(9L);
        Comment parent = buildComment(5L, 2L);
        parent.setRootId(3L);
        when(commentMapper.selectOne(any(Wrapper.class))).thenReturn(parent);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> commentService.createComment(createRequest, request));
        assertEquals(CodeEnum.COMMON_PARAM_ERROR.getCode(), ex.getCode());
    }

    @Test
    void createRootCommentSuccessSetsRootId() {
        loginAs(1L, 1);
        when(articleService.exists(any(Wrapper.class))).thenReturn(true);
        when(ipUtil.getIpAddress(request)).thenReturn("127.0.0.1");
        when(ipUtil.getUserAgent(request)).thenReturn("Mozilla/5.0");
        doAnswer(inv -> {
            Comment comment = inv.getArgument(0);
            comment.setId(100L);
            return 1;
        }).when(commentMapper).insert(any(Comment.class));

        CommentCreateRequest createRequest = new CommentCreateRequest();
        createRequest.setArticleId(1L);
        createRequest.setContent("一级评论");
        commentService.createComment(createRequest, request);

        org.mockito.ArgumentCaptor<Comment> captor = org.mockito.ArgumentCaptor.forClass(Comment.class);
        verify(commentMapper).updateById(captor.capture());
        Comment updated = captor.getValue();
        assertEquals(100L, updated.getRootId());
        assertEquals(0L, updated.getParentId());
        assertEquals(StatusConstant.COMMENT_PENDING, updated.getStatus());
        assertEquals("127.0.0.1", updated.getIpAddress());
    }

    @Test
    void createChildCommentSuccess() {
        loginAs(1L, 1);
        when(articleService.exists(any(Wrapper.class))).thenReturn(true);
        when(ipUtil.getIpAddress(request)).thenReturn("127.0.0.1");
        when(ipUtil.getUserAgent(request)).thenReturn("Mozilla/5.0");
        when(commentMapper.selectOne(any(Wrapper.class))).thenReturn(buildComment(5L, 2L));
        when(commentMapper.insert(any(Comment.class))).thenReturn(1);

        CommentCreateRequest createRequest = new CommentCreateRequest();
        createRequest.setArticleId(1L);
        createRequest.setContent("二级评论");
        createRequest.setParentId(5L);
        createRequest.setRootId(5L);
        commentService.createComment(createRequest, request);

        org.mockito.ArgumentCaptor<Comment> captor = org.mockito.ArgumentCaptor.forClass(Comment.class);
        verify(commentMapper).insert(captor.capture());
        assertEquals(5L, captor.getValue().getParentId());
    }

    @Test
    void getAllCommentListSuccess() {
        loginAs(100L, 0);
        Comment c1 = buildComment(1L, 1L);
        Comment c2 = buildComment(2L, 2L);
        Page<Comment> page = new Page<>(1, 10, 2);
        page.setRecords(List.of(c1, c2));
        when(commentMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);
        stubListDependencies();

        CommentQuery query = buildQuery(null);
        query.setArticleId(1L);
        query.setStatus(StatusConstant.COMMENT_PUBLISHED);
        PageInfo<CommentVO> result = commentService.getAllCommentList(query);

        assertEquals(2L, result.getTotal());
        assertEquals(2, result.getRecords().size());
        assertEquals(2, result.getRecords().get(0).getChildCount());
    }

    @Test
    void auditCommentWhenNotExistThrowsCommentNotFound() {
        loginAs(100L, 0);
        when(commentMapper.selectById(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> commentService.auditComment(1L, StatusConstant.COMMENT_PUBLISHED));
        assertEquals(CodeEnum.COMMENT_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void auditCommentPublishedUpdatesStatus() {
        loginAs(100L, 0);
        Comment comment = buildComment(1L, 1L);
        comment.setStatus(StatusConstant.COMMENT_PENDING);
        when(commentMapper.selectById(1L)).thenReturn(comment);

        commentService.auditComment(1L, StatusConstant.COMMENT_PUBLISHED);

        verify(commentMapper).updateById(comment);
        assertEquals(StatusConstant.COMMENT_PUBLISHED, comment.getStatus());
    }

    @Test
    void auditCommentRejectedDeletesChildren() {
        loginAs(100L, 0);
        Comment comment = buildComment(1L, 1L);
        comment.setStatus(StatusConstant.COMMENT_PENDING);
        when(commentMapper.selectById(1L)).thenReturn(comment);

        commentService.auditComment(1L, StatusConstant.COMMENT_PENDING);

        verify(commentMapper).delete(any(Wrapper.class));
    }
}