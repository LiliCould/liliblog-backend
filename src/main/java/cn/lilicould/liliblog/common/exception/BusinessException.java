package cn.lilicould.liliblog.common.exception;

import cn.lilicould.liliblog.common.enums.CodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true) // 继承父类的equals和hashCode方法
@Data
public class BusinessException extends RuntimeException {
    private Integer code;          // 错误码
    private String message;       // 错误信息

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public BusinessException(CodeEnum codeEnum) {
        super(codeEnum.getMessage());
        this.code = codeEnum.getCode();
        this.message = codeEnum.getMessage();
    }
}
