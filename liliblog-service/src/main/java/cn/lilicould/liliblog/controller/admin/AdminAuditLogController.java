package cn.lilicould.liliblog.controller.admin;

import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.exception.BusinessException;
import cn.lilicould.liliblog.result.Result;
import cn.lilicould.liliblog.util.PageUtil;
import cn.lilicould.liliblog.query.AuditQuery;
import cn.lilicould.liliblog.response.AuditLogVO;
import cn.lilicould.liliblog.response.PageInfo;
import cn.lilicould.liliblog.entity.AuditLog;
import cn.lilicould.liliblog.service.AuditLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/auditLog")
@RequiredArgsConstructor
@Tag(name = "后台-审计日志")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditLogController {
    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "获取审计日志列表")
    public Result<PageInfo<AuditLogVO>> getAuditLogs(@ParameterObject AuditQuery auditQuery) {

        // 设置分页默认值
        PageUtil.setDefault(auditQuery);

        PageInfo<AuditLogVO> auditLogVOPage = auditLogService.getAuditLogs(auditQuery);

        return Result.success(auditLogVOPage);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除审计日志,根据ID")
    public Result<?> deleteAuditLog(@PathVariable Long id) {
        if (!auditLogService.exists(new LambdaQueryWrapper<AuditLog>().eq(AuditLog::getId, id))) {
            throw new BusinessException(CodeEnum.RESOURCE_NOT_FOUND);
        }

        // 删除日志
        auditLogService.removeById(id);

        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "批量删除审计日志")
    public Result<?> deleteAuditLogs(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(CodeEnum.PARAM_MISSING);
        }
        // 批量删除日志
        auditLogService.removeByIds(ids);

        return Result.success();
    }

}
