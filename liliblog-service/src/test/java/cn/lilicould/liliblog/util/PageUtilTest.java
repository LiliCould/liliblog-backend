package cn.lilicould.liliblog.util;

import cn.lilicould.liliblog.query.BaseQuery;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * PageUtil分页工具测试类
 *
 * @author lilicould
 */
class PageUtilTest {

    @Test
    void setDefaultFillsNullCurrentAndSize() {
        BaseQuery query = new BaseQuery();

        PageUtil.setDefault(query);

        assertEquals(1L, query.getCurrent());
        assertEquals(10L, query.getSize());
    }

    @Test
    void setDefaultKeepsExistingValues() {
        BaseQuery query = new BaseQuery();
        query.setCurrent(3L);
        query.setSize(20L);

        PageUtil.setDefault(query);

        assertEquals(3L, query.getCurrent());
        assertEquals(20L, query.getSize());
    }

    @Test
    void setDefaultFillsOnlyNullField() {
        BaseQuery query = new BaseQuery();
        query.setCurrent(5L);

        PageUtil.setDefault(query);

        assertEquals(5L, query.getCurrent());
        assertEquals(10L, query.getSize());
    }
}