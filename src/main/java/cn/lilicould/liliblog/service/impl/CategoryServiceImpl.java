package cn.lilicould.liliblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.lilicould.liliblog.entity.Category;
import cn.lilicould.liliblog.service.CategoryService;
import cn.lilicould.liliblog.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

/**
* @author Lili_Could
* @description 针对表【category(分类表)】的数据库操作Service实现
* @createDate 2026-05-08 16:58:41
*/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService{

}




