package com.lilicould.blog.service.impl;

import com.lilicould.blog.annotation.Log;
import com.lilicould.blog.dao.ArticleMapper;
import com.lilicould.blog.dao.ArticleTagMapper;
import com.lilicould.blog.dao.UserMapper;
import com.lilicould.blog.dto.ArticleCreateDTO;
import com.lilicould.blog.dto.ArticleUpdateDTO;
import com.lilicould.blog.entity.Article;
import com.lilicould.blog.entity.User;
import com.lilicould.blog.exception.BusinessException;
import com.lilicould.blog.service.ArticleService;
import com.lilicould.blog.util.MarkdownUtil;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ArticleTagMapper articleTagMapper;


    /**
     * 创建文章
     * @param articleCreateDTO 文章创建DTO
     * @return 文章ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class,isolation = Isolation.READ_COMMITTED)
    @Log(value = "创建文章服务")
    public Long createArticle(ArticleCreateDTO articleCreateDTO,String username) {
        User author = userMapper.selectByUsername(username);

        // 设置默认值
        if (articleCreateDTO.getStatus() == null) {
            articleCreateDTO.setStatus("DRAFT");
        }
        Article article = new Article();
        article.setTitle(articleCreateDTO.getTitle());

        if (articleMapper.existsBySlug(articleCreateDTO.getSlug())) {
            throw new BusinessException("文章别名已存在，请更换文章别名");
        } else {
            article.setSlug(articleCreateDTO.getSlug());
        }

        // 如果没有摘要，则从内容中截取100个字符
        if (articleCreateDTO.getSummary() == null || articleCreateDTO.getSummary().isEmpty()) {
            String content = articleCreateDTO.getContent();
            // 去除markdown或HTML标签
            content = MarkdownUtil.markdownToPlainText(content);
            content = Jsoup.parse(content).text();
            article.setSummary(content.substring(0,100));
        } else {
            article.setSummary(articleCreateDTO.getSummary());
        }

        article.setContent(articleCreateDTO.getContent());
        article.setContentHtml(articleCreateDTO.getContentHtml());

        article.setCoverImage(articleCreateDTO.getCoverImage());

        article.setAuthorId(author.getId());
        // 设置状态
        article.setStatus(articleCreateDTO.getStatus());
        // 如果是发布状态，设置发布时间
        if ("PUBLISHED".equals(articleCreateDTO.getStatus())){
            article.setPublishTime(new Date(System.currentTimeMillis()));
        }

        // 设置分类ID
        article.setCategoryId(articleCreateDTO.getCategoryId());


        articleMapper.insert(article);

        // 获取新增文章的ID
        Long articleId = articleMapper.selectBySlugWithAllStatus(articleCreateDTO.getSlug()).getId();

        List<Long> tagIds = articleCreateDTO.getTagIds();
        // 批量向article_tag表中插入文章标签关系
        if (tagIds != null && !tagIds.isEmpty()){
            for (Long tagId : tagIds) {
                articleTagMapper.insert(articleId, tagId);
            }
        }
        return articleId;
    }

    /**
     * 获取所有文章,包括未发布文章（除了已删除的）
     * @return 所有文章
     */
    @Override
    @Log(value = "分页获取所有文章服务")
    public List<Article> getAllArticles(Integer pageSize, Integer pageNum) {
        return articleMapper.selectAllWithAllStatus(pageSize,(pageNum-1)*pageSize);
    }

    /**
     * 获取所有公开文章
     * @return 所有公开文章
     */
    @Override
    @Log(value = "获取所有公开文章服务")
    public List<Article> getAllPublicArticles(Integer pageSize, Integer pageNum) {
        List<Article> articles = new ArrayList<>();
        if (pageSize != null && pageNum != null) {
            articles = articleMapper.selectAll(pageSize,(pageNum-1)*pageSize);
        } else if (pageSize != null) {
            // 如果只有pageSize，则默认pageNum为1
            articles = articleMapper.selectAll(pageSize,0);
        } else if (pageNum != null) {
            // 如果只有pageNum，则默认pageSize为10
            articles = articleMapper.selectAll(10,(pageNum-1)*10);
        } else {
            // 如果都为null，则直接传null，返回所有文章
            articles = articleMapper.selectAll(null,null);
        }

        if (articles == null || articles.isEmpty()) {
            throw new BusinessException("未查询到文章",500);
        }

        return articles;
    }
    /**
     * 获取指定ID的文章
     * @param id 文章ID
     * @return 指定ID的文章
     */
    @Override
    @Log(value = "获取指定ID的文章服务")
    public Article getArticleById(Long id) {
        Article article = articleMapper.selectByIdWithAllStatus(id);
        if (article == null) {
            throw new BusinessException("文章不存在",500);
        }
        return article;
    }

    /**
     * 获取公开指定ID的文章
     * @param id 文章ID
     * @return 文章
     */
    @Override
    @Log(value = "获取公开指定ID的文章服务")
    public Article getPublicArticleById(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在",500);
        }
        return article;
    }

    /**
     * 增加阅读量
     * @param id 文章ID
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Log(value = "增加阅读量服务")
    public void incrementViewCount(Long id) {
        Integer nowViewCount = articleMapper.selectByIdWithAllStatus(id).getViewCount();
        Article article = new Article();
        article.setId(id);
        article.setViewCount(nowViewCount+1);
        if (articleMapper.existsById(id)) {
            articleMapper.update(article);
        }else {
            throw new BusinessException("文章不存在",500);
        }
    }
    /**
     * 获取指定Slug的文章
     * @param slug 文章Slug
     * @return 文章
     */
    @Override
    @Log(value = "获取指定Slug的文章服务")
    public Article getArticleBySlug(String slug) {
        Article article = articleMapper.selectBySlugWithAllStatus(slug);
        if (article == null) {
            throw new BusinessException("文章不存在",500);
        } else {
            return article;
        }
    }

    /**
     * 获取公开指定Slug的文章
     * @param slug 文章Slug
     * @return 文章
     */
    @Override
    @Log(value = "获取公开指定Slug的文章服务")
    public Article getPublicArticleBySlug(String slug) {
        Article article = articleMapper.selectBySlug(slug);
        if (article == null) {
            throw new BusinessException("文章不存在",500);
        } else {
            return article;
        }
    }

    /**
     * 更新文章
     * @param articleUpdateDTO 更新文章的DTO
     * @param username 用户名
     */
    @Override
    @Log(value = "更新文章服务")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateArticle(ArticleUpdateDTO articleUpdateDTO, String username) {
        Article article = articleMapper.selectByIdWithAllStatus(articleUpdateDTO.getId());
        // 文章不存在
        if (article == null) {
            throw new BusinessException("文章不存在",500);
        }
        // 如果不是作者
        if (!article.getAuthorId().equals(userMapper.selectByUsername(username).getId())) {
            User author = userMapper.selectById(article.getAuthorId());
            // 获取作者名称，如果作者已注销，则使用已注销用户
            String authorName = (author != null) ? author.getUsername() : "已注销用户";
            String message = "该文章作者为" + authorName + ",用户" + username + "无更新权限";
            throw new BusinessException(message,400);
        }

        article.setTitle(articleUpdateDTO.getTitle());
        article.setSlug(articleUpdateDTO.getSlug());
        article.setSummary(articleUpdateDTO.getSummary());
        article.setContent(articleUpdateDTO.getContent());
        article.setContentHtml(articleUpdateDTO.getContentHtml());
        article.setCoverImage(articleUpdateDTO.getCoverImage());
        // 如果更新为发布状态，并且发布时间不存在，说明是第一次发布，更新发布时间
        if ("PUBLISHED".equals(articleUpdateDTO.getStatus()) && article.getPublishTime() == null){
            article.setPublishTime(new Date(System.currentTimeMillis()));
        }
        article.setStatus(articleUpdateDTO.getStatus());
        article.setCategoryId(articleUpdateDTO.getCategoryId());

        List<Long> tagIds = articleUpdateDTO.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()){
            // 删除文章标签关系
            articleTagMapper.deleteByArticleId(articleUpdateDTO.getId());
            // 批量向article_tag表中插入文章标签关系
            for (Long tagId : tagIds) {
                articleTagMapper.insert(articleUpdateDTO.getId(), tagId);
            }
        }

        articleMapper.update(article);
    }

    /**
     * 删除文章
     * @param id 文章ID
     */
    @Override
    @Log(value = "删除文章服务")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteArticle(Long id , String username) {
        Article article = articleMapper.selectByIdWithAllStatus(id);
        if (article == null) {
            throw new BusinessException("文章不存在",500);
        }

        if (!article.getAuthorId().equals(userMapper.selectByUsername(username).getId())) {
            User author = userMapper.selectById(article.getAuthorId());
            // 获取作者名称，如果作者已注销，则使用已注销用户
            String authorName = (author != null) ? author.getUsername() : "已注销用户";
            String message = "该文章作者为" + authorName + ",用户" + username + "无删除权限";
            throw new BusinessException(message,400);
        }
        article.setStatus("DELETED");
        articleMapper.update(article);
    }

    @Override
    @Log(value = "搜索服务，不分页")
    public List<Article> searchAll(String keyword) {
        if (keyword == null || keyword.trim().isEmpty() || keyword.isEmpty() || keyword.length() > 50) {
            throw new BusinessException("搜索词不能为空或大于50",400);
        }

        return articleMapper.searchAll(keyword);
    }
}
