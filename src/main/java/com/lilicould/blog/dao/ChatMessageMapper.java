package com.lilicould.blog.dao;

import com.lilicould.blog.entity.ChatMessage;

/**
* @author Lili_Could
* 针对表【chat_message(聊天消息表)】的数据库操作Mapper
* 2026-04-07 17:15:10
* com.lilicould.blog.entity.ChatMessage
*/
public interface ChatMessageMapper {

    int deleteByPrimaryKey(Long id);

    int insert(ChatMessage record);

    int insertSelective(ChatMessage record);

    ChatMessage selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ChatMessage record);

    int updateByPrimaryKey(ChatMessage record);

}
