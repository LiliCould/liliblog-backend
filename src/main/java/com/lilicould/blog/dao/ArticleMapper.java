package com.lilicould.blog.dao;


import com.lilicould.blog.entity.Article;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章数据访问接口
 */
public interface ArticleMapper {
    /**
     * 添加文章
     * @param article 文章
     * @return 添加的行数
     */
    int insert(Article article);
    /**
     * 根据ID查询文章
     * @param id 文章ID
     * @return 文章
     */
    Article selectById(@Param("id") Long id);
    /**
     * 根据ID获取文章，但是获取所有状态的文章
     * @param id 文章ID
     */
    Article selectByIdWithAllStatus(@Param("id") Long id);
    /**
     * 根据ID判断文章是否存在
     * @param id 文章ID
     * @return 是否存在
     */
    boolean existsById(@Param("id") Long id);
    /**
     * 根据Slug查询文章
     * @param slug 文章Slug
     * @return 文章
     */
    Article selectBySlug(@Param("slug") String slug);
    /**
     * 根据Slug查询文章，但是获取所有状态的文章
     * @param slug 文章Slug
     * @return 文章
     */
    Article selectBySlugWithAllStatus(@Param("slug") String slug);

    /**
     * 根据Slug判断文章是否存在
     * @param slug
     * @return
     */
    boolean existsBySlug(@Param("slug") String slug);
    /**
     * 查询全部已发布文章
     * @return 文章列表
     */
    List<Article> selectAll(@Param("pageSize") Integer pageSize, @Param("pageNum") Integer pageNum);

    /**
     * 查询全部文章
     * @return 文章列表
     */
    List<Article> selectAllWithAllStatus(@Param("pageSize") Integer pageSize, @Param("pageNum") Integer pageNum);
    /**
     * 模糊搜索,查询全部
     * @param keyword 关键词
     * @return 文章列表
     */
    List<Article> searchAll(@Param("keyword") String keyword);

    /**
     * 模糊搜索，查询指定页码的指定页大小
     * @param keyword 关键词
     * @return 文章列表
     */
    List<Article> search(@Param("keyword") String keyword ,@Param ("pageNum") Integer pageNum,@Param ("pageSize") Integer pageSize);
    /**
     * 更新文章
     * @param article 文章
     * @return 更新的行数
     */
    int update(Article article);
}
