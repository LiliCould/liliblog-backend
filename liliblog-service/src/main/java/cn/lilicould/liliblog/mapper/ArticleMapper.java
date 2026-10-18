package cn.lilicould.liliblog.mapper;

import cn.lilicould.liliblog.entity.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
* @author Lili_Could
* @description 针对表【article(文章表)】的数据库操作Mapper
* @createDate 2026-05-08 16:58:40
* @Entity cn.lilicould.entity.Article
*/
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 阅读量 +1。
     * 只更新 view_count 一列，避免像 updateById 那样把 content/content_html 两个 longtext 回写；
     * 显式把 update_time 赋回原值，抑制 ON UPDATE CURRENT_TIMESTAMP 刷新更新时间。
     *
     * @param id 文章ID
     * @return 受影响行数
     */
    @Update("UPDATE article SET view_count = view_count + 1, update_time = update_time " +
            "WHERE id = #{id} AND deleted = 0")
    int incrementViewCount(@Param("id") Long id);
}




