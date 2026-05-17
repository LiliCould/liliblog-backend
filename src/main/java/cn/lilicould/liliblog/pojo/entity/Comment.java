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
 * 评论表
 * @TableName comment
 */
@TableName(value ="comment")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class Comment extends CreateOnlyEntity implements Serializable {
    /**
     * 评论ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 评论内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 文章ID
     */
    @TableField(value = "article_id")
    private Long articleId;

    /**
     * 父评论ID
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 状态,0-审核中,1-发布
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 评论者IP
     */
    @TableField(value = "ip_address")
    private String ipAddress;

    /**
     * 用户代理
     */
    @TableField(value = "user_agent")
    private String userAgent;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableField(value = "deleted")
    private Integer deleted;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}