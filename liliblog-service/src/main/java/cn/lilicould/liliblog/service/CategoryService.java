package cn.lilicould.liliblog.service;

import cn.lilicould.query.CategoryQuery;
import cn.lilicould.request.CategoryCreateRequest;
import cn.lilicould.request.CategoryUpdateRequest;
import cn.lilicould.response.CategoryVO;
import cn.lilicould.response.PageInfo;
import cn.lilicould.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Lili_Could
* @description 针对表【category(分类表)】的数据库操作Service
* @createDate 2026-05-08 16:58:41
*/
public interface CategoryService extends IService<Category> {

    /**
     * 获取分类列表
     * @param categoryQuery 查询参数
     * @return 分类列表
     */
    PageInfo<CategoryVO> getCategoryList(CategoryQuery categoryQuery);

    /**
     * 更新分类
     * @param id 分类ID
     * @param categoryCreateRequest 分类参数
     */
    void update(Long id, CategoryUpdateRequest categoryCreateRequest);

    /**
     * 创建分类
     * @param categoryCreateRequest 分类参数
     */
    void save(CategoryCreateRequest categoryCreateRequest);

    /**
     * 删除分类
     * @param id 分类ID
     */
    void remove(Long id);

    /**
     * 批量删除分类
     * @param ids 分类ID列表
     */
    void remove(List<Long> ids);
}
