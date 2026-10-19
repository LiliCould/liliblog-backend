package cn.lilicould.liliblog.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 前端直传令牌
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "前端直传令牌")
public class UploadTokenVO implements Serializable {

    @Schema(description = "文件记录ID，确认上传时使用")
    private Long fileId;

    @Schema(description = "七牛上传域名")
    private String uploadUrl;

    @Schema(description = "七牛上传令牌，仅能上传当前key")
    private String token;

    @Schema(description = "服务端生成的对象key")
    private String key;

    @Schema(description = "文件访问URL")
    private String url;

    @Schema(description = "令牌有效期（秒）")
    private Long expiresIn;

    @Schema(description = "允许的最大字节数")
    private Long maxSize;

    @Schema(description = "允许的MIME类型，分号分隔")
    private String accept;
}
