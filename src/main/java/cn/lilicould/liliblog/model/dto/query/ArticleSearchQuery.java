package cn.lilicould.liliblog.model.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "文章搜索参数")
public class ArticleSearchQuery extends BaseQuery implements Serializable {

    @NotBlank(message = "关键字不能为空")
    @Size(min = 2, max = 20, message = "关键字长度必须在2-20之间")
    private String keyword;
}
