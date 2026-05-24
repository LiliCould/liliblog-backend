package cn.lilicould.liliblog.task;

import cn.lilicould.liliblog.common.constant.StatusConstant;
import cn.lilicould.liliblog.config.properties.InfoProperties;
import cn.lilicould.liliblog.mapper.ArticleTagMapper;
import cn.lilicould.liliblog.mapper.CommentMapper;
import cn.lilicould.liliblog.mapper.LikeRecordMapper;
import cn.lilicould.liliblog.model.entity.Comment;
import cn.lilicould.liliblog.service.impl.EmailTemplateService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    private final EmailTemplateService emailTemplateService;
    private final InfoProperties infoProperties;

    public CleanTask(ArticleTagMapper articleTagMapper, CommentMapper commentMapper, LikeRecordMapper likeRecordMapper, EmailTemplateService emailTemplateService, InfoProperties infoProperties) {
        this.articleTagMapper = articleTagMapper;
        this.commentMapper = commentMapper;
        this.likeRecordMapper = likeRecordMapper;
        this.emailTemplateService = emailTemplateService;
        this.infoProperties = infoProperties;
    }

    @Scheduled(cron = "0 0 2 * * *") // 每天0点执行
    public void clean() {

        log.info("开始清理数据库中的垃圾数据");

        log.info("清理文章标签关联表完成,共清除{}条数据",articleTagMapper.clean());
        log.info("清理已删除文章的评论完成，共清除{}条数据", commentMapper.clean());
        log.info("清理已删除文章或评论的点赞记录表，共清除{}条数据", likeRecordMapper.clean());

    }

    // 定时检查是否有待审核评论
    @Scheduled(cron = "0 0 */1 * * *")
    public void checkPendingComment() {

        log.info("开始检查是否有待审核评论");
        long pendingCount = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getStatus, StatusConstant.COMMENT_PENDING));

        // 转为int
        int pendingCountInt = (int) pendingCount;
        if (pendingCountInt > 0) {
            emailTemplateService.sendCommentReviewNotify(infoProperties.getAdminEmail(), pendingCountInt);
            log.info("待审核评论数量为{}，已发送邮件通知管理员", pendingCountInt);
        }

    }

}
