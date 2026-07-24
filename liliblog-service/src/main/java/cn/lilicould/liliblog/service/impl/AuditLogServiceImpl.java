package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.mapper.AuditLogMapper;
import cn.lilicould.query.AuditQuery;
import cn.lilicould.response.AuditLogVO;
import cn.lilicould.response.PageInfo;
import cn.lilicould.entity.AuditLog;
import cn.lilicould.liliblog.service.AuditLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Lili_Could
* @description 针对表【audit_log(操作审计日志表)】的数据库操作Service实现
* @createDate 2026-05-25 09:59:05
*/
@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog>
    implements AuditLogService{


    private final AuditLogMapper auditLogMapper;

    public AuditLogServiceImpl(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    /**
     * 获取操作审计日志
     * @param auditQuery 操作审计查询参数
     * @return 操作审计日志
     */
    @Override
    public PageInfo<AuditLogVO> getAuditLogs(AuditQuery auditQuery) {

        // 创建分页对象
        Page<AuditLog> page = Page.of(auditQuery.getCurrent(), auditQuery.getSize());

        // 设置查询条件
        LambdaQueryWrapper<AuditLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(auditQuery.getUsername() != null, AuditLog::getUsername, auditQuery.getUsername())
                .like(auditQuery.getModule() != null, AuditLog::getModule, auditQuery.getModule())
                .like(auditQuery.getOperation() != null, AuditLog::getOperation, auditQuery.getOperation())
                .like(auditQuery.getTarget() != null, AuditLog::getTarget, auditQuery.getTarget())
                .like(auditQuery.getTargetType() != null, AuditLog::getTargetType, auditQuery.getTargetType())
                .eq(auditQuery.getStatus() != null, AuditLog::getStatus, auditQuery.getStatus())
                .like(auditQuery.getIpAddress() != null, AuditLog::getIpAddress, auditQuery.getIpAddress())
                .like(auditQuery.getRequestUri() != null, AuditLog::getRequestUri, auditQuery.getRequestUri())
                .ge(auditQuery.getStartTime() != null, AuditLog::getCreateTime, auditQuery.getStartTime())
                .le(auditQuery.getEndTime() != null, AuditLog::getCreateTime, auditQuery.getEndTime());

        // 查询
        page = auditLogMapper.selectPage(page, queryWrapper);

        if (!(page.getTotal() > 0)) {
            return PageInfo.empty(auditQuery.getCurrent(), auditQuery.getSize());
        }

        List<AuditLogVO> records = page.getRecords().stream().map(auditLog -> {
            AuditLogVO auditLogVO = new AuditLogVO();
            BeanUtils.copyProperties(auditLog, auditLogVO);
            return auditLogVO;
        }).toList();

        // 构建返回结果
        Page<AuditLogVO> auditLogPage = new Page<>(page.getCurrent(), page.getSize(),page.getTotal());

        // 设置记录
        auditLogPage.setRecords(records);

        return PageInfo.of(auditLogPage);
    }
}




