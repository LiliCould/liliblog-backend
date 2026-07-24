package cn.lilicould.liliblog.service;

import cn.lilicould.query.ArticleQuery;
import cn.lilicould.query.ArticleSearchQuery;
import cn.lilicould.request.ArticleCreateRequest;
import cn.lilicould.request.ArticleUpdateRequest;
import cn.lilicould.response.ArticleDetailsVO;
import cn.lilicould.response.ArticleVO;
import cn.lilicould.response.PageInfo;
import cn.lilicould.entity.Article;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

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
     * @param articleUpdateRequest 文章修改参数
     */
    void update(Long id, ArticleUpdateRequest articleUpdateRequest);

    /**
     * 根据slug获取文章详情
     * @param slug 文章slug
     * @return 文章详情
     */
    ArticleDetailsVO getArticleBySlug(String slug);

    /**
     * 审核文章
     * @param id 文章ID
     * @param status 审核状态
     * @param reason 审核失败原因
     */
    void auditArticle(Long id, Integer status, String reason);

    /**
     * 批量删除文章
     * @param ids 文章ID列表
     */
    void removeBatch(List<Long> ids);

    /**
     * 文章搜索
     * @param searchQuery 搜索参数
     * @return 搜索结果
     */
    PageInfo<ArticleVO> search(ArticleSearchQuery searchQuery);
}
