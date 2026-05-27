package cn.lilicould.liliblog.task;

import cn.lilicould.liliblog.common.constant.StatusConstant;
import cn.lilicould.liliblog.config.properties.InfoProperties;
import cn.lilicould.liliblog.mapper.ArticleTagMapper;
import cn.lilicould.liliblog.mapper.AuditLogMapper;
import cn.lilicould.liliblog.mapper.CommentMapper;
import cn.lilicould.liliblog.mapper.LikeRecordMapper;
import cn.lilicould.liliblog.model.entity.AuditLog;
import cn.lilicould.liliblog.model.entity.Comment;
import cn.lilicould.liliblog.service.impl.EmailTemplateService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

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
    private final AuditLogMapper auditLogMapper;

    public CleanTask(ArticleTagMapper articleTagMapper, CommentMapper commentMapper, LikeRecordMapper likeRecordMapper, EmailTemplateService emailTemplateService, InfoProperties infoProperties, AuditLogMapper auditLogMapper) {
        this.articleTagMapper = articleTagMapper;
        this.commentMapper = commentMapper;
        this.likeRecordMapper = likeRecordMapper;
        this.emailTemplateService = emailTemplateService;
        this.infoProperties = infoProperties;
        this.auditLogMapper = auditLogMapper;
    }

    /**
     * 每日0点执行，清理数据库中的垃圾数据
     */
    @Scheduled(cron = "0 0 2 * * *") // 每天0点执行
    public void clean() {

        log.info("开始清理数据库中的垃圾数据");

        log.info("清理文章标签关联表完成,共清除{}条数据",articleTagMapper.clean());
        log.info("清理已删除文章的评论完成，共清除{}条数据", commentMapper.clean());
        log.info("清理已删除文章或评论的点赞记录表，共清除{}条数据", likeRecordMapper.clean());

    }

    /**
     * 每小时执行一次，检查是否有待审核的评论
     */
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

    /**
     * 每天0点执行，清理七天前的审计日志
     */
    public void cleanOverSevenDaysAuditLog() {
        log.info("开始清理七天前的审核日志");

        // 创建查询条件
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        wrapper.lt(AuditLog::getCreateTime, sevenDaysAgo);

        log.info("清理完成，共清理{}条数据", auditLogMapper.delete(wrapper));
    }

}
