package cn.lilicould.liliblog.common.result;

import cn.lilicould.liliblog.common.enums.CodeEnum;

import java.io.Serializable;

public class Result<T> implements Serializable {

    private Integer code; //编码：1成功，0和其它数字为失败
    private String msg; //错误信息
    private T data; //数据

    public static <T> Result<T> success() {
        return Result.success(null);
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 200;
        result.msg = "成功";
        return result;
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.msg = msg;
        result.code = code;
        result.data = null;
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
