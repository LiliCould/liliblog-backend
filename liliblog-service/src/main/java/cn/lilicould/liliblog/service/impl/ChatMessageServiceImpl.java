package cn.lilicould.liliblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.lilicould.liliblog.entity.ChatMessage;
import cn.lilicould.liliblog.service.ChatMessageService;
import cn.lilicould.liliblog.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author Lili_Could
* @description 针对表【chat_message(聊天消息表)】的数据库操作Service实现
* @createDate 2026-05-08 16:58:41
*/
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatMessageService{

}




