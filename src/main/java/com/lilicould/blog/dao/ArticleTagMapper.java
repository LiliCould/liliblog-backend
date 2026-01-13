package com.lilicould.blog.dao;

import com.lilicould.blog.entity.Tag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章标签关系表Mapper
 */
public interface ArticleTagMapper {
    List<Tag> getTagsByArticleId(@Param("articleId") Long articleId);

    int insert(@Param("articleId") Long articleId,@Param("tagId") Long tagId);

    int deleteByArticleId(@Param("articleId") Long articleId);
}
