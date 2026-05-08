package cn.lilicould.liliblog.entity;

import cn.lilicould.liliblog.entity.base.CreateOnlyEntity;
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
 * 点赞记录表
 * @TableName like_record
 */
@TableName(value ="like_record")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class LikeRecord extends CreateOnlyEntity implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 目标ID（文章ID或评论ID）
     */
    @TableField(value = "target_id")
    private Long targetId;

    /**
     * 目标类型，0-文章，1-评论
     */
    @TableField(value = "target_type")
    private Integer targetType;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}