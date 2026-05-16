package cn.lilicould.liliblog.pojo.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;

public class CategoryQuery extends BaseQuery{

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类别名,精准查询,可以由唯一slug指定单个分类", example = "the-use-method-of-aop")
    private String slug;

    @Schema(description = "分类描述")
    private String description;
}
