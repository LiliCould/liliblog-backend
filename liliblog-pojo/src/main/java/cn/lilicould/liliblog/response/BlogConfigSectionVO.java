package cn.lilicould.liliblog.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站点配置分组对象，供管理端编辑使用
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "站点配置分组对象")
public class BlogConfigSectionVO implements Serializable {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "配置分组键")
    private String key;

    @Schema(description = "分组配置内容，原样返回 JSON")
    private Object value;

    @Schema(description = "是否启用：0-禁用，1-启用")
    private Integer enabled;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间", example = "2026-11-01 10:00:00", format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2026-11-01 10:00:00", format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
