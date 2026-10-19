package cn.lilicould.liliblog.entity;

import cn.lilicould.liliblog.entity.base.CreateOnlyEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件引用关系表
 * @TableName file_ref
 */
@TableName(value = "file_ref")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileRef extends CreateOnlyEntity implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件记录ID
     */
    @TableField(value = "file_id")
    private Long fileId;

    /**
     * 引用目标类型：ARTICLE/ARTICLE_COVER/ARTICLE_IMAGE/USER_AVATAR
     */
    @TableField(value = "target_type")
    private String targetType;

    /**
     * 引用目标ID
     */
    @TableField(value = "target_id")
    private Long targetId;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
