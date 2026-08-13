package cn.lilicould.liliblog.result;

import cn.lilicould.liliblog.enums.CodeEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Result通用返回结果测试类
 *
 * @author lilicould
 */
class ResultTest {

    @Test
    void successWithoutData() {
        Result<Object> result = Result.success();

        assertEquals(CodeEnum.SUCCESS.getCode(), result.getCode());
        assertEquals(CodeEnum.SUCCESS.getMessage(), result.getMsg());
        assertNull(result.getData());
        assertNotNull(result.getTime());
    }

    @Test
    void successWithData() {
        String data = "hello";
        Result<String> result = Result.success(data);

        assertEquals(CodeEnum.SUCCESS.getCode(), result.getCode());
        assertEquals(CodeEnum.SUCCESS.getMessage(), result.getMsg());
        assertEquals(data, result.getData());
        assertNotNull(result.getTime());
    }

    @Test
    void errorWithCodeAndMsg() {
        Result<String> result = Result.error(5002, "文件上传失败");

        assertEquals(5002, result.getCode());
        assertEquals("文件上传失败", result.getMsg());
        assertNull(result.getData());
        assertNotNull(result.getTime());
    }

    @Test
    void errorWithCodeEnum() {
        Result<String> result = Result.error(CodeEnum.TOKEN_EXPIRED);

        assertEquals(CodeEnum.TOKEN_EXPIRED.getCode(), result.getCode());
        assertEquals(CodeEnum.TOKEN_EXPIRED.getMessage(), result.getMsg());
        assertNull(result.getData());
    }
}