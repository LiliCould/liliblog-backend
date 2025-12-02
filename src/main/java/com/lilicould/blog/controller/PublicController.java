package com.lilicould.blog.controller;

import com.lilicould.blog.entity.Article;
import com.lilicould.blog.service.ArticleService;
import com.lilicould.blog.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
