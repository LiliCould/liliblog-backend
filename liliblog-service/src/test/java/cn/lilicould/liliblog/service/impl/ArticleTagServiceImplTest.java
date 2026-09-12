package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.entity.ArticleTag;
import cn.lilicould.liliblog.mapper.ArticleTagMapper;
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
 * ArticleTagServiceImpl测试类
 *
 * @author lilicould
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ArticleTagServiceImplTest {

    @Mock
    private ArticleTagMapper articleTagMapper;

    private ArticleTagServiceImpl articleTagService;

    @BeforeEach
    void setUp() {
        articleTagService = new ArticleTagServiceImpl();
        ReflectionTestUtils.setField(articleTagService, "baseMapper", articleTagMapper);
    }

    @Test
    void saveInsertsEntity() {
        ArticleTag articleTag = new ArticleTag();
        articleTag.setArticleId(1L);
        articleTag.setTagId(2L);
        when(articleTagMapper.insert(articleTag)).thenReturn(1);

        boolean result = articleTagService.save(articleTag);

        assertTrue(result);
        verify(articleTagMapper).insert(articleTag);
    }

    @Test
    void getByIdReturnsEntity() {
        ArticleTag articleTag = new ArticleTag();
        articleTag.setId(1L);
        when(articleTagMapper.selectById(1L)).thenReturn(articleTag);

        assertEquals(articleTag, articleTagService.getById(1L));
    }

    @Test
    void getByIdNotExistReturnsNull() {
        when(articleTagMapper.selectById(1L)).thenReturn(null);
        assertNull(articleTagService.getById(1L));
    }

    @Test
    void removeByIdDeletes() {
        when(articleTagMapper.deleteById(1L)).thenReturn(1);
        assertTrue(articleTagService.removeById(1L));
        verify(articleTagMapper).deleteById(1L);
    }

    @Test
    void listReturnsEntities() {
        ArticleTag articleTag = new ArticleTag();
        articleTag.setArticleId(1L);
        when(articleTagMapper.selectList(any(Wrapper.class))).thenReturn(List.of(articleTag));

        List<ArticleTag> result = articleTagService.list(new LambdaQueryWrapper<>());

        assertEquals(1, result.size());
    }

    @Test
    void pageReturnsPagedData() {
        Page<ArticleTag> page = new Page<>(1, 10, 0);
        when(articleTagMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        Page<ArticleTag> result = articleTagService.page(new Page<>(1, 10), new LambdaQueryWrapper<>());

        assertEquals(0L, result.getTotal());
    }

    @Test
    void existsReturnsTrueWhenRecordExists() {
        when(articleTagMapper.exists(any(Wrapper.class))).thenReturn(true);
        assertTrue(articleTagService.exists(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, 1L)));
    }

    @Test
    void existsReturnsFalseWhenNoRecord() {
        when(articleTagMapper.exists(any(Wrapper.class))).thenReturn(false);
        assertFalse(articleTagService.exists(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, 99L)));
    }
}