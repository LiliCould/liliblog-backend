package com.lilicould.blog.dao;

import com.lilicould.blog.entity.Tag;
import org.apache.ibatis.annotations.Param;

/**
 * 标签Mapper
 */
public interface TagMapper {
    int insert(Tag tag);

    Tag selectBySlug(@Param("slug") String slug);

    Tag selectById(@Param("id") Long id);
}
