package cn.lilicould.liliblog.pojo.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "分类查询参数")
public class CategoryQuery extends BaseQuery implements Serializable {

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类别名,精准查询,可以由唯一slug指定单个分类", example = "the-use-method-of-aop")
    private String slug;

    @Schema(description = "分类描述")
    private String description;
}
