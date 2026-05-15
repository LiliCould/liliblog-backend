package cn.lilicould.liliblog.mapper;

import cn.lilicould.liliblog.pojo.dto.response.ArticleDetailsVO;
import cn.lilicould.liliblog.pojo.entity.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author Lili_Could
* @description 针对表【article(文章表)】的数据库操作Mapper
* @createDate 2026-05-08 16:58:40
* @Entity cn.lilicould.entity.Article
*/
public interface ArticleMapper extends BaseMapper<Article> {

    ArticleDetailsVO selectArticleVOById(Long id);
}




