package cn.lilicould.liliblog.pojo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "评论展示对象")
public class CommentVO implements Serializable {
    @Schema(description = "评论ID")
    private Long id;
    @Schema(description = "评论内容")
    private String content;
    @Schema(description = "点赞数") // 需要计算点赞数
    private Integer likeCount;
    @Schema(description = "文章ID")
    private Long articleId;
    @Schema(description = "父评论ID")
    private Long parentId;
    @Schema(description = "用户ip")
    private String ipAddress;
    @Schema(description = "状态,0-审核中,1-发布")
    private LocalDateTime createTime;
    @Schema(description = "发布者信息")
    private UserInfo creator;
}
