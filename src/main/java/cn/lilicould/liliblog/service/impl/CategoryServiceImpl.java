package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.common.constant.OrderConstant;
import cn.lilicould.liliblog.common.constant.StatusConstant;
import cn.lilicould.liliblog.common.context.BaseContext;
import cn.lilicould.liliblog.mapper.CategoryMapper;
import cn.lilicould.liliblog.pojo.dto.query.CategoryQuery;
import cn.lilicould.liliblog.pojo.dto.response.CategoryVO;
import cn.lilicould.liliblog.pojo.dto.response.PageInfo;
import cn.lilicould.liliblog.pojo.entity.Category;
import cn.lilicould.liliblog.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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
        page.setOrders(OrderItem.descs(OrderConstant.SORT_ORDER, OrderConstant.UPDATE_TIME, OrderConstant.CREATE_TIME));
        // 查询
        Page<Category> categoryPage;
        if (BaseContext.isAdmin()) {
            // 管理员
            categoryPage = baseMapper.selectPage(page, null);
        } else {
            // 非管理员只能查到启用的分类
            LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Category::getStatus, StatusConstant.ENABLED);
            categoryPage = baseMapper.selectPage(page, queryWrapper);
        }

        // 如果是空
        if (categoryPage.getTotal() == 0) {
            return PageInfo.empty(categoryQuery.getCurrent(), categoryQuery.getSize());
        }

        // 转换为VO
        List<CategoryVO> categoryVOList = categoryPage.getRecords().stream().map(category -> {
            CategoryVO categoryVO = new CategoryVO();
            categoryVO.setId(category.getId());
            categoryVO.setName(category.getName());
            categoryVO.setSlug(category.getSlug());
            categoryVO.setDescription(category.getDescription());
            categoryVO.setSortOrder(category.getSortOrder());
            return categoryVO;
        }).toList();

        Page<CategoryVO> voPage = new Page<>(categoryPage.getCurrent(), categoryPage.getSize(), categoryPage.getTotal());
        voPage.setRecords(categoryVOList);
        return PageInfo.of(voPage);
    }
}




