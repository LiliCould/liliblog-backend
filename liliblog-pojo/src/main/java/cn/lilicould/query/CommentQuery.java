package cn.lilicould.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "评论查询参数")
public class CommentQuery extends BaseQuery implements Serializable {

    @Schema(description = "如果是查询一级评论，就是文章id,否则是根评论id，如果是评论管理接口，就是评论本身id")
    private Long id;

    @Schema(description = "评论内容,模糊查询", example = "不错")
    private String content;
    @Schema(description = "文章id")
    private Long articleId;
    @Schema(description = "父评论id")
    private Long parentId;
    @Schema(description = "根评论id")
    private Long rootId;
    @Schema(description = "状态，0-审核中，1-已发布")
    private Integer status;
    @Schema(description = "发布时间-起始", example = "2026-01-01 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @Schema(description = "发布时间-结束", example = "2026-05-01 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
