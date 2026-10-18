package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.annotation.Audit;
import cn.lilicould.liliblog.cache.RedisHelper;
import cn.lilicould.liliblog.constant.OrderConstant;
import cn.lilicould.liliblog.constant.RedisPrefixConstant;
import cn.lilicould.liliblog.constant.StatusConstant;
import cn.lilicould.liliblog.context.BaseContext;
import cn.lilicould.liliblog.entity.*;
import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.enums.TargetType;
import cn.lilicould.liliblog.exception.BusinessException;
import cn.lilicould.liliblog.response.*;
import cn.lilicould.liliblog.util.MarkdownUtil;
import cn.lilicould.liliblog.config.properties.InfoProperties;
import cn.lilicould.liliblog.mapper.*;
import cn.lilicould.liliblog.query.ArticleQuery;
import cn.lilicould.liliblog.query.ArticleSearchQuery;
import cn.lilicould.liliblog.request.ArticleCreateRequest;
import cn.lilicould.liliblog.request.ArticleUpdateRequest;
import cn.lilicould.liliblog.service.ArticleService;
import cn.lilicould.liliblog.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Lili_Could
* @description 针对表【article(文章表)】的数据库操作Service实现
* @createDate 2026-05-08 16:58:40
*/
@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article>
    implements ArticleService{

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final CommentMapper commentMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final UserService userService;
    private final EmailTemplateService emailTemplateService;
    private final InfoProperties infoProperties;
    private final RedisHelper redisHelper;

    /**
     * ngram 全文索引的最小分词长度（与 ngram_token_size 保持一致）。
     * 小于该长度的关键词无法形成 ngram token，需要回退 LIKE。
     */
    private static final int MIN_FULLTEXT_KEYWORD_LENGTH = 2;

    /**
     * 根据id获取文章详情
     * @param id 文章ID
     * @return 文章详情
     */
    @Override
    public ArticleDetailsVO getArticle(Long id) {
        // 查询文章基础信息
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(CodeEnum.ARTICLE_NOT_FOUND);
        }

        if (!hasReadAuthority(article)) {
            throw new BusinessException(CodeEnum.NO_PERMISSION);
        }

        ArticleDetailsVO articleDetailsVO = buildArticleDetails(article);

        log.info("获取到文章id：{}",articleDetailsVO.getId());
        return articleDetailsVO;
    }

    /**
     * 用已查询出的文章构建详情VO，并原子递增阅读量。
     * 关联数据（作者、更新者、分类、标签、点赞数、评论数）一次性批量加载，避免 N+1。
     */
    private ArticleDetailsVO buildArticleDetails(Article article) {
        ArticleDetailsVO articleDetailsVO = new ArticleDetailsVO();
        BeanUtils.copyProperties(article, articleDetailsVO);

        ArticleExtras extras = loadArticleExtras(List.of(article));
        fillArticleDetailsVO(articleDetailsVO, article, extras);

        // 阅读量 +1：只更新一列，不回写 longtext，也不刷新 update_time
        articleMapper.incrementViewCount(article.getId());

        return articleDetailsVO;
    }

    /**
     * 获取文章列表
     * @param articleQuery 查询参数
     * @return 文章列表
     */
    @Override
    public PageInfo<ArticleVO> getArticleList(ArticleQuery articleQuery) {
        // 初始化分页参数
        Page<Article> page = Page.of(articleQuery.getCurrent(), articleQuery.getSize());
        page.addOrder(OrderItem.desc(OrderConstant.CREATE_TIME));
        page.addOrder(OrderItem.desc(OrderConstant.UPDATE_TIME)); // 排序

        // 查询条件
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        // 基础条件：标题筛选（>= 2 字符走 ngram 全文索引，单字回退 LIKE）
        applyTitleFilter(queryWrapper, articleQuery.getTitle());
        queryWrapper.eq(articleQuery.getCreateBy() != null, Article::getCreateBy, articleQuery.getCreateBy())
                .eq(articleQuery.getCategoryId() != null, Article::getCategoryId, articleQuery.getCategoryId())
                .between(
                        articleQuery.getStartTime() != null && articleQuery.getEndTime() != null,
                        Article::getCreateTime,
                        articleQuery.getStartTime(),
                        articleQuery.getEndTime()
                )
                .select(Article::getId, Article::getTitle, Article::getSlug, Article::getStatus, Article::getSummary, Article::getCoverImage, Article::getViewCount, Article::getCategoryId, Article::getCreateBy, Article::getUpdateBy, Article::getCreateTime, Article::getUpdateTime);

        // 权限控制：根据用户角色和登录状态过滤文章
        applyPermissionFilter(queryWrapper, articleQuery.getStatus());

        // 查询
        Page<Article> articlePage = articleMapper.selectPage(page, queryWrapper);

        // 转换为VO返回
        return convertToVO(articlePage, articleQuery.getCurrent(), articleQuery.getSize());
    }

    /**
     * 删除文章
     * @param id 文章ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class,isolation = Isolation.READ_COMMITTED)
    @Audit(
            module = "article",
            operation = "DELETE",
            description = "'删除文章:' + #id",
            targetType = "ARTICLE",
            target = "#id"
    )
    public void remove(Long id) {
        // 一次查询同时拿到存在性和作者，避免同一行被查 2 次
        Article existing = articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                .eq(Article::getId, id)
                .select(Article::getId, Article::getCreateBy));
        if (existing == null) {
            throw new BusinessException(CodeEnum.ARTICLE_NOT_FOUND);
        }

        // 校验权限
        if (!hasWriteAuthority(existing)) {
            throw new BusinessException(CodeEnum.NO_PERMISSION);
        }

        // 删除文章
        this.removeById(id);

        // 同步随机文章缓存池：从池中移除（幂等，不存在不报错）
        removeFromArticleRandomPool(id);

        // 删除点赞记录
        LambdaQueryWrapper<LikeRecord> likeQueryWrapper = new LambdaQueryWrapper<>();
        likeQueryWrapper.eq(LikeRecord::getTargetId, id)
                .eq(LikeRecord::getTargetType, TargetType.ARTICLE.getCode());
        likeRecordMapper.delete(likeQueryWrapper);

        // 删除对应的article_tag记录
        LambdaQueryWrapper<ArticleTag> articleTagQueryWrapper = new LambdaQueryWrapper<>();
        articleTagQueryWrapper.eq(ArticleTag::getArticleId, id);
        articleTagMapper.delete(articleTagQueryWrapper);

        // 删除对应的所有评论
        LambdaQueryWrapper<Comment> commentQueryWrapper = new LambdaQueryWrapper<>();
        commentQueryWrapper.eq(Comment::getArticleId, id);
        commentMapper.delete(commentQueryWrapper);
    }


    /**
     * 修改文章
     * @param id 文章ID
     * @param articleUpdateRequest 文章参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class,isolation = Isolation.READ_COMMITTED)
    @Audit(
            module = "article",
            operation = "UPDATE",
            description = "'更新文章:' + #id",
            targetType = "ARTICLE",
            target = "#id"
    )
    public void update(Long id, ArticleUpdateRequest articleUpdateRequest) {
        if (id == null) {
            throw new BusinessException(CodeEnum.PARAM_MISSING);
        }

        // 一次查询同时拿到存在性、作者和更新前状态，避免同一行被查 3 次
        Article existing = articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                .eq(Article::getId, id)
                .select(Article::getId, Article::getCreateBy, Article::getStatus));
        if (existing == null) {
            throw new BusinessException(CodeEnum.ARTICLE_NOT_FOUND);
        }

        // 校验权限
        if (!hasWriteAuthority(existing)) {
            throw new BusinessException(CodeEnum.NO_PERMISSION);
        }

        // 获取更新前的状态
        Integer oldStatus = existing.getStatus();

        // 拷贝数据
        Article article = new Article();
        BeanUtils.copyProperties(articleUpdateRequest, article);
        article.setId(id);

        // 将markdown内容转为HTML
        String contentHtml = MarkdownUtil.markdownToHtml(article.getContent());
        article.setContentHtml(contentHtml);

        // 设置状态（根据权限信息计算）
        article.setStatus(calculateStatus(article.getStatus()));

        // 如果要修改别名，则检查别名是否已存在（排除自己）
        if (articleUpdateRequest.getSlug() != null && articleMapper.exists(new LambdaQueryWrapper<Article>().eq(Article::getSlug, articleUpdateRequest.getSlug()).ne(Article::getId, id))) {
            throw new BusinessException(CodeEnum.SLUG_ALREADY_EXISTS);
        }

        // 检查分类是否存在且被启用
        if (articleUpdateRequest.getCategoryId() != null
                &&
                !categoryMapper.exists(new LambdaQueryWrapper<Category>()
                        .eq(Category::getId, articleUpdateRequest.getCategoryId())
                        .eq(Category::getStatus,StatusConstant.ENABLED)
                )) {
            throw new BusinessException(CodeEnum.CATEGORY_NOT_FOUND);
        }

        // 检查标签列表是否存在（批量查询，避免逐个 selectById）
        validateTags(articleUpdateRequest.getTags());

        // 删除原来标签
        LambdaQueryWrapper<ArticleTag> articleTagQueryWrapper = new LambdaQueryWrapper<>();
        articleTagQueryWrapper.eq(ArticleTag::getArticleId, id);
        articleTagMapper.delete(articleTagQueryWrapper);

        // 更新文章
        articleMapper.updateById(article);

        // 同步随机文章缓存池
        syncArticleRandomPool(id, oldStatus, article.getStatus());

        // 如果是待审核,发送邮件通知管理员审核
        if (StatusConstant.ARTICLE_PENDING.equals(article.getStatus())) {
            emailTemplateService.sendArticleReviewNotify(infoProperties.getAdminEmail(), article.getTitle(),BaseContext.getCurrentUserName(),LocalDateTime.now().toString());
        }

        // 批量插入文章标签关联
        saveArticleTags(article.getId(), articleUpdateRequest.getTags());

    }

    /**
     * 获取文章详情
     * @param slug 别名
     * @return 文章详情
     */
    @Override
    public ArticleDetailsVO getArticleBySlug(String slug) {
        // 查询文章基础信息（slug 上有唯一索引）
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getSlug, slug);
        Article article = articleMapper.selectOne(queryWrapper);
        if (article == null) {
            return null;
        }
        if (!hasReadAuthority(article)) {
            throw new BusinessException(CodeEnum.NO_PERMISSION);
        }
        // 直接复用已查出的文章，避免按 id 再查一次
        return buildArticleDetails(article);
    }

    /**
     * 审核文章
     *
     * @param id 文章ID
     * @param status 0:待审核 1:审核通过 2:审核未通过
     * @param reason 原因
     */
    @Override
    @Audit(
            module = "article",
            operation = "AUDIT",
            description = "'审核文章:' + #id",
            targetType = "ARTICLE",
            target = "#id"
    )
    public void auditArticle(Long id, Integer status, String reason) {
        // 根据审核结果更新文章状态
        Article article = this.getById(id);

        if (article == null) {
            throw new BusinessException(CodeEnum.ARTICLE_NOT_FOUND);
        }

        // 获取审核前的状态
        Integer oldStatus = article.getStatus();

        // 查询文章的作者
        Long authorId = article.getCreateBy();
        if (authorId ==  null) {
            // 理论上不会走到这里，因为文章的作者不能为空，但是怕有未知情况，所以这里加上
            throw new BusinessException(CodeEnum.USER_NOT_FOUND);
        }
        User author = userService.getById(authorId);
        String email = author.getEmail();

        // 发送邮件给作者
        String articleTitle = article.getTitle();
        if (StatusConstant.ARTICLE_PUBLISHED.equals(status)) {
            // 审核通过，发送邮件给作者
            emailTemplateService.sendArticleReviewResult(email, articleTitle, true, reason);
        } else if (StatusConstant.ARTICLE_DRAFT.equals(status)) {
            // 审核不通过，发送邮件给作者
            emailTemplateService.sendArticleReviewResult(email, articleTitle, false, reason);
        }


        // 更新文章状态
        article.setStatus(status);
        this.updateById(article);

        // 同步随机文章缓存池
        syncArticleRandomPool(id, oldStatus, status);
    }

    /**
     * 批量删除文章
     * @param ids 文章ID列表
     */
    @Override
    @Audit(
            module = "article",
            operation = "BATCH_DELETE",
            description = "'批量删除文章，数量:' + #ids.size()",
            targetType = "ARTICLE",
            target = "#ids"  // 取第一个ID作为代表
    )
    public void removeBatch(List<Long> ids) {
        this.removeByIds(ids);

        // 同步随机文章缓存池：批量移除
        if (ids != null && !ids.isEmpty()) {
            Object[] idArray = ids.toArray();
            redisHelper.sRemove(RedisPrefixConstant.ARTICLE_RANDOM_POOL, idArray);
        }
    }

    /**
     * 搜索文章
     * @param searchQuery 搜索条件
     * @return 文章列表
     */
    @Override
    public PageInfo<ArticleVO> search(ArticleSearchQuery searchQuery) {
        Page<Article> page = Page.of(searchQuery.getCurrent(), searchQuery.getSize());
        String keyword = searchQuery.getKeyword();
        // 排序
        page.addOrder(OrderItem.descs(OrderConstant.ID, OrderConstant.CREATE_TIME, OrderConstant.UPDATE_TIME));

        // 构造查询条件：使用 ngram 全文索引 ft_article_search(title, summary, content)，
        // 不再使用会导致全表扫描的 LIKE '%keyword%'。
        // 关键词用双引号包成短语，保证与原先“连续子串”语义一致。
        String fullTextQuery = buildFullTextQuery(keyword);
        if (fullTextQuery == null) {
            return PageInfo.empty(searchQuery.getCurrent(), searchQuery.getSize());
        }

        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.apply("MATCH(title, summary, content) AGAINST ({0} IN BOOLEAN MODE)", fullTextQuery)
                .eq(Article::getStatus, StatusConstant.ARTICLE_PUBLISHED)
                // 列表VO不需要 content/content_html，避免把 longtext 读出来
                .select(Article::getId, Article::getTitle, Article::getSlug, Article::getStatus, Article::getSummary,
                        Article::getCoverImage, Article::getViewCount, Article::getCategoryId,
                        Article::getCreateBy, Article::getUpdateBy, Article::getCreateTime, Article::getUpdateTime);

        // 查询
        Page<Article> articlePage = this.page(page, queryWrapper);

        // 转换为返回
        return convertToVO(articlePage, searchQuery.getCurrent(), searchQuery.getSize());
    }

    /**
     * 查询指定数量的随机文章
     * @param num 查询文章数量
     *
     * @return 查询结果
     */
    @Override
    public List<ArticleVO> randomArticle(int num) {
        if (num < 1 || num > 10) {
            throw new BusinessException(CodeEnum.COMMON_PARAM_ERROR);
        }

        List<Long> randomIds = getRandomArticleIds(num);

        if (randomIds.isEmpty()) {
            return List.of();
        }

        // 批量查询文章
        List<Article> articles = this.listByIds(randomIds);
        if (articles.isEmpty()) {
            return List.of();
        }

        // 关联数据一次性批量加载，避免每篇文章再查 6 次
        ArticleExtras extras = loadArticleExtras(articles);
        return articles.stream().map(article -> toArticleVO(article, extras)).toList();
    }

    /**
     * 从 Redis 缓存池中随机获取文章ID，若缓存不存在则从DB加载
     */
    private List<Long> getRandomArticleIds(int num) {
        String poolKey = RedisPrefixConstant.ARTICLE_RANDOM_POOL;

        // 缓存不存在则从DB加载
        if (!Boolean.TRUE.equals(redisHelper.exists(poolKey))) {
            refreshArticleRandomPool();
        }

        // 从缓存中随机获取
        List<Object> randomMembers = redisHelper.sRandMember(poolKey, num);
        if (randomMembers == null || randomMembers.isEmpty()) {
            return List.of();
        }

        return randomMembers.stream()
                .map(obj -> Long.valueOf(obj.toString()))
                .toList();
    }

    /**
     * 从数据库加载所有已发布文章ID到 Redis 缓存池
     */
    private void refreshArticleRandomPool() {
        String poolKey = RedisPrefixConstant.ARTICLE_RANDOM_POOL;

        List<Long> publishedArticleIds = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, StatusConstant.ARTICLE_PUBLISHED)
                        .eq(Article::getDeleted, StatusConstant.NOT_DELETED)
                        .select(Article::getId)
        ).stream().map(Article::getId).toList();

        if (!publishedArticleIds.isEmpty()) {
            Object[] ids = publishedArticleIds.toArray();
            redisHelper.sAdd(poolKey, ids);
        }
    }

    /**
     * 将文章ID加入 Redis 缓存池
     */
    private void addToArticleRandomPool(Long articleId) {
        redisHelper.sAdd(RedisPrefixConstant.ARTICLE_RANDOM_POOL, articleId);
    }

    /**
     * 从 Redis 缓存池中移除文章ID
     */
    private void removeFromArticleRandomPool(Long articleId) {
        redisHelper.sRemove(RedisPrefixConstant.ARTICLE_RANDOM_POOL, articleId);
    }

    /**
     * 同步文章状态变更后的随机文章缓存池
     * @param articleId 文章ID
     * @param oldStatus 更新前状态
     * @param newStatus 更新后状态
     */
    private void syncArticleRandomPool(Long articleId, Integer oldStatus, Integer newStatus) {
        boolean wasPublished = StatusConstant.ARTICLE_PUBLISHED.equals(oldStatus);
        boolean isPublished = StatusConstant.ARTICLE_PUBLISHED.equals(newStatus);

        if (!wasPublished && isPublished) {
            addToArticleRandomPool(articleId);
        } else if (wasPublished && !isPublished) {
            removeFromArticleRandomPool(articleId);
        }
    }

    /**
     * 保存文章
     * @param articleCreateRequest 文章参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class,isolation = Isolation.READ_COMMITTED)
    @Audit(
            module = "article",
            operation = "CREATE",
            description = "'创建文章: ' + #articleCreateRequest.title",
            targetType = "ARTICLE",
            target = "#articleCreateRequest.title"
    )
    public void save(ArticleCreateRequest articleCreateRequest) {

        // 拷贝数据
        Article article = new Article();
        BeanUtils.copyProperties(articleCreateRequest, article);

        // 将markdown内容转为HTML
        String contentHtml = MarkdownUtil.markdownToHtml(article.getContent());
        article.setContentHtml(contentHtml);

        // 设置默认值
        article.setStatus(calculateStatus(article.getStatus()));
        article.setViewCount(0); // 阅读量默认0

        // 检查别名是否存在
        if (articleMapper.exists(new LambdaQueryWrapper<Article>().eq(Article::getSlug, article.getSlug()))) {
            throw new BusinessException(CodeEnum.SLUG_ALREADY_EXISTS);
        }

        // 检查分类是否存在且启用
        if (!categoryMapper.exists(new LambdaQueryWrapper<Category>()
                .eq(Category::getId, article.getCategoryId())
                .eq(article.getStatus() != null, Category::getStatus, StatusConstant.ENABLED)
        )) {
            throw new BusinessException(CodeEnum.CATEGORY_NOT_FOUND);
        }

        // 检查标签列表是否存在（批量查询，避免逐个 selectById）
        validateTags(articleCreateRequest.getTags());
        // 存文章
        articleMapper.insert(article);

        // 同步随机文章缓存池：如果文章最终状态为已发布，加入缓存池
        if (StatusConstant.ARTICLE_PUBLISHED.equals(article.getStatus())) {
            addToArticleRandomPool(article.getId());
        }

        // 如果提交状态为待审核,发送邮件通知站长审核
        if (StatusConstant.ARTICLE_PENDING.equals(articleCreateRequest.getStatus())) {
            emailTemplateService.sendArticleReviewNotify(infoProperties.getAdminEmail(), article.getTitle(),BaseContext.getCurrentUserName(),LocalDateTime.now().toString());
        }

        // 批量插入文章标签关联
        saveArticleTags(article.getId(), articleCreateRequest.getTags());
    }

    private PageInfo<ArticleVO> convertToVO(Page<Article> articlePage, long current, long size) {
        if (articlePage.getTotal() == 0)
            return PageInfo.empty(current, size);

        List<Article> articles = articlePage.getRecords();
        if (articles.isEmpty()) {
            return PageInfo.empty(current, size);
        }

        // 关联数据一次性批量加载，避免 N+1
        ArticleExtras extras = loadArticleExtras(articles);
        List<ArticleVO> records = articles.stream()
                .map(article -> toArticleVO(article, extras))
                .toList();

        Page<ArticleVO> voPage = new Page<>(articlePage.getCurrent(), articlePage.getSize(), articlePage.getTotal()); // 会自动计算其他信息
        voPage.setRecords(records);

        return PageInfo.of(voPage);
    }

    /**
     * 批量加载文章列表/详情所需的所有关联数据。
     * 无论多少篇文章，固定只需要 6 条 SQL：用户、分类、文章标签关联、标签、点赞数、评论数。
     */
    private ArticleExtras loadArticleExtras(Collection<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            return ArticleExtras.empty();
        }

        Set<Long> articleIds = articles.stream().map(Article::getId).collect(Collectors.toSet());
        Set<Long> userIds = articles.stream()
                .flatMap(article -> java.util.stream.Stream.of(article.getCreateBy(), article.getUpdateBy()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> categoryIds = articles.stream()
                .map(Article::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        ArticleExtras extras = new ArticleExtras();
        extras.users = loadUserInfoMap(userIds);
        extras.categories = loadCategoryVOMap(categoryIds);
        extras.tagMap = loadTagVOMap(articleIds);
        extras.likeCounts = loadLikeCountMap(articleIds, TargetType.ARTICLE.getCode());
        extras.commentCounts = loadCommentCountMap(articleIds);
        return extras;
    }

    private ArticleVO toArticleVO(Article article, ArticleExtras extras) {
        ArticleVO articleVO = new ArticleVO();
        BeanUtils.copyProperties(article, articleVO);
        fillArticleVO(articleVO, article, extras);
        return articleVO;
    }

    /**
     * 把批量加载好的关联数据填充到列表VO
     */
    private void fillArticleVO(ArticleVO articleVO, Article article, ArticleExtras extras) {
        articleVO.setCreator(article.getCreateBy() == null ? null : extras.users.get(article.getCreateBy()));
        articleVO.setUpdater(article.getUpdateBy() == null ? null : extras.users.get(article.getUpdateBy()));
        articleVO.setLikeCount(extras.likeCounts.getOrDefault(article.getId(), 0));
        articleVO.setCommentCount(extras.commentCounts.getOrDefault(article.getId(), 0));
        articleVO.setCategory(article.getCategoryId() == null ? null : extras.categories.get(article.getCategoryId()));
        articleVO.setTags(extras.tagMap.getOrDefault(article.getId(), List.of()));
    }

    /**
     * 把批量加载好的关联数据填充到详情VO
     */
    private void fillArticleDetailsVO(ArticleDetailsVO articleDetailsVO, Article article, ArticleExtras extras) {
        articleDetailsVO.setCreator(article.getCreateBy() == null ? null : extras.users.get(article.getCreateBy()));
        articleDetailsVO.setUpdater(article.getUpdateBy() == null ? null : extras.users.get(article.getUpdateBy()));
        articleDetailsVO.setLikeCount(extras.likeCounts.getOrDefault(article.getId(), 0));
        articleDetailsVO.setCommentCount(extras.commentCounts.getOrDefault(article.getId(), 0));
        articleDetailsVO.setCategory(article.getCategoryId() == null ? null : extras.categories.get(article.getCategoryId()));
        articleDetailsVO.setTags(extras.tagMap.getOrDefault(article.getId(), List.of()));
    }

    private Map<Long, UserInfo> loadUserInfoMap(Collection<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, UserInfo::from));
    }

    private Map<Long, CategoryVO> loadCategoryVOMap(Collection<Long> categoryIds) {
        if (categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return categoryMapper.selectBatchIds(categoryIds).stream()
                // 与原先 buildCategoryVO 一致：禁用的分类不返回
                .filter(category -> !StatusConstant.DISABLED.equals(category.getStatus()))
                .collect(Collectors.toMap(Category::getId, category -> {
                    CategoryVO categoryVO = new CategoryVO();
                    BeanUtils.copyProperties(category, categoryVO);
                    return categoryVO;
                }));
    }

    /**
     * 一次查出所有文章-标签关联，再按标签ID批量查标签，固定 2 条 SQL。
     * article_tag 走 uk_article_tag(article_id, tag_id) 覆盖索引，tag 走主键。
     */
    private Map<Long, List<TagVO>> loadTagVOMap(Collection<Long> articleIds) {
        if (articleIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ArticleTag> relations = articleTagMapper.selectList(new LambdaQueryWrapper<ArticleTag>()
                .in(ArticleTag::getArticleId, articleIds)
                .select(ArticleTag::getArticleId, ArticleTag::getTagId)
                .orderByAsc(ArticleTag::getId));
        if (relations.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> tagIds = relations.stream().map(ArticleTag::getTagId).collect(Collectors.toSet());
        Map<Long, TagVO> tagVOMap = tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, tag -> {
                    TagVO tagVO = new TagVO();
                    BeanUtils.copyProperties(tag, tagVO);
                    return tagVO;
                }));

        Map<Long, List<TagVO>> result = new HashMap<>();
        for (ArticleTag relation : relations) {
            TagVO tagVO = tagVOMap.get(relation.getTagId());
            if (tagVO != null) {
                result.computeIfAbsent(relation.getArticleId(), key -> new ArrayList<>()).add(tagVO);
            }
        }
        return result;
    }

    /**
     * 批量统计点赞数。like_record 走 idx_target(target_id, target_type)。
     */
    private Map<Long, Integer> loadLikeCountMap(Collection<Long> targetIds, Integer targetType) {
        if (targetIds.isEmpty()) {
            return Collections.emptyMap();
        }
        QueryWrapper<LikeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("target_id AS target_id", "COUNT(*) AS cnt")
                .eq("target_type", targetType)
                .in("target_id", targetIds)
                .groupBy("target_id");
        return toCountMap(likeRecordMapper.selectMaps(queryWrapper));
    }

    /**
     * 批量统计评论数。comment 走 idx_comment_article_status(article_id, status, deleted)。
     */
    private Map<Long, Integer> loadCommentCountMap(Collection<Long> articleIds) {
        if (articleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("article_id AS target_id", "COUNT(*) AS cnt")
                .eq("status", StatusConstant.COMMENT_PUBLISHED)
                .in("article_id", articleIds)
                .groupBy("article_id");
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

    /**
     * 标题筛选：长度 >= ngram_token_size 时走 ft_article_title 全文索引；
     * 单字无法形成 ngram token，回退 LIKE 保持原有行为。
     */
    private void applyTitleFilter(LambdaQueryWrapper<Article> queryWrapper, String title) {
        if (title == null || title.isBlank()) {
            return;
        }

        String trimmed = title.trim();
        int length = trimmed.codePointCount(0, trimmed.length());
        if (length >= MIN_FULLTEXT_KEYWORD_LENGTH) {
            String fullTextQuery = buildFullTextQuery(trimmed);
            if (fullTextQuery != null) {
                queryWrapper.apply("MATCH(title) AGAINST ({0} IN BOOLEAN MODE)", fullTextQuery);
                return;
            }
        }

        queryWrapper.like(Article::getTitle, title);
    }

    /**
     * 把用户输入包装成布尔模式短语，避免 + - * 等操作符改变查询语义。
     */
    private String buildFullTextQuery(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String escaped = keyword.trim()
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }

    /**
     * 批量校验标签是否存在。一条 SQL 代替逐个 selectById。
     */
    private void validateTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        Set<Long> distinctTagIds = tagIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (distinctTagIds.isEmpty()) {
            return;
        }
        if (tagMapper.selectBatchIds(distinctTagIds).size() != distinctTagIds.size()) {
            throw new BusinessException(CodeEnum.TAG_NOT_FOUND);
        }
    }

    /**
     * 批量加载文章关联数据的一次性快照
     */
    private static final class ArticleExtras {
        private Map<Long, UserInfo> users = Collections.emptyMap();
        private Map<Long, CategoryVO> categories = Collections.emptyMap();
        private Map<Long, List<TagVO>> tagMap = Collections.emptyMap();
        private Map<Long, Integer> likeCounts = Collections.emptyMap();
        private Map<Long, Integer> commentCounts = Collections.emptyMap();

        private static ArticleExtras empty() {
            return new ArticleExtras();
        }
    }

    /**
     * 判断当前用户是否有权限访问文章
     */
    private boolean hasReadAuthority(Article article) {
        if (BaseContext.isAdmin()) { // 管理员
            return true;
        }

        if (StatusConstant.ARTICLE_PENDING.equals(article.getStatus())
                || StatusConstant.ARTICLE_DRAFT.equals(article.getStatus())) { // 待审核或草稿
            return Objects.equals(BaseContext.getCurrentUserId(), article.getCreateBy()); // 作者本人可访问
        }

        return StatusConstant.ARTICLE_PUBLISHED.equals(article.getStatus()); // 已发布则默认所有人可以看
    }

    /**
     * 判断当前用户是否有权限修改文章
     * @param articleId 文章ID
     * @return 是否有权限
     */
    private boolean hasWriteAuthority(Article article) {
        if (BaseContext.isAdmin()) {
            return true;  // 管理员
        }

        Long createBy = article.getCreateBy();
        if (createBy == null) {
            return false; // 作者不存在,则只允许管理员修改
        }

        return Objects.equals(BaseContext.getCurrentUserId(), createBy); // 当前用户是否为作者本人
    }


    /**
     * 计算目标状态
     */
    private Integer calculateStatus(Integer targetStatus) {
        // 如果是草稿，直接保存为草稿（任何人都有权存草稿）
        if (StatusConstant.ARTICLE_DRAFT.equals(targetStatus)) {
            return StatusConstant.ARTICLE_DRAFT;
        }
        // 如果是发布状态
        else if (StatusConstant.ARTICLE_PUBLISHED.equals(targetStatus)) {
            // 管理员：直接发布
            if (BaseContext.isAdmin()) {
                return StatusConstant.ARTICLE_PUBLISHED;
            }
            // 普通用户：转为待审核
            else {
                return StatusConstant.ARTICLE_PENDING;
            }
        }
        // 其他情况（如待审核、或者非法状态），统一归为待审核
        else {
            return StatusConstant.ARTICLE_PENDING;
        }
    }


    /**
     * 应用权限过滤条件
     * @param queryWrapper 查询包装器
     * @param status 查询的状态（可能为null）
     */
    private void applyPermissionFilter(LambdaQueryWrapper<Article> queryWrapper, Integer status) {
        Long currentUserId = BaseContext.getCurrentUserId();
        boolean isAdmin = BaseContext.isAdmin();

        // 情况1：管理员 - 可以查所有状态，如果指定了status则按status查
        if (isAdmin) {
            queryWrapper.eq(status != null, Article::getStatus, status);
            return;
        }

        // 情况2：未登录用户 - 只能查已发布文章
        if (currentUserId == null) {
            queryWrapper.eq(Article::getStatus, StatusConstant.ARTICLE_PUBLISHED);
            return;
        }

        // 情况3：已登录普通用户
        if (status != null) {
            // 如果指定了已发布状态，可以查所有人的已发布文章
            if (StatusConstant.ARTICLE_PUBLISHED.equals(status)) {
                queryWrapper.eq(Article::getStatus, status);
            }
            // 如果指定了非已发布状态（待审核/草稿），只能查自己的
            else {
                queryWrapper.eq(Article::getStatus, status)
                        .eq(Article::getCreateBy, currentUserId);
            }
        } else {
            // 如果未指定status，可以查：别人的已发布文章 + 自己的所有文章
            queryWrapper.and(wrapper ->
                    wrapper.eq(Article::getStatus, StatusConstant.ARTICLE_PUBLISHED)  // 已发布的文章
                            .or()  // 或者
                            .eq(Article::getCreateBy, currentUserId)  // 自己创建的文章（所有状态）
            );
        }
    }

    /**
     * 保存文章标签关联
     * @param articleId 文章ID
     * @param tagIds 标签ID列表
     */
    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        List<ArticleTag> articleTags = tagIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(tagId -> {
                    ArticleTag articleTag = new ArticleTag();
                    articleTag.setArticleId(articleId);
                    articleTag.setTagId(tagId);
                    return articleTag;
                })
                .toList();
        articleTagMapper.insert(articleTags);
    }
}




