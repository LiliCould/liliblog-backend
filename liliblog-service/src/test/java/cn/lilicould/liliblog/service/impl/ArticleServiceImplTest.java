package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.cache.RedisHelper;
import cn.lilicould.liliblog.config.properties.InfoProperties;
import cn.lilicould.liliblog.constant.StatusConstant;
import cn.lilicould.liliblog.context.BaseContext;
import cn.lilicould.liliblog.domain.security.SecurityUser;
import cn.lilicould.liliblog.entity.*;
import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.exception.BusinessException;
import cn.lilicould.liliblog.mapper.*;
import cn.lilicould.liliblog.query.ArticleQuery;
import cn.lilicould.liliblog.query.ArticleSearchQuery;
import cn.lilicould.liliblog.request.ArticleCreateRequest;
import cn.lilicould.liliblog.request.ArticleUpdateRequest;
import cn.lilicould.liliblog.response.ArticleDetailsVO;
import cn.lilicould.liliblog.response.ArticleVO;
import cn.lilicould.liliblog.response.PageInfo;
import cn.lilicould.liliblog.service.UserService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ArticleServiceImpl测试类
 *
 * @author lilicould
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ArticleServiceImplTest {

    @Mock
    private ArticleMapper articleMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private LikeRecordMapper likeRecordMapper;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private TagMapper tagMapper;
    @Mock
    private ArticleTagMapper articleTagMapper;
    @Mock
    private UserService userService;
    @Mock
    private EmailTemplateService emailTemplateService;
    @Mock
    private RedisHelper redisHelper;

    private final InfoProperties infoProperties = new InfoProperties();

    private ArticleServiceImpl articleService;

    @BeforeAll
    static void initTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, Article.class);
        TableInfoHelper.initTableInfo(assistant, cn.lilicould.liliblog.entity.Comment.class);
        TableInfoHelper.initTableInfo(assistant, cn.lilicould.liliblog.entity.LikeRecord.class);
        TableInfoHelper.initTableInfo(assistant, cn.lilicould.liliblog.entity.Category.class);
        TableInfoHelper.initTableInfo(assistant, cn.lilicould.liliblog.entity.Tag.class);
        TableInfoHelper.initTableInfo(assistant, cn.lilicould.liliblog.entity.ArticleTag.class);
        TableInfoHelper.initTableInfo(assistant, User.class);
    }

    private ArticleServiceImpl buildService() {
        infoProperties.setAdminEmail("admin@lilicould.cn");
        ArticleServiceImpl service = new ArticleServiceImpl(
                articleMapper, userMapper, likeRecordMapper, commentMapper,
                categoryMapper, tagMapper, articleTagMapper, userService,
                emailTemplateService, infoProperties, redisHelper);
        ReflectionTestUtils.setField(service, "baseMapper", articleMapper);
        return service;
    }

    private void loginAs(Long userId, Integer role) {
        User user = User.builder().id(userId).username("user" + userId).role(role).status(1).build();
        SecurityUser securityUser = new SecurityUser(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities()));
    }

    private void loginAsAdmin() {
        loginAs(100L, 0);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Article buildPublishedArticle() {
        Article article = new Article();
        article.setId(1L);
        article.setTitle("测试文章");
        article.setSlug("test-slug");
        article.setSummary("摘要");
        article.setContent("# 标题");
        article.setContentHtml("<h1>标题</h1>");
        article.setStatus(StatusConstant.ARTICLE_PUBLISHED);
        article.setViewCount(10);
        article.setCategoryId(2L);
        article.setCreateBy(1L);
        article.setUpdateBy(1L);
        return article;
    }

    private User buildUser(Long id, String username) {
        return User.builder().id(id).username(username).nickname("昵称" + id).email(username + "@test.com").role(1).status(1).build();
    }

    private void stubSuccessDependencies(Article article) {
        when(userMapper.selectById(1L)).thenReturn(buildUser(1L, "zhangsan"));
        when(userMapper.selectById(2L)).thenReturn(buildUser(2L, "lisi"));
        when(likeRecordMapper.selectCount(any(Wrapper.class))).thenReturn(5L);
        when(commentMapper.selectCount(any(Wrapper.class))).thenReturn(3L);
        Category category = new Category();
        category.setId(2L);
        category.setName("Java");
        category.setSlug("java");
        category.setStatus(StatusConstant.ENABLED);
        when(categoryMapper.selectById(2L)).thenReturn(category);
        Tag tag = new Tag();
        tag.setId(9L);
        tag.setName("随笔");
        when(tagMapper.selectTagsByArticleId(article.getId())).thenReturn(List.of(tag));
    }

    @Test
    void getArticleWhenNotExistThrowsArticleNotFound() {
        articleService = buildService();
        when(articleMapper.selectById(1L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> articleService.getArticle(1L));
        assertEquals(CodeEnum.ARTICLE_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void getArticleWhenNoReadAuthorityThrowsNoPermission() {
        articleService = buildService();
        loginAs(200L, 1);
        Article pending = buildPublishedArticle();
        pending.setStatus(StatusConstant.ARTICLE_PENDING);
        when(articleMapper.selectById(1L)).thenReturn(pending);
        BusinessException ex = assertThrows(BusinessException.class, () -> articleService.getArticle(1L));
        assertEquals(CodeEnum.NO_PERMISSION.getCode(), ex.getCode());
    }

    @Test
    void getArticleSuccessAsAdmin() {
        articleService = buildService();
        loginAsAdmin();
        Article article = buildPublishedArticle();
        when(articleMapper.selectById(1L)).thenReturn(article);
        stubSuccessDependencies(article);

        ArticleDetailsVO vo = articleService.getArticle(1L);

        assertNotNull(vo);
        assertEquals(1L, vo.getId());
        assertEquals("测试文章", vo.getTitle());
        assertEquals(5, vo.getLikeCount());
        assertEquals(3, vo.getCommentCount());
        assertNotNull(vo.getCreator());
        assertEquals("zhangsan", vo.getCreator().getUsername());
        assertNotNull(vo.getCategory());
        assertEquals("Java", vo.getCategory().getName());
        assertEquals(1, vo.getTags().size());
        assertEquals(11, article.getViewCount());
        verify(articleMapper).updateById(article);
    }

    @Test
    void getArticleSuccessForAuthorOfDraft() {
        articleService = buildService();
        loginAs(1L, 1);
        Article draft = buildPublishedArticle();
        draft.setStatus(StatusConstant.ARTICLE_DRAFT);
        when(articleMapper.selectById(1L)).thenReturn(draft);
        stubSuccessDependencies(draft);

        ArticleDetailsVO vo = articleService.getArticle(1L);

        assertNotNull(vo);
        assertEquals(1L, vo.getId());
    }

    @Test
    void getArticleListEmptyReturnsEmptyPageInfo() {
        articleService = buildService();
        loginAsAdmin();
        ArticleQuery query = new ArticleQuery();
        query.setCurrent(1L);
        query.setSize(10L);
        Page<Article> emptyPage = new Page<>(1, 10, 0);
        when(articleMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(emptyPage);

        PageInfo<ArticleVO> result = articleService.getArticleList(query);

        assertEquals(0L, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
        assertEquals(1L, result.getCurrent());
    }

    @Test
    void getArticleListSuccessConvertsToVO() {
        articleService = buildService();
        loginAsAdmin();
        ArticleQuery query = new ArticleQuery();
        query.setCurrent(1L);
        query.setSize(10L);
        query.setTitle("测试");
        Article article = buildPublishedArticle();
        Page<Article> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(article));
        when(articleMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);
        stubSuccessDependencies(article);

        PageInfo<ArticleVO> result = articleService.getArticleList(query);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getRecords().size());
        ArticleVO vo = result.getRecords().get(0);
        assertEquals("测试文章", vo.getTitle());
        assertEquals("zhangsan", vo.getCreator().getUsername());
        assertEquals(5, vo.getLikeCount());
    }

    @Test
    void getArticleListForAnonymousOnlyPublished() {
        articleService = buildService();
        ArticleQuery query = new ArticleQuery();
        query.setCurrent(1L);
        query.setSize(10L);
        Article article = buildPublishedArticle();
        Page<Article> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(article));
        when(articleMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);
        stubSuccessDependencies(article);

        PageInfo<ArticleVO> result = articleService.getArticleList(query);

        assertEquals(1, result.getRecords().size());
    }

    @Test
    void removeWhenNotExistThrowsArticleNotFound() {
        articleService = buildService();
        when(articleMapper.exists(any(Wrapper.class))).thenReturn(false);
        BusinessException ex = assertThrows(BusinessException.class, () -> articleService.remove(1L));
        assertEquals(CodeEnum.ARTICLE_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void removeWhenNoWriteAuthorityThrowsNoPermission() {
        articleService = buildService();
        loginAs(200L, 1);
        when(articleMapper.exists(any(Wrapper.class))).thenReturn(true);
        Article owner = buildPublishedArticle();
        owner.setCreateBy(1L);
        when(articleMapper.selectOne(any(Wrapper.class))).thenReturn(owner);

        BusinessException ex = assertThrows(BusinessException.class, () -> articleService.remove(1L));
        assertEquals(CodeEnum.NO_PERMISSION.getCode(), ex.getCode());
    }

    @Test
    void removeSuccessDeletesRelatedRecords() {
        articleService = buildService();
        loginAsAdmin();
        when(articleMapper.exists(any(Wrapper.class))).thenReturn(true);
        when(articleMapper.deleteById(1L)).thenReturn(1);

        articleService.remove(1L);

        verify(articleMapper).deleteById(1L);
        verify(likeRecordMapper).delete(any(Wrapper.class));
        verify(articleTagMapper).delete(any(Wrapper.class));
        verify(commentMapper).delete(any(Wrapper.class));
    }

    @Test
    void updateWhenIdNullThrowsParamMissing() {
        articleService = buildService();
        BusinessException ex = assertThrows(BusinessException.class,
                () -> articleService.update(null, new ArticleUpdateRequest()));
        assertEquals(CodeEnum.PARAM_MISSING.getCode(), ex.getCode());
    }

    @Test
    void updateWhenNotExistThrowsArticleNotFound() {
        articleService = buildService();
        when(articleMapper.exists(any(Wrapper.class))).thenReturn(false);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> articleService.update(1L, new ArticleUpdateRequest()));
        assertEquals(CodeEnum.ARTICLE_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void updateWhenSlugAlreadyExistsThrowsSlugAlreadyExists() {
        articleService = buildService();
        loginAs(1L, 1);
        ArticleUpdateRequest request = new ArticleUpdateRequest();
        request.setSlug("dup-slug");
        request.setStatus(StatusConstant.ARTICLE_DRAFT);
        request.setContent("内容");
        request.setTags(List.of());
        when(articleMapper.exists(any(Wrapper.class))).thenReturn(true, true);
        Article owner = buildPublishedArticle();
        owner.setCreateBy(1L);
        when(articleMapper.selectOne(any(Wrapper.class))).thenReturn(owner);

        BusinessException ex = assertThrows(BusinessException.class, () -> articleService.update(1L, request));
        assertEquals(CodeEnum.SLUG_ALREADY_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void updateWhenCategoryNotExistThrowsCategoryNotFound() {
        articleService = buildService();
        loginAsAdmin();
        ArticleUpdateRequest request = new ArticleUpdateRequest();
        request.setSlug("new-slug");
        request.setCategoryId(99L);
        request.setStatus(StatusConstant.ARTICLE_PUBLISHED);
        request.setContent("内容");
        request.setTags(List.of());
        when(articleMapper.exists(any(Wrapper.class))).thenReturn(true, false);
        when(categoryMapper.exists(any(Wrapper.class))).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> articleService.update(1L, request));
        assertEquals(CodeEnum.CATEGORY_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void updateWhenTagNotExistThrowsTagNotFound() {
        articleService = buildService();
        loginAs(1L, 1);
        ArticleUpdateRequest request = new ArticleUpdateRequest();
        request.setStatus(StatusConstant.ARTICLE_DRAFT);
        request.setContent("内容");
        request.setTags(List.of(99L));
        when(articleMapper.exists(any(Wrapper.class))).thenReturn(true, false);
        Article owner = buildPublishedArticle();
        owner.setCreateBy(1L);
        when(articleMapper.selectOne(any(Wrapper.class))).thenReturn(owner);
        when(tagMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> articleService.update(1L, request));
        assertEquals(CodeEnum.TAG_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void updateSuccessAsAdminPublishedDirectly() {
        articleService = buildService();
        loginAsAdmin();
        ArticleUpdateRequest request = new ArticleUpdateRequest();
        request.setTitle("新标题");
        request.setSlug("new-slug");
        request.setContent("## 新内容");
        request.setStatus(StatusConstant.ARTICLE_PUBLISHED);
        request.setCategoryId(2L);
        request.setTags(List.of(1L, 2L));
        when(articleMapper.exists(any(Wrapper.class))).thenReturn(true, false);
        when(categoryMapper.exists(any(Wrapper.class))).thenReturn(true);
        when(tagMapper.selectById(1L)).thenReturn(new Tag());
        when(tagMapper.selectById(2L)).thenReturn(new Tag());

        articleService.update(1L, request);

        verify(articleMapper).updateById(any(Article.class));
        verify(articleTagMapper).delete(any(Wrapper.class));
        verify(articleTagMapper).insert(anyList());
        verify(emailTemplateService, never()).sendArticleReviewNotify(anyString(), anyString(), anyString(), anyString());
        ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
        verify(articleMapper).updateById(captor.capture());
        assertEquals(StatusConstant.ARTICLE_PUBLISHED, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getContentHtml());
    }

    @Test
    void updatePendingSendsReviewNotifyEmail() {
        articleService = buildService();
        loginAs(1L, 1);
        ArticleUpdateRequest request = new ArticleUpdateRequest();
        request.setTitle("待审核文章");
        request.setContent("内容");
        request.setStatus(StatusConstant.ARTICLE_PENDING);
        request.setTags(List.of());
        when(articleMapper.exists(any(Wrapper.class))).thenReturn(true, false);
        Article owner = buildPublishedArticle();
        owner.setCreateBy(1L);
        when(articleMapper.selectOne(any(Wrapper.class))).thenReturn(owner);
        when(articleMapper.updateById(any(Article.class))).thenReturn(1);

        articleService.update(1L, request);

        verify(emailTemplateService).sendArticleReviewNotify(
                eq("admin@lilicould.cn"), eq("待审核文章"), eq("user1"), anyString());
    }

    @Test
    void getArticleBySlugWhenNotExistReturnsNull() {
        articleService = buildService();
        when(articleMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        assertNull(articleService.getArticleBySlug("no-exist"));
    }

    @Test
    void getArticleBySlugSuccess() {
        articleService = buildService();
        loginAsAdmin();
        Article article = buildPublishedArticle();
        when(articleMapper.selectOne(any(Wrapper.class))).thenReturn(article);
        when(articleMapper.selectById(1L)).thenReturn(article);
        stubSuccessDependencies(article);

        ArticleDetailsVO vo = articleService.getArticleBySlug("test-slug");

        assertNotNull(vo);
        assertEquals(1L, vo.getId());
    }

    @Test
    void auditArticleWhenNotExistThrowsArticleNotFound() {
        articleService = buildService();
        when(articleMapper.selectById(1L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> articleService.auditArticle(1L, StatusConstant.ARTICLE_PUBLISHED, "ok"));
        assertEquals(CodeEnum.ARTICLE_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void auditArticleWhenAuthorMissingThrowsUserNotFound() {
        articleService = buildService();
        Article article = buildPublishedArticle();
        article.setCreateBy(null);
        when(articleMapper.selectById(1L)).thenReturn(article);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> articleService.auditArticle(1L, StatusConstant.ARTICLE_PUBLISHED, "ok"));
        assertEquals(CodeEnum.USER_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void auditArticleApprovedSendsPassEmail() {
        articleService = buildService();
        Article article = buildPublishedArticle();
        article.setCreateBy(1L);
        when(articleMapper.selectById(1L)).thenReturn(article);
        when(userService.getById(1L)).thenReturn(buildUser(1L, "zhangsan"));
        when(articleMapper.updateById(article)).thenReturn(1);

        articleService.auditArticle(1L, StatusConstant.ARTICLE_PUBLISHED, "审核通过");

        verify(emailTemplateService).sendArticleReviewResult("zhangsan@test.com", "测试文章", true, "审核通过");
        assertEquals(StatusConstant.ARTICLE_PUBLISHED, article.getStatus());
    }

    @Test
    void auditArticleRejectedSendsFailEmail() {
        articleService = buildService();
        Article article = buildPublishedArticle();
        article.setCreateBy(1L);
        when(articleMapper.selectById(1L)).thenReturn(article);
        when(userService.getById(1L)).thenReturn(buildUser(1L, "zhangsan"));

        articleService.auditArticle(1L, StatusConstant.ARTICLE_DRAFT, "内容不合格");

        verify(emailTemplateService).sendArticleReviewResult("zhangsan@test.com", "测试文章", false, "内容不合格");
    }

    @Test
    void removeBatchDeletesByIds() {
        articleService = buildService();
        when(articleMapper.deleteByIds(anyList())).thenReturn(2);
        articleService.removeBatch(List.of(1L, 2L));
        verify(articleMapper).deleteByIds(List.of(1L, 2L));
    }

    @Test
    void searchReturnsResults() {
        articleService = buildService();
        ArticleSearchQuery searchQuery = new ArticleSearchQuery();
        searchQuery.setCurrent(1L);
        searchQuery.setSize(10L);
        searchQuery.setKeyword("测试");
        Article article = buildPublishedArticle();
        Page<Article> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(article));
        when(articleMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);
        stubSuccessDependencies(article);

        PageInfo<ArticleVO> result = articleService.search(searchQuery);

        assertEquals(1, result.getRecords().size());
        assertEquals("测试文章", result.getRecords().get(0).getTitle());
    }

    @Test
    void searchWithEmptyResultReturnsEmptyPageInfo() {
        articleService = buildService();
        ArticleSearchQuery searchQuery = new ArticleSearchQuery();
        searchQuery.setCurrent(2L);
        searchQuery.setSize(5L);
        searchQuery.setKeyword("不存在");
        when(articleMapper.selectPage(any(Page.class), any(Wrapper.class)))
                .thenReturn(new Page<>(2, 5, 0));

        PageInfo<ArticleVO> result = articleService.search(searchQuery);

        assertEquals(0L, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    void saveWhenSlugExistsThrowsSlugAlreadyExists() {
        articleService = buildService();
        ArticleCreateRequest request = new ArticleCreateRequest();
        request.setTitle("标题");
        request.setSlug("dup");
        request.setSummary("摘要");
        request.setContent("内容");
        request.setStatus(StatusConstant.ARTICLE_DRAFT);
        request.setCategoryId(1L);
        when(articleMapper.exists(any(Wrapper.class))).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> articleService.save(request));
        assertEquals(CodeEnum.SLUG_ALREADY_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void saveWhenCategoryNotExistThrowsCategoryNotFound() {
        articleService = buildService();
        ArticleCreateRequest request = new ArticleCreateRequest();
        request.setTitle("标题");
        request.setSlug("slug-1");
        request.setSummary("摘要");
        request.setContent("内容");
        request.setStatus(StatusConstant.ARTICLE_DRAFT);
        request.setCategoryId(99L);
        when(articleMapper.exists(any(Wrapper.class))).thenReturn(false);
        when(categoryMapper.exists(any(Wrapper.class))).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> articleService.save(request));
        assertEquals(CodeEnum.CATEGORY_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void saveWhenTagNotExistThrowsTagNotFound() {
        articleService = buildService();
        ArticleCreateRequest request = new ArticleCreateRequest();
        request.setTitle("标题");
        request.setSlug("slug-1");
        request.setSummary("摘要");
        request.setContent("内容");
        request.setStatus(StatusConstant.ARTICLE_DRAFT);
        request.setCategoryId(1L);
        request.setTags(List.of(99L));
        when(articleMapper.exists(any(Wrapper.class))).thenReturn(false);
        when(categoryMapper.exists(any(Wrapper.class))).thenReturn(true);
        when(tagMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> articleService.save(request));
        assertEquals(CodeEnum.TAG_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void saveSuccessAsAdminPublishesAndInsertsTags() {
        articleService = buildService();
        loginAsAdmin();
        ArticleCreateRequest request = new ArticleCreateRequest();
        request.setTitle("新文章");
        request.setSlug("new-article");
        request.setSummary("摘要");
        request.setContent("# 内容");
        request.setStatus(StatusConstant.ARTICLE_PUBLISHED);
        request.setCategoryId(1L);
        request.setTags(List.of(1L));
        when(articleMapper.exists(any(Wrapper.class))).thenReturn(false);
        when(categoryMapper.exists(any(Wrapper.class))).thenReturn(true);
        when(tagMapper.selectById(1L)).thenReturn(new Tag());
        when(articleMapper.insert(any(Article.class))).thenAnswer(inv -> {
            Article a = inv.getArgument(0);
            a.setId(888L);
            return 1;
        });

        articleService.save(request);

        verify(articleMapper).insert(any(Article.class));
        verify(articleTagMapper).insert(anyList());
        verify(emailTemplateService, never()).sendArticleReviewNotify(anyString(), anyString(), anyString(), anyString());
        ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
        verify(articleMapper).insert(captor.capture());
        Article saved = captor.getValue();
        assertEquals(StatusConstant.ARTICLE_PUBLISHED, saved.getStatus());
        assertEquals(0, saved.getViewCount());
        assertNotNull(saved.getContentHtml());
    }

    @Test
    void savePendingSendsReviewNotifyEmail() {
        articleService = buildService();
        loginAs(1L, 1);
        ArticleCreateRequest request = new ArticleCreateRequest();
        request.setTitle("待审核新文章");
        request.setSlug("pending-article");
        request.setSummary("摘要");
        request.setContent("内容");
        request.setStatus(StatusConstant.ARTICLE_PENDING);
        request.setCategoryId(1L);
        when(articleMapper.exists(any(Wrapper.class))).thenReturn(false);
        when(categoryMapper.exists(any(Wrapper.class))).thenReturn(true);
        when(articleMapper.insert(any(Article.class))).thenReturn(1);

        articleService.save(request);

        verify(emailTemplateService).sendArticleReviewNotify(eq("admin@lilicould.cn"), eq("待审核新文章"), eq("user1"), anyString());
    }
}