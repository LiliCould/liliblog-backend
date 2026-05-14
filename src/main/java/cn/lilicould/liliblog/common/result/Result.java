package cn.lilicould.liliblog.common.result;

import cn.lilicould.liliblog.common.enums.CodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "通用返回结果")
@Data
public class Result<T> implements Serializable {

    @Schema(description = "状态码，0表示成功", example = "0")
    private Integer code; // 0代表成功，其他数字代表失败
    @Schema(description = "响应信息/错误信息",example = "成功")
    private String msg; //错误信息
    @Schema(description = "数据")
    private T data; //数据
    @Schema(description = "响应时间")
    private LocalDateTime time;

    public static <T> Result<T> success() {
        return Result.success(null);
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<>();
        result.data = object;
        result.code = CodeEnum.SUCCESS.getCode();
        result.msg = CodeEnum.SUCCESS.getMessage();
        result.time = LocalDateTime.now();
        return result;
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.msg = msg;
        result.code = code;
        result.data = null;
        result.time = LocalDateTime.now();
        return result;
    }

    /**
     * 错误信息
     * @param codeEnum 状态枚举
     */
    public static <T> Result<T> error(CodeEnum codeEnum) {
        return error(codeEnum.getCode(), codeEnum.getMessage());
    }
}
