package cn.lilicould.liliblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.lilicould.liliblog.entity.ArticleTag;
import cn.lilicould.liliblog.service.ArticleTagService;
import cn.lilicould.liliblog.mapper.ArticleTagMapper;
import org.springframework.stereotype.Service;

/**
* @author Lili_Could
* @description 针对表【article_tag(文章标签关联表)】的数据库操作Service实现
* @createDate 2026-05-08 16:58:40
*/
@Service
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagMapper, ArticleTag>
    implements ArticleTagService{

}




