package cn.lilicould.liliblog.service;

import cn.lilicould.liliblog.pojo.dto.query.CategoryQuery;
import cn.lilicould.liliblog.pojo.dto.request.CategoryUpdateRequest;
import cn.lilicould.liliblog.pojo.dto.response.CategoryVO;
import cn.lilicould.liliblog.pojo.dto.response.PageInfo;
import cn.lilicould.liliblog.pojo.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;

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
     * @return 分类
     */
    void update(Long id, CategoryUpdateRequest categoryCreateRequest);
}
