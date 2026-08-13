package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.entity.Tag;
import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.exception.BusinessException;
import cn.lilicould.liliblog.mapper.TagMapper;
import cn.lilicould.liliblog.query.TagQuery;
import cn.lilicould.liliblog.request.TagCreateRequest;
import cn.lilicould.liliblog.request.TagUpdateRequest;
import cn.lilicould.liliblog.response.PageInfo;
import cn.lilicould.liliblog.response.TagVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TagServiceImpl测试类
 *
 * @author lilicould
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TagServiceImplTest {

    @Mock
    private TagMapper tagMapper;

    private TagServiceImpl tagService;

    @BeforeEach
    void setUp() {
        tagService = new TagServiceImpl();
        ReflectionTestUtils.setField(tagService, "baseMapper", tagMapper);
    }

    private Tag buildTag() {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("随笔");
        tag.setColor("#ff0000");
        tag.setCreateTime(LocalDateTime.of(2026, 5, 8, 10, 0));
        return tag;
    }

    @Test
    void getTagListSuccess() {
        TagQuery query = new TagQuery();
        query.setCurrent(1L);
        query.setSize(10L);
        query.setName("随");
        Tag tag = buildTag();
        Page<Tag> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(tag));
        when(tagMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        PageInfo<TagVO> result = tagService.getTagList(query);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getRecords().size());
        TagVO vo = result.getRecords().get(0);
        assertEquals("随笔", vo.getName());
        assertEquals("#ff0000", vo.getColor());
    }

    @Test
    void getTagListWithEmptyResult() {
        TagQuery query = new TagQuery();
        query.setCurrent(1L);
        query.setSize(10L);
        when(tagMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(new Page<>(1, 10, 0));

        PageInfo<TagVO> result = tagService.getTagList(query);

        assertEquals(0L, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    void saveWhenNameExistsThrowsTagAlreadyExists() {
        when(tagMapper.exists(any(Wrapper.class))).thenReturn(true);
        TagCreateRequest request = new TagCreateRequest();
        request.setName("随笔");
        request.setColor("#ffffff");

        BusinessException ex = assertThrows(BusinessException.class, () -> tagService.save(request));
        assertEquals(CodeEnum.TAG_ALREADY_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void saveSuccess() {
        when(tagMapper.exists(any(Wrapper.class))).thenReturn(false);
        when(tagMapper.insert(any(Tag.class))).thenReturn(1);
        TagCreateRequest request = new TagCreateRequest();
        request.setName("新标签");
        request.setColor("#abcdef");

        tagService.save(request);

        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        verify(tagMapper).insert(captor.capture());
        assertEquals("新标签", captor.getValue().getName());
        assertEquals("#abcdef", captor.getValue().getColor());
    }

    @Test
    void updateWhenTagNotExistThrowsTagNotFound() {
        when(tagMapper.exists(any(Wrapper.class))).thenReturn(false);
        TagUpdateRequest request = new TagUpdateRequest();
        request.setName("随笔");

        BusinessException ex = assertThrows(BusinessException.class, () -> tagService.update(1L, request));
        assertEquals(CodeEnum.TAG_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void updateWhenNameUsedByOtherTagThrowsTagAlreadyExists() {
        when(tagMapper.exists(any(Wrapper.class))).thenReturn(true, true);
        TagUpdateRequest request = new TagUpdateRequest();
        request.setName("重复");

        BusinessException ex = assertThrows(BusinessException.class, () -> tagService.update(1L, request));
        assertEquals(CodeEnum.TAG_ALREADY_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void updateSuccess() {
        when(tagMapper.exists(any(Wrapper.class))).thenReturn(true, false);
        when(tagMapper.updateById(any(Tag.class))).thenReturn(1);
        TagUpdateRequest request = new TagUpdateRequest();
        request.setName("新随笔");
        request.setColor("#00ff00");

        tagService.update(1L, request);

        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        verify(tagMapper).updateById(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals("新随笔", captor.getValue().getName());
    }

    @Test
    void deleteWhenTagNotExistThrowsTagNotFound() {
        when(tagMapper.exists(any(Wrapper.class))).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> tagService.delete(1L));
        assertEquals(CodeEnum.TAG_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void deleteSuccess() {
        when(tagMapper.exists(any(Wrapper.class))).thenReturn(true);
        when(tagMapper.deleteById(1L)).thenReturn(1);

        tagService.delete(1L);

        verify(tagMapper).deleteById(1L);
    }

    @Test
    void deleteBatchWhenEmptyThrowsParamMissing() {
        BusinessException ex = assertThrows(BusinessException.class, () -> tagService.delete(List.of()));
        assertEquals(CodeEnum.PARAM_MISSING.getCode(), ex.getCode());
    }

    @Test
    void deleteBatchSuccess() {
        when(tagMapper.deleteByIds(List.of(1L, 2L))).thenReturn(2);

        tagService.delete(List.of(1L, 2L));

        verify(tagMapper).deleteByIds(List.of(1L, 2L));
    }
}