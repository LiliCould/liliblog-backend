package cn.lilicould.liliblog.entity;

import cn.lilicould.liliblog.entity.base.FullBaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件上传记录表
 * @TableName file_record
 */
@TableName(value = "file_record")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileRecord extends FullBaseEntity implements Serializable {

    /**
     * 文件记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 上传者ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 对象key，唯一
     */
    @TableField(value = "file_key")
    private String fileKey;

    /**
     * 访问URL
     */
    @TableField(value = "url")
    private String url;

    /**
     * 原始文件名
     */
    @TableField(value = "original_name")
    private String originalName;

    /**
     * 按扩展名判定的MIME
     */
    @TableField(value = "mime_type")
    private String mimeType;

    /**
     * 魔数检测出的MIME
     */
    @TableField(value = "detected_mime")
    private String detectedMime;

    /**
     * 真实字节数
     */
    @TableField(value = "file_size")
    private Long fileSize;

    /**
     * 七牛etag
     */
    @TableField(value = "hash")
    private String hash;

    /**
     * 业务类型：cover/avatar/image/file
     */
    @TableField(value = "biz_type")
    private String bizType;

    /**
     * 上传来源：1-前端直传，2-后端中转，3-历史文件导入
     */
    @TableField(value = "upload_source")
    private Integer uploadSource;

    /**
     * 状态：0-待确认，1-成功，2-失败，3-过期，4-拒绝
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 失败原因
     */
    @TableField(value = "error_msg")
    private String errorMsg;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableField(value = "deleted")
    private Integer deleted;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
