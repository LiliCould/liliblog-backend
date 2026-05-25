package cn.lilicould.liliblog.model.dto.response;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "审计日志对象")
public class AuditLogVO implements Serializable {
    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;
    /**
     * 操作用户名
     */
    @Schema(description = "操作用户名")
    private String username;

    @Schema(description = "操作模块")
    private String module;

    @Schema(description = "操作类型(CREATE/UPDATE/DELETE/AUDIT)")
    private String operation;

    @Schema(description = "目标资源，id或文章title")
    private String target;

    @Schema(description = "目标资源类型")
    private String targetType;

    @Schema(description = "操作结果描述")
    private String description;

    @Schema(description = "请求方法")
    private String requestMethod;

    @Schema(description = "请求URI")
    private String requestUri;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "用户代理")
    private String userAgent;

    @Schema(description = "执行时间(毫秒)")
    private Integer executionTime;

    @Schema(description = "状态，0-失败，1-成功")
    private Integer status;

    @Schema(description = "错误信息（成功则为null）")
    private String errorMessage;

    @Schema(description = "创建时间", example = "2022-01-01 00:00:00",format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
