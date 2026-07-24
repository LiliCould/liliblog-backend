package cn.lilicould.liliblog.service.impl;

import cn.lilicould.annotation.Audit;
import cn.lilicould.constant.OrderConstant;
import cn.lilicould.constant.StatusConstant;
import cn.lilicould.liliblog.context.BaseContext;
import cn.lilicould.enums.CodeEnum;
import cn.lilicould.exception.BusinessException;
import cn.lilicould.liliblog.mapper.CategoryMapper;
import cn.lilicould.query.CategoryQuery;
import cn.lilicould.request.CategoryCreateRequest;
import cn.lilicould.request.CategoryUpdateRequest;
import cn.lilicould.response.CategoryVO;
import cn.lilicould.response.PageInfo;
import cn.lilicould.entity.Category;
import cn.lilicould.liliblog.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author Lili_Could
* @description 针对表【category(分类表)】的数据库操作Service实现
* @createDate 2026-05-08 16:58:41
*/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService{

    /**
     * 获取分类列表
     * @param categoryQuery 查询参数
     * @return 分类列表
     */
    @Override
    public PageInfo<CategoryVO> getCategoryList(CategoryQuery categoryQuery) {
        // 创建分页对象
        Page<Category> page = Page.of(categoryQuery.getCurrent(), categoryQuery.getSize());
        // 设置排序字段
        page.setOrders(List.of(
                OrderItem.asc(OrderConstant.SORT_ORDER),
                OrderItem.desc(OrderConstant.CREATE_TIME),
                OrderItem.desc(OrderConstant.UPDATE_TIME)
        ));
        // 创建查询条件
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(categoryQuery.getName() != null, Category::getName, categoryQuery.getName())
                .eq(categoryQuery.getSlug() != null, Category::getSlug, categoryQuery.getSlug())
                .like(categoryQuery.getDescription() != null, Category::getDescription, categoryQuery.getDescription())
                .eq(categoryQuery.getStatus() != null, Category::getStatus, categoryQuery.getStatus())
                .ge(categoryQuery.getStartTime() != null, Category::getCreateTime, categoryQuery.getStartTime())
                .le(categoryQuery.getEndTime() != null, Category::getCreateTime, categoryQuery.getEndTime());
        if (!BaseContext.isAdmin()) {
            queryWrapper.eq(Category::getStatus, StatusConstant.ENABLED); // 如果不是管理员只能查到启用的分类
        }
        // 查询
        Page<Category> categoryPage = baseMapper.selectPage(page, queryWrapper);

        // 如果是空
        if (categoryPage.getTotal() == 0) {
            return PageInfo.empty(categoryQuery.getCurrent(), categoryQuery.getSize());
        }

        // 转换为VO
        List<CategoryVO> categoryVOList = categoryPage.getRecords().stream().map(category -> {
            CategoryVO categoryVO = new CategoryVO();
            categoryVO.setId(category.getId());
            categoryVO.setName(category.getName());
            categoryVO.setStatus(category.getStatus());
            categoryVO.setSlug(category.getSlug());
            categoryVO.setDescription(category.getDescription());
            categoryVO.setSortOrder(category.getSortOrder());
            categoryVO.setCreateTime(category.getCreateTime());
            return categoryVO;
        }).toList();

        Page<CategoryVO> voPage = new Page<>(categoryPage.getCurrent(), categoryPage.getSize(), categoryPage.getTotal());
        voPage.setRecords(categoryVOList);
        return PageInfo.of(voPage);
    }

    /**
     * 修改分类
     * @param id 分类ID
     * @param categoryCreateRequest 修改参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class,isolation = Isolation.READ_COMMITTED)
    @Audit(
            module = "category",
            operation = "UPDATE",
            description = "'更新分类:' + #id",
            targetType = "CATEGORY",
            target = "#id"
    )
    public void update(Long id, CategoryUpdateRequest categoryCreateRequest) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryCreateRequest, category);
        category.setId(id);
        // 检查分类是否存在
        if (!this.exists(new LambdaQueryWrapper<Category>().eq(Category::getId, id))) {
            throw new BusinessException(CodeEnum.CATEGORY_NOT_FOUND);
        }

        // 检查其他分类是否已经使用了别名
        if (this.exists(new LambdaQueryWrapper<Category>().eq(Category::getSlug, category.getSlug()).ne(Category::getId, id))) {
            throw new BusinessException(CodeEnum.SLUG_ALREADY_EXISTS);
        }

        // 更新分类，空值不更新
        this.update(category, new LambdaQueryWrapper<Category>().eq(Category::getId, id));
    }

    /**
     * 更新分类
     * @param categoryCreateRequest 分类参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class,isolation = Isolation.READ_COMMITTED)
    @Audit(
            module = "category",
            operation = "UPDATE",
            description = "'新增分类:' + #categoryCreateRequest.getName()",
            targetType = "CATEGORY",
            target = "#categoryCreateRequest.getName()"
    )
    public void save(CategoryCreateRequest categoryCreateRequest) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryCreateRequest, category);
        category.setStatus(StatusConstant.ENABLED);

        // 检查别名和分类名是否已存在
        if (this.exists(new LambdaQueryWrapper<Category>().eq(Category::getSlug, category.getSlug()))
                || this.exists(new LambdaQueryWrapper<Category>().eq(Category::getName, category.getName())) ) {
            throw new BusinessException(CodeEnum.CATEGORY_ALREADY_EXISTS);
        }

        this.save(category);
    }

    /**
     * 删除分类
     * @param id 分类ID
     */
    @Override
    @Audit(
            module = "category",
            operation = "DELETE",
            description = "'删除分类:' + #id",
            targetType = "CATEGORY",
            target = "#id"
    )
    public void remove(Long id) {
        Category category = this.getById(id);

        if (category == null) {
            throw new BusinessException(CodeEnum.CATEGORY_NOT_FOUND);
        }
        this.removeById(id);
    }

    /**
     * 批量删除分类
     * @param ids 分类ID列表
     */
    @Override
    @Audit(
            module = "category",
            operation = "DELETE",
            description = "'删除分类:' + #ids.size()",
            targetType = "CATEGORY",
            target = "#ids"
    )
    public void remove(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(CodeEnum.PARAM_MISSING);
        }

        this.removeByIds(ids);
    }
}




