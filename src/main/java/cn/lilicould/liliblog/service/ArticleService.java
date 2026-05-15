package cn.lilicould.liliblog.service;

import cn.lilicould.liliblog.pojo.dto.query.ArticleQuery;
import cn.lilicould.liliblog.pojo.dto.request.ArticleRequest;
import cn.lilicould.liliblog.pojo.dto.response.ArticleDetailsVO;
import cn.lilicould.liliblog.pojo.dto.response.ArticleVO;
import cn.lilicould.liliblog.pojo.dto.response.PageInfo;
import cn.lilicould.liliblog.pojo.entity.Article;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Lili_Could
* @description 针对表【article(文章表)】的数据库操作Service
* @createDate 2026-05-08 16:58:40
*/
public interface ArticleService extends IService<Article> {

    /**
     * 根据id获取文章详情
     * @param id 文章ID
     * @return 文章详情
     */
    ArticleDetailsVO getArticle(Long id);

    /**
     * 保存文章
     * @param articleRequest 文章参数
     */
    void save(ArticleRequest articleRequest);

    /**
     * 获取文章列表
     * @param articleQuery 查询参数
     * @return 文章列表
     */
    PageInfo<ArticleVO> getArticleList(ArticleQuery articleQuery);
}
