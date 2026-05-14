package cn.lilicould.liliblog.mapper;

import cn.lilicould.liliblog.pojo.entity.Tag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author Lili_Could
* @description 针对表【tag(标签表)】的数据库操作Mapper
* @createDate 2026-05-08 16:58:41
* @Entity cn.lilicould.entity.Tag
*/
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 根据文章ID查询标签列表
     * @param articleId 文章ID
     * @return 标签列表
     */
    List<Tag> selectTagsByArticleId(Long articleId);
}




