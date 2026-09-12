package cn.lilicould.liliblog.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TargetType目标类型枚举测试类
 *
 * @author lilicould
 */
class TargetTypeTest {

    @Test
    void enumValues() {
        assertEquals(0, TargetType.ARTICLE.getCode());
        assertEquals("文章", TargetType.ARTICLE.getName());
        assertEquals(1, TargetType.COMMENT.getCode());
        assertEquals("评论", TargetType.COMMENT.getName());
        assertEquals(2, TargetType.USER.getCode());
        assertEquals("用户", TargetType.USER.getName());
    }

    @Test
    void countAndOrder() {
        assertEquals(3, TargetType.values().length);
    }
}