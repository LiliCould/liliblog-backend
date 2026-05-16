package cn.lilicould.liliblog.pojo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(name = "新建标签参数")
public class TagCreateRequest implements Serializable {

    @NotBlank
    @Size(min = 1, max = 20, message = "标签名称长度必须在1到20之间")
    @Schema(description = "标签名称", example = "生活")
    private String name;

    @Schema(description = "标签颜色,格式为标准CSS十六进制颜色代码", example = "#000000")
    @Pattern(regexp = "^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$", message = "颜色格式不正确")
    @NotBlank
    private String color;
}
