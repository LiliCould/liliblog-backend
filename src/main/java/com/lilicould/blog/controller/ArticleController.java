package com.lilicould.blog.controller;

import com.lilicould.blog.dto.ArticleCreateDTO;

import com.lilicould.blog.dto.ArticleUpdateDTO;
import com.lilicould.blog.entity.Article;
import com.lilicould.blog.service.ArticleService;
import com.lilicould.blog.vo.ResultVO;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章控制器
 */
@RestController
@RequestMapping("/api/article")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    /**
     * 获取所有文章
     * @return 所有文章,包括未发布文章（除了已删除的）
     */
    @GetMapping
    public ResultVO<List<Article>> getAllArticles(
            @RequestParam(value = "pageSize",required = false) Integer pageSize, // 每页数量
            @RequestParam(value = "pageNum",required = false) Integer pageNum // 页码
    ) {
        // 如果没有指定每页数量，则默认为10
        if (pageSize == null) {
            pageSize = 10;
        }
        // 如果没有指定页码，则默认为1
        if (pageNum == null) {
            pageNum = 1;
        }
        List<Article> articles = articleService.getAllArticles(pageSize,pageNum);
        return ResultVO.success("成功获取到"+articles.size()+"篇文章",articles);
    }

    /**
     * 获取指定ID的文章
     * @param id 文章ID
     * @return 指定ID的文章
     */
    @GetMapping("/{id}")
    public ResultVO<Article> getArticle(@PathVariable("id") Long id) {
        Article article = articleService.getArticleById(id);
        articleService.incrementViewCount(id); // 增加阅读量
        return ResultVO.success(article);
    }

    /**
     * 根据Slug获取文章
     * @param slug 文章Slug
     * @return 文章，包括未发布的文章
     */
    @GetMapping("/slug/{slug}")
    public ResultVO<Article> getArticleBySlug(@PathVariable("slug") String slug) {
        Article article = articleService.getArticleBySlug(slug);
        articleService.incrementViewCount(article.getId());
        return ResultVO.success(article);
    }

    /**
     * 新建文章
     * @param articleCreateDTO 新建文章的DTO
     * @param username 用户名
     * @return 创建成功VO
     */
    @PostMapping
    public ResultVO<Long> createArticle(
            @Valid @RequestBody ArticleCreateDTO articleCreateDTO,
            @RequestAttribute("username") String username
    ) {
        Long articleId = articleService.createArticle(articleCreateDTO,username);
        return ResultVO.success("新建博文成功，博文ID：" + articleId);
    }

    /**
     * 更新文章
     * @param id 文章ID
     * @param article 文章
     * @return 更新成功VO
     */
    @PutMapping("/{id}")
    public ResultVO<Void> updateArticle(
            @PathVariable("id") Long id,
            @Valid @RequestBody ArticleUpdateDTO articleUpdateDTO,
            @RequestAttribute("username") String username
    ) {
        articleUpdateDTO.setId(id);
        articleService.updateArticle(articleUpdateDTO,username);
        return ResultVO.success("更新文章成功");
    }

    /**
     * 删除文章
     * @param id 文章ID
     * @return 删除成功VO
     */
    @DeleteMapping("/{id}")
    public ResultVO<Void> deleteArticle(@PathVariable("id") Long id, @RequestAttribute("username") String username) {
        articleService.deleteArticle(id, username);
        return ResultVO.success("删除成功");
    }
}
