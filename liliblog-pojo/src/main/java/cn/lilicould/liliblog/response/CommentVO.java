package cn.lilicould.liliblog.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "评论展示对象")
public class CommentVO implements Serializable {
    @Schema(description = "评论ID")
    private Long id;
    @Schema(description = "评论内容")
    private String content;
    @Schema(description = "点赞数") // 需要计算点赞数
    private Integer likeCount;
    @Schema(description = "子评论数")
    private Integer childCount;
    @Schema(description = "文章ID")
    private Long articleId;
    @Schema(description = "父评论ID")
    private Long parentId;
    @Schema(description = "用户ip")
    private String ipAddress;
    @Schema(description = "用户ip地址")
    private String ipAddressLocation;
    @Schema(description = "状态,0-审核中,1-发布")
    private Integer status;
    @Schema(description = "创建时间",type = "string",format = "date-time",example = "2026-05-09 14:51:06")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @Schema(description = "发布者信息")
    private UserInfo creator;
    @Schema(description = "评论层级,0-一级评论,1-二级评论")
    private Integer level;
    @Schema(description = "根评论Id")
    private Long rootId;

    public Integer getLevel() {
        if (parentId == null || parentId == 0) {
            return 0;
        } else {
            return 1;
        }
    }
}
