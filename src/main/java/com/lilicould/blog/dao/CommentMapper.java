package com.lilicould.blog.dao;

import com.lilicould.blog.dto.CommentCreateDTO;
import com.lilicould.blog.dto.CommentUpdateDTO;
import com.lilicould.blog.entity.Comment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentMapper {
    List<Comment> selectAll();

    Comment selectById(@Param("id") Long id);

    List<Comment> selectByArticleId(@Param("articleId") int articleId);

    List<Comment> selectByParentId(@Param("parentId") int parentId);

    void insert(CommentCreateDTO comment);

    void update(CommentUpdateDTO comment);

    void delete(@Param("id") Long id);

}
