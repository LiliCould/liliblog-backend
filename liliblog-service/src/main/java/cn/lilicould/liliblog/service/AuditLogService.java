package cn.lilicould.liliblog.service;

import cn.lilicould.liliblog.query.AuditQuery;
import cn.lilicould.liliblog.response.AuditLogVO;
import cn.lilicould.liliblog.response.PageInfo;
import cn.lilicould.liliblog.entity.AuditLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Lili_Could
* @description 针对表【audit_log(操作审计日志表)】的数据库操作Service
* @createDate 2026-05-25 09:59:05
*/
public interface AuditLogService extends IService<AuditLog> {


    /**
     * 获取操作审计日志
     * @param auditQuery 查询参数
     * @return 操作审计日志
     */
    PageInfo<AuditLogVO> getAuditLogs(AuditQuery auditQuery);
}
