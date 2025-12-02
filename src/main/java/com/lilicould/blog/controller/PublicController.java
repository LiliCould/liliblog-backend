package com.lilicould.blog.controller;

import com.lilicould.blog.entity.Article;
import com.lilicould.blog.service.ArticleService;
import com.lilicould.blog.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公开控制器
 * @author lilicould
 */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    @Autowired
    private ArticleService articleService;

    /**
     * 获取公开文章列表，若不指定页码和每页数量，则返回所有文章
     * @param pageSize 每页数量
     * @param pageNum 页码
     * @return 公开文章列表
     */
    @GetMapping("/articles")
    public ResultVO<List<Article>> getArticles(
            @RequestParam(value = "pageSize",required = false) Integer pageSize,
            @RequestParam(value = "pageNum",required = false) Integer pageNum
    ) {
        List<Article> articles = articleService.getAllPublicArticles(pageSize, pageNum);
        return ResultVO.success("成功获取到"+articles.size()+"条文章", articles);
    }

    /**
     * 获取公开文章
     * @param id 文章ID
     * @return 公开文章
     */
    @GetMapping("/article/{id}")
    public ResultVO<Article> getArticle(@PathVariable("id") Long id) {
        Article article = articleService.getPublicArticleById(id);
        articleService.incrementViewCount(id);
        return ResultVO.success(article);
    }

    /**
     * 获取公开文章
     * @param slug 文章Slug
     * @return 公开文章
     */
    @GetMapping("/article/slug/{slug}")
    public ResultVO<Article> getArticleBySlug(@PathVariable("slug") String slug) {
        Article article = articleService.getPublicArticleBySlug(slug);
        articleService.incrementViewCount(article.getId());
        return ResultVO.success(article);
    }

}
