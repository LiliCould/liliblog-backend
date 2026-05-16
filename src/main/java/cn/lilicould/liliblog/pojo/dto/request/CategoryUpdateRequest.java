package cn.lilicould.liliblog.pojo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(name = "新建分类参数")
public class CategoryUpdateRequest implements Serializable {

    @Size(min = 2, max = 30, message = "分类名长度必须在2-30个字符之间")
    @Schema(description = "分类名",example = "生活趣事")
    private String name;

    @Schema(description = "分类别名",example = "life")
    private String slug;

    @Schema(description = "描述",example = "分享生活小事")
    private String description;

    @Schema(description = "排序字段，数值越小优先级越高",example = "1")
    private Integer sortOrder;
}
