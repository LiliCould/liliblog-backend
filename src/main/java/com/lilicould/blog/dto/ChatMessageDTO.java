package com.lilicould.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageDTO implements Serializable {
    private Long id;
    private Long senderId;
    private String senderUsername;
    private String senderName;
    private String senderAvatar;
    private String ipAddress;
    private String content;
    private String type = "TEXT";
    private Long parentId = 0L;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
