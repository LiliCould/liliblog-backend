package cn.lilicould.liliblog.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 申请前端直传令牌参数
 */
@Data
@Schema(name = "申请上传令牌参数")
public class UploadTokenRequest implements Serializable {

    @NotBlank(message = "上传类型不能为空")
    @Pattern(regexp = "cover|avatar|image|file", message = "上传类型仅支持 cover/avatar/image/file")
    @Schema(description = "上传类型", example = "cover")
    private String type;

    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名过长")
    @Schema(description = "原始文件名", example = "封面.png")
    private String fileName;

    @NotNull(message = "文件大小不能为空")
    @Positive(message = "文件大小必须大于0")
    @Schema(description = "文件字节数", example = "204800")
    private Long fileSize;
}
