package cn.lilicould.liliblog.pojo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(name = "发布评论参数")
public class CommentCreateRequest implements Serializable {
    @NotNull
    @Schema(description = "文章ID(不管一二级评论都需要指定)")
    private Long articleId;

    @NotBlank
    @Size(min = 2, max = 500, message = "评论内容长度必须在2-500个字符之间")
    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父评论ID,如果是一级评论，则该字段为空或直接给0")
    private Long parentId;

    @Schema(description = "根评论ID,如果是一级评论，则该字段为空")
    private Long rootId;
}
