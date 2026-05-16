package cn.lilicould.liliblog.mapper;

import cn.lilicould.liliblog.pojo.entity.ArticleTag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;

/**
* @author Lili_Could
* @description 针对表【article_tag(文章标签关联表)】的数据库操作Mapper
* @createDate 2026-05-08 16:58:40
* @Entity cn.lilicould.entity.ArticleTag
*/
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {

    @Delete("DELETE FROM article_tag WHERE article_id NOT IN (SELECT id FROM article)")
    Long clean();
}




