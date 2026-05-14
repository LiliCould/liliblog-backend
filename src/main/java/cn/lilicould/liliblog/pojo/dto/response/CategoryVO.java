package cn.lilicould.liliblog.pojo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "分类展示对象")
public class CategoryVO implements Serializable {

    @Schema(description = "分类id")
    private Long id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类别名")
    private String slug;

    @Schema(description = "分类描述")
    private String description;

    @Schema(description = "排序字段，约定越小等级越高")
    private Integer sortOrder;
}
