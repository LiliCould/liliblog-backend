package cn.lilicould.liliblog.pojo.entity;

import cn.lilicould.liliblog.pojo.entity.base.CreateOnlyEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天消息表
 * @TableName chat_message
 */
@TableName(value ="chat_message")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class ChatMessage extends CreateOnlyEntity implements Serializable {
    /**
     * 消息ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发送者ID
     */
    @TableField(value = "sender_id")
    private Long senderId;

    /**
     * 消息内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 消息类型
     */
    @TableField(value = "type")
    private Object type;

    /**
     * 回复的消息ID（0表示非回复）
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 状态：0-已删除，1-正常
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 发送者IP
     */
    @TableField(value = "ip_address")
    private String ipAddress;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}