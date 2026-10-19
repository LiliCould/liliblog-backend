package cn.lilicould.liliblog.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 放弃前端直传参数
 */
@Data
@Schema(name = "放弃上传参数")
public class UploadAbandonRequest implements Serializable {

    @NotNull(message = "文件记录ID不能为空")
    @Schema(description = "申请令牌时返回的文件记录ID", example = "1024")
    private Long fileId;
}
