package cn.lilicould.liliblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.lilicould.liliblog.entity.Article;
import cn.lilicould.liliblog.service.ArticleService;
import cn.lilicould.liliblog.mapper.ArticleMapper;
import org.springframework.stereotype.Service;

/**
* @author Lili_Could
* @description 针对表【article(文章表)】的数据库操作Service实现
* @createDate 2026-05-08 16:58:40
*/
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article>
    implements ArticleService{

}




