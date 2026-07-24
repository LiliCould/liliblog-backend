package cn.lilicould.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "文章审核请求")
public class ArticleAuditRequest implements Serializable {
    @Schema(description = "审核结果描述，不通过时建议填写原因")
    private String reason;
}
