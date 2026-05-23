package cn.lilicould.liliblog.mapper;

import cn.lilicould.liliblog.pojo.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;

/**
* @author Lili_Could
* @description 针对表【comment(评论表)】的数据库操作Mapper
* @createDate 2026-05-08 16:58:41
* @Entity cn.lilicould.entity.Comment
*/
public interface CommentMapper extends BaseMapper<Comment> {

    @Delete("DELETE FROM comment WHERE article_id NOT IN (SELECT id FROM article)")
    Long clean();
}




