package cn.lilicould.liliblog.task;

import cn.lilicould.liliblog.config.properties.OssProperties;
import cn.lilicould.liliblog.constant.StatusConstant;
import cn.lilicould.liliblog.entity.FileRecord;
import cn.lilicould.liliblog.entity.FileRef;
import cn.lilicould.liliblog.mapper.FileRecordMapper;
import cn.lilicould.liliblog.mapper.FileRefMapper;
import cn.lilicould.liliblog.util.OssUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiniu.common.QiniuException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件记录清理任务：
 * <p>前端直传只申请令牌、未确认完成的记录会在 pending-expire-hours 后过期。</p>
 * <p>如果对象已经上传，先删除对象再标记过期，避免产生孤儿文件。</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FileCleanTask {

    private static final int BATCH_SIZE = 500;
    private static final int QINIU_NO_SUCH_FILE = 612;

    private final FileRecordMapper fileRecordMapper;
    private final FileRefMapper fileRefMapper;
    private final OssUtil ossUtil;
    private final OssProperties ossProperties;

    /**
     * 每小时 :15 清理超时未确认的记录
     */
    @Scheduled(cron = "0 15 * * * *")
    public void cleanExpiredPendingFiles() {
        LocalDateTime deadline = LocalDateTime.now()
                .minusHours(Math.max(1, ossProperties.getPendingExpireHours()));

        List<FileRecord> records = fileRecordMapper.selectList(
                new LambdaQueryWrapper<FileRecord>()
                        // 待确认、已失败、已拒绝都可能残留对象：
                        // 分别是客户端没确认、确认前中断、校验失败后删除对象未成功
                        .in(FileRecord::getStatus,
                                StatusConstant.FILE_PENDING,
                                StatusConstant.FILE_FAILED,
                                StatusConstant.FILE_REJECTED)
                        .lt(FileRecord::getCreateTime, deadline)
                        .orderByAsc(FileRecord::getId)
                        .last("LIMIT " + BATCH_SIZE)
        );

        if (records.isEmpty()) {
            return;
        }

        log.info("开始清理超时未确认的文件记录，共{}条", records.size());
        int cleaned = 0;
        for (FileRecord record : records) {
            try {
                // 对象存在则先删除；不存在会抛 612，直接标记过期
                if (ossUtil.stat(record.getFileKey()) != null) {
                    ossUtil.delete(record.getFileKey());
                }
            } catch (QiniuException e) {
                int statusCode = e.response != null ? e.response.statusCode : -1;
                if (statusCode != QINIU_NO_SUCH_FILE && statusCode != 404) {
                    // 网络或鉴权异常：本轮跳过，下一小时重试
                    log.warn("清理文件记录失败, fileId={}, key={}: {}",
                            record.getId(), record.getFileKey(), e.getMessage());
                    continue;
                }
            }

            record.setStatus(StatusConstant.FILE_EXPIRED);
            record.setErrorMsg("上传未确认或已失败，对象已清理");
            fileRecordMapper.updateById(record);
            cleaned++;
        }

        log.info("超时未确认的文件记录清理完成，共清理{}条", cleaned);
    }

    /**
     * 每天 03:30 清理无引用的孤儿文件。
     * <p>默认关闭。开启前建议先观察日志中的候选数量，确认文章引用解析准确。</p>
     */
    @Scheduled(cron = "0 30 3 * * *")
    public void cleanOrphanFiles() {
        if (!ossProperties.isOrphanCleanEnabled()) {
            return;
        }
        LocalDateTime deadline = LocalDateTime.now()
                .minusDays(Math.max(1, ossProperties.getOrphanRetentionDays()));

        List<FileRecord> candidates = fileRecordMapper.selectList(
                new LambdaQueryWrapper<FileRecord>()
                        .eq(FileRecord::getStatus, StatusConstant.FILE_SUCCESS)
                        .lt(FileRecord::getCreateTime, deadline)
                        .orderByAsc(FileRecord::getId)
                        .last("LIMIT " + BATCH_SIZE)
        );
        if (candidates.isEmpty()) {
            return;
        }

        int cleaned = 0;
        for (FileRecord record : candidates) {
            Long refCount = fileRefMapper.selectCount(
                    new LambdaQueryWrapper<FileRef>()
                            .eq(FileRef::getFileId, record.getId())
            );
            if (refCount != null && refCount > 0) {
                continue;
            }
            log.info("清理孤儿文件: fileId={}, key={}", record.getId(), record.getFileKey());
            ossUtil.deleteQuietly(record.getFileKey());
            record.setStatus(StatusConstant.FILE_EXPIRED);
            record.setErrorMsg("无引用文件，已自动清理");
            fileRecordMapper.updateById(record);
            cleaned++;
        }
        log.info("孤儿文件清理完成，共清理{}条", cleaned);
    }
}
