package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.entity.AuditLog;
import cn.lilicould.liliblog.mapper.AuditLogMapper;
import cn.lilicould.liliblog.query.AuditQuery;
import cn.lilicould.liliblog.response.AuditLogVO;
import cn.lilicould.liliblog.response.PageInfo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * AuditLogServiceImpl测试类
 *
 * @author lilicould
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogMapper auditLogMapper;

    private AuditLogServiceImpl auditLogService;

    @BeforeEach
    void setUp() {
        auditLogService = new AuditLogServiceImpl(auditLogMapper);
        ReflectionTestUtils.setField(auditLogService, "baseMapper", auditLogMapper);
    }

    private AuditLog buildAuditLog() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setUsername("admin");
        log.setModule("article");
        log.setOperation("DELETE");
        log.setTarget("1");
        log.setTargetType("ARTICLE");
        log.setStatus(1);
        log.setIpAddress("127.0.0.1");
        log.setRequestUri("/api/article/1");
        log.setCreateTime(LocalDateTime.of(2026, 5, 25, 10, 0));
        return log;
    }

    @Test
    void getAuditLogsWithEmptyResultReturnsEmptyPageInfo() {
        AuditQuery query = new AuditQuery();
        query.setCurrent(1L);
        query.setSize(10L);
        when(auditLogMapper.selectPage(any(Page.class), any(Wrapper.class)))
                .thenReturn(new Page<>(1, 10, 0));

        PageInfo<AuditLogVO> result = auditLogService.getAuditLogs(query);

        assertEquals(0L, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    void getAuditLogsSuccessConvertsToVO() {
        AuditQuery query = new AuditQuery();
        query.setCurrent(1L);
        query.setSize(10L);
        query.setUsername("admin");
        query.setModule("article");
        query.setOperation("DELETE");
        query.setTarget(1L);
        query.setTargetType("ARTICLE");
        query.setStatus(1);
        query.setIpAddress("127.0.0.1");
        query.setRequestUri("/api/article");
        query.setStartTime(LocalDateTime.of(2026, 5, 1, 0, 0));
        query.setEndTime(LocalDateTime.of(2026, 5, 31, 23, 59));
        Page<AuditLog> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(buildAuditLog()));
        when(auditLogMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        PageInfo<AuditLogVO> result = auditLogService.getAuditLogs(query);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getRecords().size());
        AuditLogVO vo = result.getRecords().get(0);
        assertEquals("admin", vo.getUsername());
        assertEquals("article", vo.getModule());
        assertEquals("DELETE", vo.getOperation());
        assertEquals(1, vo.getStatus());
        assertEquals("127.0.0.1", vo.getIpAddress());
    }
}