package cn.lilicould.liliblog.pojo.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "标签查询参数")
public class TagQuery extends BaseQuery implements Serializable {
    @Schema(description = "标签名称")
    private String name;
}
