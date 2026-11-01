package cn.lilicould.liliblog.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 站点配置展示对象，GET /api/blog-config 的返回体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "站点配置展示对象")
public class BlogConfigVO implements Serializable {

    @Schema(description = "接口结构版本，结构不兼容变更时递增")
    private Integer version;

    @Schema(description = "配置最后更新时间", example = "2026-11-01 10:00:00", format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "首页 Hero 配置")
    private HeroConfigVO hero;

    @Schema(description = "关于作者配置")
    private AuthorConfigVO author;

    @Schema(description = "扩展配置：未在代码中建模的分组，原样透出")
    private Map<String, Object> extensions;
}
