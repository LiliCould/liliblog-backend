package com.lilicould.blog.dao;

import com.lilicould.blog.dto.TagCreateDTO;
import com.lilicould.blog.dto.TagUpdateDTO;
import com.lilicould.blog.entity.Tag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标签Mapper
 */
public interface TagMapper {
    int insert(TagCreateDTO tag);

    Tag selectByName(@Param("name") String name);

    Tag selectBySlug(@Param("slug") String slug);

    Tag selectById(@Param("id") Long id);

    List<Tag> selectAll();

    int update(TagUpdateDTO tag);

    int delete(@Param("id") Long id);
}
