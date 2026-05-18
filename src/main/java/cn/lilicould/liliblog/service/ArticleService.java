package cn.lilicould.liliblog.service;

import cn.lilicould.liliblog.pojo.dto.query.ArticleQuery;
import cn.lilicould.liliblog.pojo.dto.request.ArticleCreateRequest;
import cn.lilicould.liliblog.pojo.dto.request.ArticleUpdateRequest;
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
     * @param articleCreateRequest 文章参数
     */
    void save(ArticleCreateRequest articleCreateRequest);

    /**
     * 获取文章列表
     * @param articleQuery 查询参数
     * @return 文章列表
     */
    PageInfo<ArticleVO> getArticleList(ArticleQuery articleQuery);

    /**
     * 删除文章
     * @param id 文章ID
     */
    void remove(Long id);


    /**
     * 更新文章
     * @param id 文章ID
     * @param articleCreateRequest 文章参数
     */
    void update(Long id, ArticleUpdateRequest articleUpdateRequest);

    /**
     * 根据slug获取文章详情
     * @param slug 文章slug
     * @return 文章详情
     */
    ArticleDetailsVO getArticleBySlug(String slug);
}
