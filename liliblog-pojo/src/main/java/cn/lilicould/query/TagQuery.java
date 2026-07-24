package cn.lilicould.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "标签查询参数")
public class TagQuery extends BaseQuery implements Serializable {
    @Schema(description = "标签名称",example = "生活")
    private String name;

    @Schema(description = "标签创建时间-起始",example = "2026-05-08 16:58:41")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "标签创建时间-结束",example = "2026-05-08 16:58:41")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

}
