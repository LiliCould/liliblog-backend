package com.lilicould.blog.service.impl;

import com.lilicould.blog.annotation.Log;
import com.lilicould.blog.dao.ArticleTagMapper;
import com.lilicould.blog.dao.TagMapper;
import com.lilicould.blog.dto.TagCreateDTO;
import com.lilicould.blog.dto.TagUpdateDTO;
import com.lilicould.blog.entity.Tag;
import com.lilicould.blog.exception.BusinessException;
import com.lilicould.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED,rollbackFor = Exception.class)
public class TagServiceImpl implements TagService {
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Override
    @Log(value = "添加标签服务")
    public void addTag(TagCreateDTO tag) {
        // 颜色必须满足颜色格式
        if (tag.getColor() != null && !tag.getColor().matches("^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$")) {
            throw new BusinessException("颜色格式错误", 400);
        }
        tagMapper.insert(tag);
    }

    @Override
    @Log(value = "获取所有标签服务")
    public List<Tag> getAllTags() {
        return tagMapper.selectAll();
    }

    @Override
    @Log(value = "删除标签服务")
    public void deleteTag(String tagName) {
        Tag tag = tagMapper.selectByName(tagName);
        if (tag == null) {
            throw new BusinessException("标签不存在",501);
        }
        tagMapper.delete(tag.getId());
    }

    @Override
    @Log(value = "更新标签服务")
    public void updateTag(TagUpdateDTO tag) {
        // 不允许空字符
        if (tag.getName() != null && tag.getName().isEmpty()) {
            tag.setName(null);
        }
        if (tag.getSlug() != null && tag.getSlug().isEmpty()) {
            tag.setSlug(null);
        }
        // 颜色必须满足颜色格式
        if (tag.getColor() != null && !tag.getColor().matches("^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$")) {
            throw new BusinessException("颜色格式错误", 400);
        }
        tagMapper.update(tag);
    }

    @Override
    @Log(value = "获取标签服务")
    public Tag getTag(String tagName) {
        Tag tag = tagMapper.selectByName(tagName);
        if (tag == null) {
            throw new BusinessException("标签不存在",501);
        }
        return tag;
    }

    @Override
    @Log(value = "获取文章标签服务")
    public List<Tag> getTagsByArticleId(Long articleId) {
        return articleTagMapper.getTagsByArticleId(articleId);
    }
}
