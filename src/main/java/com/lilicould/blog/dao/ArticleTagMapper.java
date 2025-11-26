package com.lilicould.blog.dao;

import org.apache.ibatis.annotations.Param;

/**
 * 文章标签关系表Mapper
 */
public interface ArticleTagMapper {
    int insert(@Param("articleId") Long articleId,@Param("tagId") Long tagId);

    int deleteByArticleId(@Param("articleId") Long articleId);
}
