package cn.lilicould.liliblog.util;


import cn.lilicould.liliblog.query.BaseQuery;

public class PageUtil {
    public static <T extends BaseQuery> T setDefault(T query) {
        if (query.getCurrent() == null) {
            query.setCurrent(1L);
        }
        if (query.getSize() == null) {
            query.setSize(10L);
        }
        return query;
    }
}
