package cn.lilicould.liliblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.lilicould.liliblog.model.entity.AuditLog;
import cn.lilicould.liliblog.service.AuditLogService;
import cn.lilicould.liliblog.mapper.AuditLogMapper;
import org.springframework.stereotype.Service;

/**
* @author Lili_Could
* @description 针对表【audit_log(操作审计日志表)】的数据库操作Service实现
* @createDate 2026-05-25 09:59:05
*/
@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog>
    implements AuditLogService{

}




