package cn.lilicould.liliblog.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件记录分页查询参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "文件记录查询参数")
public class FileQuery extends BaseQuery implements Serializable {

    @Schema(description = "上传者ID")
    private Long userId;

    @Schema(description = "业务类型：cover/avatar/image/file")
    private String bizType;

    @Schema(description = "状态：0-待确认，1-成功，2-失败，3-过期，4-拒绝")
    private Integer status;

    @Schema(description = "原始文件名，模糊查询")
    private String originalName;

    @Schema(description = "创建时间起")
    private LocalDateTime createTimeStart;

    @Schema(description = "创建时间止")
    private LocalDateTime createTimeEnd;
}
