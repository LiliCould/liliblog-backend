package com.lilicould.blog.service.impl;

import com.lilicould.blog.dao.ChatMessageMapper;
import com.lilicould.blog.dto.ChatMessageDTO;
import com.lilicould.blog.entity.ChatMessage;
import com.lilicould.blog.service.ChatMessageService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {
    private final ChatMessageMapper chatMessageMapper;

    public ChatMessageServiceImpl(ChatMessageMapper chatMessageMapper) {
        this.chatMessageMapper = chatMessageMapper;
    }

    @Override
    public List<ChatMessageDTO> getHistory() {

        return chatMessageMapper.listHistory();

    }
}
