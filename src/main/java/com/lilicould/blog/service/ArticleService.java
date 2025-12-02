package com.lilicould.blog.service;

import com.lilicould.blog.dto.ArticleCreateDTO;
import com.lilicould.blog.dto.ArticleUpdateDTO;
import com.lilicould.blog.entity.Article;

import java.util.List;

/**
 * 文章服务接口
 */
public interface ArticleService {
    /**
     * 创建文章
     *
     * @param article 文章
     * @return 文章ID
     */
    Long createArticle(ArticleCreateDTO articleCreateDTO,String username);

    List<Article> getAllArticles(Integer pageSize, Integer pageNum);

    List<Article> getAllPublicArticles(Integer pageSize, Integer pageNum);

    Article getArticleById(Long id);

    Article getPublicArticleById(Long id);

    void incrementViewCount(Long id);

    Article getArticleBySlug(String slug);

    Article getPublicArticleBySlug(String slug);

    void updateArticle(ArticleUpdateDTO articleUpdateDTO, String username);

    void deleteArticle(Long id, String username);

}
