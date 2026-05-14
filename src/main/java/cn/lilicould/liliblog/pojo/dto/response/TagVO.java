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
@Schema(name = "标签信息")
public class TagVO implements Serializable{

    @Schema(description = "标签id")
    private Long id;

    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "标签颜色",defaultValue = "#666666")
    private String color;
}
