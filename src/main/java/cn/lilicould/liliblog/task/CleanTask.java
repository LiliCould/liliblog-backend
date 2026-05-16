package cn.lilicould.liliblog.task;

import cn.lilicould.liliblog.mapper.ArticleTagMapper;
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

    public CleanTask(ArticleTagMapper articleTagMapper) {
        this.articleTagMapper = articleTagMapper;
    }

    @Scheduled(cron = "0 0 2 * * *") // 每天0点执行
    public void clean() {

        log.info("开始清理数据库中的垃圾数据");

        log.info("清理文章标签关联表完成,共清除{}条数据",articleTagMapper.clean());


    }
}
