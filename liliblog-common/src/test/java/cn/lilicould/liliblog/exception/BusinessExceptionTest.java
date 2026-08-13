package cn.lilicould.liliblog.exception;

import cn.lilicould.liliblog.enums.CodeEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * BusinessException业务异常测试类
 *
 * @author lilicould
 */
class BusinessExceptionTest {

    @Test
    void constructWithCodeAndMessage() {
        BusinessException e = new BusinessException(4001, "参数异常");

        assertEquals(4001, e.getCode());
        assertEquals("参数异常", e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    void constructWithCodeMessageAndCause() {
        Throwable cause = new RuntimeException("root");
        BusinessException e = new BusinessException(5002, "文件上传失败", cause);

        assertEquals(5002, e.getCode());
        assertEquals("文件上传失败", e.getMessage());
        assertSame(cause, e.getCause());
    }

    @Test
    void constructWithCodeEnum() {
        BusinessException e = new BusinessException(CodeEnum.TOKEN_EXPIRED);

        assertEquals(CodeEnum.TOKEN_EXPIRED.getCode(), e.getCode());
        assertEquals(CodeEnum.TOKEN_EXPIRED.getMessage(), e.getMessage());
    }
}