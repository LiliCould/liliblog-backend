package com.lilicould.blog.controller;

import com.lilicould.blog.dto.ChatMessageDTO;
import com.lilicould.blog.vo.ResultVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatMessageController {

    @GetMapping("/messages")
    public ResultVO<List<ChatMessageDTO>> getMessages() {
        return ResultVO.success("获取聊天消息成功", null);
    }
}
