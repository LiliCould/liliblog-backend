package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.entity.LikeRecord;
import cn.lilicould.liliblog.mapper.LikeRecordMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * LikeRecordServiceImpl测试类
 *
 * @author lilicould
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LikeRecordServiceImplTest {

    @Mock
    private LikeRecordMapper likeRecordMapper;

    private LikeRecordServiceImpl likeRecordService;

    @BeforeEach
    void setUp() {
        likeRecordService = new LikeRecordServiceImpl();
        ReflectionTestUtils.setField(likeRecordService, "baseMapper", likeRecordMapper);
    }

    private LikeRecord buildLikeRecord() {
        LikeRecord record = new LikeRecord();
        record.setId(1L);
        record.setUserId(10L);
        record.setTargetId(100L);
        record.setTargetType(0);
        return record;
    }

    @Test
    void saveInsertsEntity() {
        LikeRecord record = buildLikeRecord();
        when(likeRecordMapper.insert(record)).thenReturn(1);

        boolean result = likeRecordService.save(record);

        assertTrue(result);
        verify(likeRecordMapper).insert(record);
    }

    @Test
    void getByIdReturnsEntity() {
        LikeRecord record = buildLikeRecord();
        when(likeRecordMapper.selectById(1L)).thenReturn(record);

        LikeRecord result = likeRecordService.getById(1L);

        assertEquals(100L, result.getTargetId());
        assertEquals(10L, result.getUserId());
    }

    @Test
    void getByIdNotExistReturnsNull() {
        when(likeRecordMapper.selectById(1L)).thenReturn(null);
        assertNull(likeRecordService.getById(1L));
    }

    @Test
    void removeByIdDeletes() {
        when(likeRecordMapper.deleteById(1L)).thenReturn(1);
        assertTrue(likeRecordService.removeById(1L));
        verify(likeRecordMapper).deleteById(1L);
    }

    @Test
    void removeByWrapperDeletes() {
        when(likeRecordMapper.delete(any(Wrapper.class))).thenReturn(2);
        boolean result = likeRecordService.remove(
                new LambdaQueryWrapper<LikeRecord>().eq(LikeRecord::getTargetId, 100L));
        assertTrue(result);
        verify(likeRecordMapper).delete(any(Wrapper.class));
    }

    @Test
    void pageReturnsPagedData() {
        LikeRecord record = buildLikeRecord();
        Page<LikeRecord> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(record));
        when(likeRecordMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        Page<LikeRecord> result = likeRecordService.page(new Page<>(1, 10), new LambdaQueryWrapper<>());

        assertEquals(1, result.getRecords().size());
    }

    @Test
    void existsChecksRecord() {
        when(likeRecordMapper.exists(any(Wrapper.class))).thenReturn(true);
        assertTrue(likeRecordService.exists(new LambdaQueryWrapper<LikeRecord>().eq(LikeRecord::getUserId, 10L)));
        when(likeRecordMapper.exists(any(Wrapper.class))).thenReturn(false);
        assertFalse(likeRecordService.exists(new LambdaQueryWrapper<LikeRecord>().eq(LikeRecord::getUserId, 99L)));
    }
}