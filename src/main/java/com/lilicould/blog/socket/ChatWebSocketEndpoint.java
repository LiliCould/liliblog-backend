package com.lilicould.blog.socket;

import com.lilicould.blog.config.WebSocketConfig;
import com.lilicould.blog.dao.ChatMessageMapper;
import com.lilicould.blog.dao.UserMapper;
import com.lilicould.blog.dto.ChatMessageDTO;
import com.lilicould.blog.entity.ChatMessage;
import com.lilicould.blog.entity.User;
import com.lilicould.blog.util.RedisUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@ServerEndpoint(value = "/ws/chat", configurator = WebSocketConfig.WebSocketHandshakeInterceptor.class)
public class ChatWebSocketEndpoint {

    // todo 使用redis替换ConcurrentHashMap
    private static final String ONLINE_COUNT_KEY = "online_count";
    private static final String CHAT_SESSIONS_KEY = "chat_sessions";

    // 存储所有连接的会话
    private static final Map<Long, Session> sessions = new ConcurrentHashMap<>();
    // 使用redis
    private static RedisUtil redisUtil;
    private static ObjectMapper objectMapper;
    private static UserMapper userMapper;
    private static ChatMessageMapper chatMessageMapper;

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        ChatWebSocketEndpoint.userMapper = userMapper;
    }
    @Autowired
    public void setChatMessageMapper(ChatMessageMapper chatMessageMapper) {
        ChatWebSocketEndpoint.chatMessageMapper = chatMessageMapper;
    }
    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        ChatWebSocketEndpoint.objectMapper = objectMapper;
    }
    @Autowired
    public void setRedisUtil(RedisUtil redisUtil) {
        ChatWebSocketEndpoint.redisUtil = redisUtil;
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        // 设置会话不超时
        session.setMaxIdleTimeout(0);
        Long userId = (Long) config.getUserProperties().get("userId");
        String ipAddress = (String) config.getUserProperties().get("ipAddress");

        if (userId != null) {
            sessions.put(userId, session);
            session.getUserProperties().put("ipAddress", ipAddress);
            log.info("用户 {} 已连接，IP: {}，当前在线人数: {}", userId, ipAddress, sessions.size());

            User user = userMapper.selectById(userId);
            String content = "用户 [" + user.getNickname() + "] 风风光光进入了聊天室";

            ChatMessageDTO systemMessage = ChatMessageDTO.builder()
                    .senderId(0L)
                    .senderName("系统")
                    .senderUsername("system")
                    .content(content)
                    .type("SYSTEM")
                    .parentId(0L)
                    .createTime(new Date())
                    .build();

            // 向所有人发送信息
            broadcastMessage(systemMessage);

            // 广播在线用户列表
            broadcastOnlineUsers();
        } else {
            // 尝试关闭会话
            try {
                session.close();
            } catch (IOException e) {
                log.error("关闭会话失败", e);
            }
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {

        // 心跳消息
        if ("PING".equals(message)) {
            session.getBasicRemote().sendText("PONG");
            return;
        }

        Long userId = getUserIdFromSession(session);
        if (userId == null) {
            return;
        }

        log.info("收到用户 {} 的消息: {}", userId, message);

        try {
            ChatMessageDTO chatMessage = objectMapper.readValue(message, ChatMessageDTO.class);
            chatMessage.setSenderId(userId);

            // 填充用户信息
            User user = userMapper.selectById(userId);
            chatMessage.setSenderName(user.getNickname());
            chatMessage.setSenderAvatar(user.getAvatar());
            chatMessage.setSenderUsername(user.getUsername());

            // 设置消息创建时间
            chatMessage.setCreateTime(new Date(System.currentTimeMillis()));

            // 获取用户IP
            String ipAddress = (String) session.getUserProperties().get("ipAddress");
            chatMessage.setIpAddress(ipAddress);

            ChatMessage record = ChatMessage.builder()
                        .senderId(chatMessage.getSenderId())
                        .content(chatMessage.getContent())
                        .type(chatMessage.getType())
                        .parentId(chatMessage.getParentId())
                        .status(1)
                        .ipAddress(chatMessage.getIpAddress())
                        .createTime(chatMessage.getCreateTime())
                        .build();

            // 保存消息
            chatMessageMapper.insert(record);

            // 设置消息ID
            chatMessage.setId(record.getId());

            // 广播消息
            broadcastMessage(chatMessage);
        } catch (Exception e) {
            log.error("处理消息失败", e);
        }
    }

    @OnClose
    public void onClose(Session session) {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.remove(userId);
            User user = userMapper.selectById(userId);
            String content = "用户 [" + user.getNickname() + "] 悄悄地离开了~~~";
            ChatMessageDTO systemMessage = ChatMessageDTO.builder()
                    .senderId(0L)
                    .senderName("系统")
                    .senderUsername("system")
                    .content(content)
                    .type("SYSTEM")
                    .parentId(0L)
                    .createTime(new Date())
                    .build();
            broadcastMessage(systemMessage);

            broadcastOnlineUsers();
            log.info("用户 {} 已断开，当前在线人数: {}", userId, sessions.size());
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        Long userId = getUserIdFromSession(session);
        log.error("用户 {} WebSocket 错误", userId, error);

        if (session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                log.error("关闭会话失败", e);
            }
        }
    }


    private Long getUserIdFromSession(Session session) {
        for (Map.Entry<Long, Session> entry : sessions.entrySet()) {
            if (entry.getValue().equals(session)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 广播消息给所有连接的用户
     * @param message 要广播的消息
     */
    private void broadcastMessage(ChatMessageDTO message) {
        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("消息序列化失败", e);
            return;
        }

        sessions.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(jsonMessage);
                } catch (IOException e) {
                    log.error("发送消息给用户 {} 失败", userId, e);
                }
            }
        });
    }

    /**
     * 发送系统消息给指定会话
     * @param session 要发送的系统消息的会话
     * @param content 要发送的系统消息的内容
     */
    private void sendSystemMessage(Session session, String content) {
        try {
            ChatMessageDTO systemMessage = new ChatMessageDTO();
            systemMessage.setType("SYSTEM");
            systemMessage.setContent(content);

            String json = objectMapper.writeValueAsString(systemMessage);
            session.getBasicRemote().sendText(json);
        } catch (Exception e) {
            log.error("发送系统消息失败", e);
        }
    }

    private void broadcastOnlineUsers() {
        try {
            List<User> onlineUsers = sessions.keySet().stream()
                    .map(userId -> userMapper.selectById(userId))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            ChatMessageDTO onlineListMessage = ChatMessageDTO.builder()
                    .senderId(0L)
                    .senderName("系统")
                    .senderUsername("system")
                    .type("ONLINE_LIST")
                    .parentId(0L)
                    .createTime(new Date())
                    .build();

            String onlineUsersJson = objectMapper.writeValueAsString(onlineUsers);
            onlineListMessage.setContent(onlineUsersJson);

            String jsonMessage = objectMapper.writeValueAsString(onlineListMessage);

            sessions.forEach((userId, session) -> {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(jsonMessage);
                    } catch (IOException e) {
                        log.error("发送在线用户列表给用户 {} 失败", userId, e);
                    }
                }
            });

            log.info("广播在线用户列表，当前在线人数: {}", onlineUsers.size());
        } catch (Exception e) {
            log.error("广播在线用户列表失败", e);
        }
    }

    public static int getOnlineCount() {
        return sessions.size();
    }


}
