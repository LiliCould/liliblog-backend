package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.common.constant.OrderConstant;
import cn.lilicould.liliblog.common.constant.StatusConstant;
import cn.lilicould.liliblog.common.context.BaseContext;
import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.enums.TargetType;
import cn.lilicould.liliblog.common.exception.BusinessException;
import cn.lilicould.liliblog.common.util.MarkdownUtil;
import cn.lilicould.liliblog.mapper.*;
import cn.lilicould.liliblog.pojo.dto.query.ArticleQuery;
import cn.lilicould.liliblog.pojo.dto.request.ArticleCreateRequest;
import cn.lilicould.liliblog.pojo.dto.request.ArticleUpdateRequest;
import cn.lilicould.liliblog.pojo.dto.response.*;
import cn.lilicould.liliblog.pojo.entity.*;
import cn.lilicould.liliblog.service.ArticleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

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
            return null;
        }

        if (!hasReadAuthority(article)) {
            throw new BusinessException(CodeEnum.NO_PERMISSION);
        }

        ArticleDetailsVO articleDetailsVO = new ArticleDetailsVO();
        BeanUtils.copyProperties(article, articleDetailsVO);

        // 设置作者和更新者信息
        articleDetailsVO.setCreator(buildUserInfo(article.getCreateBy()));
        articleDetailsVO.setUpdater(buildUserInfo(article.getUpdateBy()));

        // 设置点赞数和评论数
        articleDetailsVO.setLikeCount(getLikeCount(id));
        articleDetailsVO.setCommentCount(getCommentCount(id));

        // 设置分类信息
        articleDetailsVO.setCategory(buildCategoryVO(article.getCategoryId()));

        // 设置标签列表
        articleDetailsVO.setTags(buildTagVOList(id));

        log.info(articleDetailsVO.toString());
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
        page.addOrder(OrderItem.desc(OrderConstant.UPDATE_TIME)); // 排序

        // 查询条件
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        // 基础条件：标题模糊查询
        queryWrapper.like(articleQuery.getTitle() != null, Article::getTitle, articleQuery.getTitle())
                .eq(articleQuery.getCreateBy() != null, Article::getCreateBy, articleQuery.getCreateBy())
                .eq(articleQuery.getCategoryId() != null, Article::getCategoryId, articleQuery.getCategoryId())
                .between(
                        articleQuery.getStartTime() != null && articleQuery.getEndTime() != null,
                        Article::getCreateTime,
                        articleQuery.getStartTime(),
                        articleQuery.getEndTime()
                )
                .select(Article::getId, Article::getTitle, Article::getSlug, Article::getSummary, Article::getCoverImage, Article::getViewCount, Article::getCategoryId, Article::getCreateBy, Article::getUpdateBy, Article::getCreateTime, Article::getUpdateTime);

        // 权限控制：根据用户角色和登录状态过滤文章
        applyPermissionFilter(queryWrapper, articleQuery.getStatus());

        // 查询
        Page<Article> articlePage = articleMapper.selectPage(page, queryWrapper);

        if (articlePage.getTotal() == 0)
            return PageInfo.empty(articleQuery.getCurrent(), articleQuery.getSize());

        // 填充信息并返回
        List<ArticleVO> records = articlePage.getRecords().stream().map(article -> {
            ArticleVO articleVO = new ArticleVO();
            BeanUtils.copyProperties(article, articleVO);
            articleVO.setCreator(buildUserInfo(article.getCreateBy())); // 设置作者信息
            articleVO.setUpdater(buildUserInfo(article.getUpdateBy())); // 设置新者信息
            articleVO.setLikeCount(getLikeCount(article.getId())); // 填充点赞数和评论数
            articleVO.setCommentCount(getCommentCount(article.getId())); // 填充点赞数和评论数
            articleVO.setCategory(buildCategoryVO(article.getCategoryId())); // 填充分类信息
            articleVO.setTags(buildTagVOList(article.getId())); // 填充标签列表
            return articleVO;
        }).toList();

        Page<ArticleVO> voPage = new Page<>(articlePage.getCurrent(), articlePage.getSize(), articlePage.getTotal()); // 会自动计算其他信息
        voPage.setRecords(records);
        return PageInfo.of(voPage);
    }

    /**
     * 删除文章
     * @param id 文章ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class,isolation = Isolation.READ_COMMITTED)
    public void remove(Long id) {
        // 校验文章是否存在
        if (!this.exists(id)) {
            throw new BusinessException(CodeEnum.ARTICLE_NOT_FOUND);
        }

        // 校验权限
        if (!hasWriteAuthority(id)) {
            throw new BusinessException(CodeEnum.NO_PERMISSION);
        }

        // 删除文章
        this.removeById(id);

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
    public void update(Long id, ArticleUpdateRequest articleUpdateRequest) {
        if (id == null) {
            throw new BusinessException(CodeEnum.PARAM_MISSING);
        }

        // 校验文章是否存在
        if (!this.exists(id)) {
            throw new BusinessException(CodeEnum.ARTICLE_NOT_FOUND);
        }

        // 校验权限
        if (!hasWriteAuthority(id)) {
            throw new BusinessException(CodeEnum.NO_PERMISSION);
        }

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

        // 检查标签列表是否存在
        for (Long tagId : articleUpdateRequest.getTags()) {
            Tag tag = tagMapper.selectById(tagId);
            if (tag == null) {
                throw new BusinessException(CodeEnum.TAG_NOT_FOUND);
            }
        }

        // 删除原来标签
        LambdaQueryWrapper<ArticleTag> articleTagQueryWrapper = new LambdaQueryWrapper<>();
        articleTagQueryWrapper.eq(ArticleTag::getArticleId, id);
        articleTagMapper.delete(articleTagQueryWrapper);

        // 更新文章
        articleMapper.updateById(article);

        // 批量插入文章标签关联
        saveArticleTags(article.getId(), articleUpdateRequest.getTags());

    }

    /**
     * 保存文章
     * @param articleCreateRequest 文章参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class,isolation = Isolation.READ_COMMITTED)
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

        // 检查标签列表是否存在
        for (Long tagId : articleCreateRequest.getTags()) {
            Tag tag = tagMapper.selectById(tagId);
            if (tag == null) {
                throw new BusinessException(CodeEnum.TAG_NOT_FOUND);
            }
        }
        // 存文章
        articleMapper.insert(article);

        // 批量插入文章标签关联
        saveArticleTags(article.getId(), articleCreateRequest.getTags());
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
    private boolean hasWriteAuthority(Long articleId) {
        if (BaseContext.isAdmin()) {
            return true;  // 管理员
        }

        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getId, articleId)
                .select(Article::getCreateBy);
        Long createBy = articleMapper.selectOne(queryWrapper).getCreateBy(); // 获取作者ID

        if (createBy == null) {
            return false; // 作者不存在,则只允许管理员修改
        }

        return Objects.equals(BaseContext.getCurrentUserId(), createBy); // 当前用户是否为作者本人
    }

    /**
     * 判断文章是否存在
     */
    private boolean exists(Long articleId) {
        if (articleId == null) {
            return false;
        }
        return articleMapper.exists(new LambdaQueryWrapper<Article>().eq(Article::getId, articleId));
    }
    /**
     * 构建用户信息
     */
    private UserInfo buildUserInfo(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userMapper.selectById(userId);
        return user != null ? UserInfo.from(user) : null;
    }

    /**
     * 获取文章点赞数
     */
    private int getLikeCount(Long articleId) {
        LambdaQueryWrapper<LikeRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LikeRecord::getTargetId, articleId)
                .eq(LikeRecord::getTargetType, TargetType.ARTICLE.getCode());
        return likeRecordMapper.selectCount(queryWrapper).intValue();
    }

    /**
     * 获取文章评论数
     */
    private int getCommentCount(Long articleId) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getArticleId, articleId)
                .eq(Comment::getStatus, StatusConstant.COMMENT_PUBLISHED);
        return commentMapper.selectCount(queryWrapper).intValue();
    }

    /**
     * 构建分类信息
     */
    private CategoryVO buildCategoryVO(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            return null;
        }
        if (StatusConstant.DISABLED.equals(category.getStatus())) { // 如果分类被禁用，则不返回
            return null;
        }
        CategoryVO categoryVO = new CategoryVO();
        BeanUtils.copyProperties(category, categoryVO);
        return categoryVO;
    }

    /**
     * 构建标签列表
     */
    private List<TagVO> buildTagVOList(Long articleId) {
        List<Tag> tags = tagMapper.selectTagsByArticleId(articleId);
        log.debug("标签列表：{}", tags);
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .map(tag -> {
                    TagVO tagVO = new TagVO();
                    BeanUtils.copyProperties(tag, tagVO);
                    return tagVO;
                })
                .toList();
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




