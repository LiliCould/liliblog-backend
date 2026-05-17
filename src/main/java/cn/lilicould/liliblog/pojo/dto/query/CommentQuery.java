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
    @Schema(description = "如果是查询一级评论，就是文章id,否则是父评论id")
    private Long id;
}
