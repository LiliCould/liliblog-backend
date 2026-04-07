package com.lilicould.blog.entity;



import java.io.Serializable;

import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 聊天消息表
 * chat_message
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage implements Serializable {

    /**
     * 消息ID
     */
    @NotNull(message="[消息ID]不能为空")
    private Long id;
    /**
     * 发送者ID
     */
    @NotNull(message="[发送者ID]不能为空")
    private Long senderId;
    /**
     * 消息内容
     */
    @NotBlank(message="[消息内容]不能为空")
    @Size(max = 200,message="消息内容限制在0-200个字符")
    @Length(max= 200,message="消息内容限制在0-200个字符")
    private String content;
    /**
     * 消息类型
     */
    private Object type;
    /**
     * 回复的消息ID（0表示非回复）
     */
    private Long parentId;
    /**
     * 状态：0-已删除，1-正常
     */
    private Integer status;
    /**
     * 发送者IP
     */
    @Size(max= 45,message="ip信息异常")
    @Length(max= 45,message="ip信息异常")
    private String ipAddress;
    /**
     * 发送时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * 更新时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

}
