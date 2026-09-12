package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.constant.StatusConstant;
import cn.lilicould.liliblog.context.BaseContext;
import cn.lilicould.liliblog.domain.security.SecurityUser;
import cn.lilicould.liliblog.entity.Category;
import cn.lilicould.liliblog.entity.User;
import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.exception.BusinessException;
import cn.lilicould.liliblog.mapper.CategoryMapper;
import cn.lilicould.liliblog.query.CategoryQuery;
import cn.lilicould.liliblog.request.CategoryCreateRequest;
import cn.lilicould.liliblog.request.CategoryUpdateRequest;
import cn.lilicould.liliblog.response.CategoryVO;
import cn.lilicould.liliblog.response.PageInfo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * CategoryServiceImpl测试类
 *
 * @author lilicould
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CategoryServiceImplTest {

    @Mock
    private CategoryMapper categoryMapper;

    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl();
        ReflectionTestUtils.setField(categoryService, "baseMapper", categoryMapper);
    }

    private void loginAs(Long userId, Integer role) {
        User user = User.builder().id(userId).username("u" + userId).role(role).status(1).build();
        SecurityUser securityUser = new SecurityUser(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Category buildCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("生活");
        category.setSlug("life");
        category.setDescription("生活趣事");
        category.setSortOrder(1);
        category.setStatus(StatusConstant.ENABLED);
        return category;
    }

    private CategoryQuery buildQuery() {
        CategoryQuery query = new CategoryQuery();
        query.setCurrent(1L);
        query.setSize(10L);
        return query;
    }

    @Test
    void getCategoryListEmptyReturnsEmptyPageInfoAsAdmin() {
        loginAs(1L, 0);
        when(categoryMapper.selectPage(any(Page.class), any(Wrapper.class)))
                .thenReturn(new Page<>(1, 10, 0));

        PageInfo<CategoryVO> result = categoryService.getCategoryList(buildQuery());

        assertEquals(0L, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    void getCategoryListSuccessAsAdmin() {
        loginAs(1L, 0);
        Category category = buildCategory();
        Page<Category> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(category));
        when(categoryMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        PageInfo<CategoryVO> result = categoryService.getCategoryList(buildQuery());

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getRecords().size());
        CategoryVO vo = result.getRecords().get(0);
        assertEquals("生活", vo.getName());
        assertEquals("life", vo.getSlug());
        assertEquals(StatusConstant.ENABLED, vo.getStatus());
    }

    @Test
    void getCategoryListSuccessAsNormalUser() {
        loginAs(2L, 1);
        Category category = buildCategory();
        Page<Category> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(category));
        when(categoryMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        PageInfo<CategoryVO> result = categoryService.getCategoryList(buildQuery());

        assertEquals(1, result.getRecords().size());
        assertEquals("生活", result.getRecords().get(0).getName());
    }

    @Test
    void updateWhenCategoryNotExistThrowsCategoryNotFound() {
        loginAs(1L, 0);
        when(categoryMapper.exists(any(Wrapper.class))).thenReturn(false);
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("新分类");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> categoryService.update(1L, request));
        assertEquals(CodeEnum.CATEGORY_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void updateWhenSlugAlreadyExistsThrowsSlugAlreadyExists() {
        loginAs(1L, 0);
        when(categoryMapper.exists(any(Wrapper.class))).thenReturn(true, true);
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setSlug("dup");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> categoryService.update(1L, request));
        assertEquals(CodeEnum.SLUG_ALREADY_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void updateSuccess() {
        loginAs(1L, 0);
        when(categoryMapper.exists(any(Wrapper.class))).thenReturn(true, false);
        when(categoryMapper.update(any(Category.class), any(Wrapper.class))).thenReturn(1);
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName("新分类");
        request.setSlug("new-life");

        categoryService.update(1L, request);

        verify(categoryMapper).update(any(Category.class), any(Wrapper.class));
    }

    @Test
    void saveWhenSlugExistsThrowsCategoryAlreadyExists() {
        loginAs(1L, 0);
        when(categoryMapper.exists(any(Wrapper.class))).thenReturn(true);
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("生活");
        request.setSlug("life");

        BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.save(request));
        assertEquals(CodeEnum.CATEGORY_ALREADY_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void saveWhenNameExistsThrowsCategoryAlreadyExists() {
        loginAs(1L, 0);
        when(categoryMapper.exists(any(Wrapper.class))).thenReturn(false, true);
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("生活");
        request.setSlug("new-life");

        BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.save(request));
        assertEquals(CodeEnum.CATEGORY_ALREADY_EXISTS.getCode(), ex.getCode());
    }

    @Test
    void saveSuccessInsertsEnabledCategory() {
        loginAs(1L, 0);
        when(categoryMapper.exists(any(Wrapper.class))).thenReturn(false, false);
        when(categoryMapper.insert(any(Category.class))).thenReturn(1);
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("生活");
        request.setSlug("life");
        request.setDescription("描述");
        request.setSortOrder(2);

        categoryService.save(request);

        org.mockito.ArgumentCaptor<Category> captor = org.mockito.ArgumentCaptor.forClass(Category.class);
        verify(categoryMapper).insert(captor.capture());
        assertEquals("生活", captor.getValue().getName());
        assertEquals(StatusConstant.ENABLED, captor.getValue().getStatus());
        assertEquals(2, captor.getValue().getSortOrder());
    }

    @Test
    void removeWhenNotExistThrowsCategoryNotFound() {
        loginAs(1L, 0);
        when(categoryMapper.selectById(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.remove(1L));
        assertEquals(CodeEnum.CATEGORY_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void removeSuccess() {
        loginAs(1L, 0);
        when(categoryMapper.selectById(1L)).thenReturn(buildCategory());
        when(categoryMapper.deleteById(1L)).thenReturn(1);

        categoryService.remove(1L);

        verify(categoryMapper).deleteById(1L);
    }

    @Test
    void removeBatchWhenEmptyThrowsParamMissing() {
        loginAs(1L, 0);
        BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.remove(List.of()));
        assertEquals(CodeEnum.PARAM_MISSING.getCode(), ex.getCode());
    }

    @Test
    void removeBatchSuccess() {
        loginAs(1L, 0);
        when(categoryMapper.deleteByIds(List.of(1L, 2L))).thenReturn(2);

        categoryService.remove(List.of(1L, 2L));

        verify(categoryMapper).deleteByIds(List.of(1L, 2L));
    }
}