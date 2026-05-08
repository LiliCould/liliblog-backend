package cn.lilicould.liliblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.lilicould.liliblog.entity.Comment;
import cn.lilicould.liliblog.service.CommentService;
import cn.lilicould.liliblog.mapper.CommentMapper;
import org.springframework.stereotype.Service;

/**
* @author Lili_Could
* @description 针对表【comment(评论表)】的数据库操作Service实现
* @createDate 2026-05-08 16:58:41
*/
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService{

}




