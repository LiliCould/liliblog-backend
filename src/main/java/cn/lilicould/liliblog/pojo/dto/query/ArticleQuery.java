package cn.lilicould.liliblog.pojo.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "文章查询参数")
public class ArticleQuery extends BaseQuery implements Serializable {

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章作者ID", example = "1")
    private Long createBy;

    @Schema(description = "文章分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "文章状态，0-审核中，1-发布，2-草稿")
    private Integer status;

    @Schema(description = "文章发布时间范围(开始)",type = "string",format = "date-time",example = "2026-05-09 14:51:06")
    private LocalDateTime startTime;

    @Schema(description = "文章发布时间范围(结束)",type = "string",format = "date-time",example = "2026-05-11 14:51:06")
    private LocalDateTime endTime;
}
