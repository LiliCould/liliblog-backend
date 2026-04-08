package com.lilicould.blog.service;

import com.lilicould.blog.dto.ChatMessageDTO;
import org.springframework.stereotype.Service;

import java.util.List;

public interface ChatMessageService {

    /**
     * 获取聊天记录(七天内)
     * @return 七天内的聊天记录
     */
    List<ChatMessageDTO> getHistory();
}
