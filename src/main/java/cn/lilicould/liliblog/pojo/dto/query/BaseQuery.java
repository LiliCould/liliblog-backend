package cn.lilicould.liliblog.pojo.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(name = "通用分页查询参数")
public class BaseQuery implements Serializable {
    @Schema(description = "当前页码", example = "1")
    private Long current;

    @Schema(description = "每页数量", example = "10")
    private Long size;
}
