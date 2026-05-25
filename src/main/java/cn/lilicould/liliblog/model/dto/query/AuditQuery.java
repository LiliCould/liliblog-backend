package cn.lilicould.liliblog.model.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "审计日志查询参数")
public class AuditQuery extends BaseQuery implements Serializable {

    @Schema(description = "操作用户名（模糊查询）", example = "admin")
    private String username;

    @Schema(description = "模块名称", example = "article")
    private String module;

    @Schema(description = "操作类型", example = "CREATE")
    private String operation;

    @Schema(description = "目标资源ID", example = "1")
    private Long target;

    @Schema(description = "目标资源类型", example = "ARTICLE")
    private String targetType;

    @Schema(description = "操作状态", example = "1", allowableValues = {"0", "1"})
    private Integer status;

    @Schema(description = "开始时间", type = "string", format = "date-time", example = "2026-05-20 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", type = "string", format = "date-time", example = "2026-05-25 23:59:59")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "IP地址", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "请求URI（模糊查询）", example = "/api/article")
    private String requestUri;
}
