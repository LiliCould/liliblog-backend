package cn.lilicould.liliblog.config.mybatis;

import cn.lilicould.liliblog.common.constant.StatusConstant;
import cn.lilicould.liliblog.common.context.BaseContext;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BaseMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 仅当子类存在该字段时才填充
        if (metaObject.hasSetter("createTime")) {
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        }
        if (metaObject.hasSetter("createBy")) {
            Long currentUserId = BaseContext.getCurrentUserId();
            this.strictInsertFill(metaObject, "createBy", Long.class, currentUserId);
        }
        if (metaObject.hasSetter("updateTime")) {
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }
        if (metaObject.hasSetter("updateBy")) {
            Long currentUserId = BaseContext.getCurrentUserId();
            this.strictInsertFill(metaObject, "updateBy", Long.class, currentUserId);
        }
        if (metaObject.hasSetter("deleted")) {
            this.strictInsertFill(metaObject, "deleted", Integer.class, StatusConstant.NOT_DELETED);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 只填充更新字段
        if (metaObject.hasSetter("updateTime")) {
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }
        if (metaObject.hasSetter("updateBy")) {
            Long currentUserId = BaseContext.getCurrentUserId();
            this.strictUpdateFill(metaObject, "updateBy", Long.class, currentUserId);
        }
    }
}
