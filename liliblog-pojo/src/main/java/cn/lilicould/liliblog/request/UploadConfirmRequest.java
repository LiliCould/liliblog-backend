package cn.lilicould.liliblog.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 确认前端直传完成参数
 */
@Data
@Schema(name = "确认上传完成参数")
public class UploadConfirmRequest implements Serializable {

    @NotNull(message = "文件记录ID不能为空")
    @Schema(description = "申请令牌时返回的文件记录ID", example = "1024")
    private Long fileId;

    @NotBlank(message = "文件hash不能为空")
    @Size(max = 64, message = "文件hash过长")
    @Schema(description = "七牛返回的etag/hash", example = "Fq3x...")
    private String hash;
}
