package cn.lilicould.liliblog.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * CodeEnum错误码枚举测试类
 *
 * @author lilicould
 */
class CodeEnumTest {

    @Test
    void successHasCodeZero() {
        assertEquals(0, CodeEnum.SUCCESS.getCode());
        assertEquals("成功", CodeEnum.SUCCESS.getMessage());
    }

    @Test
    void allEnumValuesHaveValidCodeAndMessage() {
        for (CodeEnum codeEnum : CodeEnum.values()) {
            assertNotNull(codeEnum.getCode());
            assertNotNull(codeEnum.getMessage());
            assertEquals(codeEnum, CodeEnum.valueOf(codeEnum.name()));
        }
    }

    @Test
    void commonParamErrorHasCode1000() {
        assertEquals(1000, CodeEnum.COMMON_PARAM_ERROR.getCode());
        assertEquals("参数异常", CodeEnum.COMMON_PARAM_ERROR.getMessage());
    }

    @Test
    void systemErrorHasCode5000() {
        assertEquals(5000, CodeEnum.SYSTEM_ERROR.getCode());
        assertEquals("系统异常，请稍后重试", CodeEnum.SYSTEM_ERROR.getMessage());
    }
}