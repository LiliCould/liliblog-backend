package cn.lilicould.query;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "分类查询参数")
public class CategoryQuery extends BaseQuery implements Serializable {

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类别名,精准查询,可以由唯一slug指定单个分类", example = "the-use-method-of-aop")
    private String slug;

    @Schema(description = "分类状态，1-启用，0-禁用")
    private Integer status;

    @Schema(description = "分类描述")
    private String description;

    @Schema(description = "创建时间-起始",example = "2026-01-01 00:00:00")
    @Parameter(description = "创建时间-起始")
    private LocalDateTime startTime;
    @Schema(description = "创建时间-结束",example = "2026-06-01 00:00:00")
    @Parameter(description = "创建时间-结束")
    private LocalDateTime endTime;
}
