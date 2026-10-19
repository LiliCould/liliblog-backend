package cn.lilicould.liliblog.response;

import cn.lilicould.liliblog.entity.FileRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件记录视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "文件记录")
public class FileRecordVO implements Serializable {

    @Schema(description = "文件记录ID")
    private Long id;

    @Schema(description = "上传者ID")
    private Long userId;

    @Schema(description = "对象key")
    private String fileKey;

    @Schema(description = "访问URL")
    private String url;

    @Schema(description = "原始文件名")
    private String originalName;

    @Schema(description = "按扩展名判定的MIME")
    private String mimeType;

    @Schema(description = "魔数检测出的MIME")
    private String detectedMime;

    @Schema(description = "真实字节数")
    private Long fileSize;

    @Schema(description = "七牛etag")
    private String hash;

    @Schema(description = "业务类型：cover/avatar/image/file")
    private String bizType;

    @Schema(description = "上传来源：1-前端直传，2-后端中转")
    private Integer uploadSource;

    @Schema(description = "状态：0-待确认，1-成功，2-失败，3-过期，4-拒绝")
    private Integer status;

    @Schema(description = "失败原因")
    private String errorMsg;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    public static FileRecordVO from(FileRecord record) {
        if (record == null) {
            return null;
        }
        return FileRecordVO.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .fileKey(record.getFileKey())
                .url(record.getUrl())
                .originalName(record.getOriginalName())
                .mimeType(record.getMimeType())
                .detectedMime(record.getDetectedMime())
                .fileSize(record.getFileSize())
                .hash(record.getHash())
                .bizType(record.getBizType())
                .uploadSource(record.getUploadSource())
                .status(record.getStatus())
                .errorMsg(record.getErrorMsg())
                .createTime(record.getCreateTime())
                .build();
    }
}
