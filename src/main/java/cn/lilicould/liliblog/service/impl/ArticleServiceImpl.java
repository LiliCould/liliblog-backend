package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.common.constant.StatusConstant;
import cn.lilicould.liliblog.common.context.BaseContext;
import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.enums.TargetType;
import cn.lilicould.liliblog.common.exception.BusinessException;
import cn.lilicould.liliblog.common.util.MarkdownUtil;
import cn.lilicould.liliblog.mapper.*;
import cn.lilicould.liliblog.pojo.dto.request.ArticleRequest;
import cn.lilicould.liliblog.pojo.dto.response.ArticleVO;
import cn.lilicould.liliblog.pojo.dto.response.CategoryVO;
import cn.lilicould.liliblog.pojo.dto.response.TagVO;
import cn.lilicould.liliblog.pojo.dto.response.UserInfo;
import cn.lilicould.liliblog.pojo.entity.*;
import cn.lilicould.liliblog.service.ArticleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

    @Override
    public ArticleVO getArticle(Long id) {
        // 查询文章基础信息
        Article article = articleMapper.selectById(id);
        if (article == null) {
            return null;
        }

        if (!hasAuthority(article)) {
            throw new BusinessException(CodeEnum.NO_PERMISSION);
        }

        ArticleVO articleVO = new ArticleVO();
        BeanUtils.copyProperties(article, articleVO);

        // 设置作者和更新者信息
        articleVO.setCreator(buildUserInfo(article.getCreateBy()));
        articleVO.setUpdater(buildUserInfo(article.getUpdateBy()));

        // 设置点赞数和评论数
        articleVO.setLikeCount(getLikeCount(id));
        articleVO.setCommentCount(getCommentCount(id));

        // 设置分类信息
        articleVO.setCategory(buildCategoryVO(article.getCategoryId()));

        // 设置标签列表
        articleVO.setTags(buildTagVOList(id));

        log.info(articleVO.toString());
        return articleVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class,isolation = Isolation.READ_COMMITTED)
    public void save(ArticleRequest articleRequest) {

        // 拷贝数据
        Article article = new Article();
        BeanUtils.copyProperties(articleRequest, article);

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

        // 检查分类是否存在
        if (!categoryMapper.exists(new LambdaQueryWrapper<Category>().eq(Category::getId, article.getCategoryId()))) {
            throw new BusinessException(CodeEnum.CATEGORY_NOT_FOUND);
        }

        // 检查标签列表是否存在
        for (Long tagId : articleRequest.getTags()) {
            Tag tag = tagMapper.selectById(tagId);
            if (tag == null) {
                throw new BusinessException(CodeEnum.TAG_NOT_FOUND);
            }
        }
        // 存文章
        articleMapper.insert(article);
        // 批量插入文章标签关联
        if (articleRequest.getTags() != null && !articleRequest.getTags().isEmpty()) {
            List<ArticleTag> articleTags = articleRequest.getTags().stream()
                    .map(tagId -> {
                        ArticleTag articleTag = new ArticleTag();
                        articleTag.setArticleId(article.getId());
                        articleTag.setTagId(tagId);
                        return articleTag;
                    })
                    .toList();
            articleTagMapper.insert(articleTags);
        }
    }

    /**
     * 判断当前用户是否有权限访问文章
     */
    private boolean hasAuthority(Article article) {
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
}




