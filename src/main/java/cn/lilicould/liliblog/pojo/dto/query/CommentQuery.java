package cn.lilicould.liliblog.pojo.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "评论查询参数")
public class CommentQuery extends BaseQuery implements Serializable {

    @NotNull
    @Schema(description = "文章ID")
    private Long articleId;
}
