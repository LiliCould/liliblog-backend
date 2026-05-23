package cn.lilicould.liliblog.task;

import cn.lilicould.liliblog.mapper.ArticleTagMapper;
import cn.lilicould.liliblog.mapper.CommentMapper;
import cn.lilicould.liliblog.mapper.LikeRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务,清理数据库中的垃圾数据
 */
@Component
@Slf4j
public class CleanTask {

    private final ArticleTagMapper articleTagMapper;
    private final CommentMapper commentMapper;
    private final LikeRecordMapper likeRecordMapper;

    public CleanTask(ArticleTagMapper articleTagMapper, CommentMapper commentMapper, LikeRecordMapper likeRecordMapper) {
        this.articleTagMapper = articleTagMapper;
        this.commentMapper = commentMapper;
        this.likeRecordMapper = likeRecordMapper;
    }

    @Scheduled(cron = "0 0 2 * * *") // 每天0点执行
    public void clean() {

        log.info("开始清理数据库中的垃圾数据");

        log.info("清理文章标签关联表完成,共清除{}条数据",articleTagMapper.clean());
        log.info("清理已删除文章的评论完成，共清除{}条数据", commentMapper.clean());
        log.info("清理已删除文章或评论的点赞记录表，共清除{}条数据", likeRecordMapper.clean());

    }
}
