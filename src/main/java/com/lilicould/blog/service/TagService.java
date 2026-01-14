package com.lilicould.blog.service;

import com.lilicould.blog.dto.TagCreateDTO;
import com.lilicould.blog.dto.TagUpdateDTO;
import com.lilicould.blog.entity.Tag;

import java.util.List;

/**
 * 标签服务接口
 */
public interface TagService {
    void addTag(TagCreateDTO tag);
    List<Tag> getAllTags();
    void deleteTag(String tagName);
    void updateTag(TagUpdateDTO tag);
    Tag getTag(String tagName);
    List<Tag> getTagsByArticleId(Long articleId);
}
