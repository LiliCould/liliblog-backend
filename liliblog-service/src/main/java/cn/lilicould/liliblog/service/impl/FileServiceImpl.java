package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.cache.RedisHelper;
import cn.lilicould.liliblog.config.properties.OssProperties;
import cn.lilicould.liliblog.constant.RedisPrefixConstant;
import cn.lilicould.liliblog.constant.StatusConstant;
import cn.lilicould.liliblog.context.BaseContext;
import cn.lilicould.liliblog.entity.Article;
import cn.lilicould.liliblog.entity.FileRecord;
import cn.lilicould.liliblog.entity.FileRef;
import cn.lilicould.liliblog.entity.User;
import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.exception.BusinessException;
import cn.lilicould.liliblog.mapper.ArticleMapper;
import cn.lilicould.liliblog.mapper.FileRecordMapper;
import cn.lilicould.liliblog.mapper.UserMapper;
import cn.lilicould.liliblog.query.FileQuery;
import cn.lilicould.liliblog.request.UploadAbandonRequest;
import cn.lilicould.liliblog.request.UploadConfirmRequest;
import cn.lilicould.liliblog.request.UploadTokenRequest;
import cn.lilicould.liliblog.response.BackfillResultVO;
import cn.lilicould.liliblog.response.FileRecordVO;
import cn.lilicould.liliblog.response.PageInfo;
import cn.lilicould.liliblog.response.UploadTokenVO;
import cn.lilicould.liliblog.service.FileRefService;
import cn.lilicould.liliblog.service.FileService;
import cn.lilicould.liliblog.util.OssUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.model.FileInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文件上传记录 Service 实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl extends ServiceImpl<FileRecordMapper, FileRecord> implements FileService {

    private static final Set<String> ALLOWED_BIZ_TYPES = Set.of("cover", "avatar", "image", "file");
    private static final Set<String> IMAGE_BIZ_TYPES = Set.of("cover", "avatar", "image");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final int QINIU_NO_SUCH_FILE = 612;

    private final OssProperties ossProperties;
    private final OssUtil ossUtil;
    private final RedisHelper redisHelper;
    private final FileRefService fileRefService;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    private final Tika tika = new Tika();

    @Override
    public UploadTokenVO issueUploadToken(UploadTokenRequest request) {
        if (!ossProperties.isDirectUploadEnabled()) {
            throw new BusinessException(CodeEnum.FILE_UPLOAD_FAIL.getCode(), "前端直传未开启，请使用后台上传接口");
        }

        Long userId = currentUserId();
        String type = normalizeType(request.getType());
        String safeName = ossUtil.sanitizeFileName(request.getFileName());
        String claimedMime = tika.detect(safeName);

        validateMime(claimedMime);

        long limit = sizeLimitBytes(type);
        if (request.getFileSize() > limit) {
            throw new BusinessException(CodeEnum.FILE_UPLOAD_OVER_SIZE.getCode(),
                    "上传文件超过大小限制(" + (limit >> 20) + "MB)");
        }
        checkPending(userId);
        checkQuota(userId, request.getFileSize());

        String key = buildKey(type, userId, safeName);
        String url = buildUrl(key);

        FileRecord record = FileRecord.builder()
                .userId(userId)
                .fileKey(key)
                .url(url)
                // 数据库保留可读的原始文件名，key 中只使用安全扩展名
                .originalName(normalizeOriginalName(request.getFileName()))
                .mimeType(claimedMime)
                .fileSize(request.getFileSize())
                .bizType(type)
                .uploadSource(StatusConstant.FILE_SOURCE_DIRECT)
                .status(StatusConstant.FILE_PENDING)
                .deleted(StatusConstant.NOT_DELETED)
                .build();
        save(record);

        String mimeLimit = buildMimeLimit(type);
        String token = ossUtil.generateUploadToken(
                key, limit, mimeLimit, ossProperties.getTokenExpireSeconds());

        log.info("签发直传令牌: userId={}, fileId={}, key={}, size={}",
                userId, record.getId(), key, request.getFileSize());

        return UploadTokenVO.builder()
                .fileId(record.getId())
                .uploadUrl(ossProperties.getUploadUrl())
                .token(token)
                .key(key)
                .url(url)
                .expiresIn(ossProperties.getTokenExpireSeconds())
                .maxSize(limit)
                .accept(mimeLimit)
                .build();
    }

    @Override
    public FileRecordVO confirmUpload(UploadConfirmRequest request) {
        Long userId = currentUserId();
        FileRecord record = getById(request.getFileId());
        if (record == null || !Objects.equals(record.getUserId(), userId)) {
            throw new BusinessException(CodeEnum.FILE_RECORD_NOT_FOUND);
        }
        // 幂等：重复确认直接返回已有结果
        if (StatusConstant.FILE_SUCCESS.equals(record.getStatus())) {
            return FileRecordVO.from(record);
        }
        if (!StatusConstant.FILE_PENDING.equals(record.getStatus())) {
            throw new BusinessException(CodeEnum.FILE_STATUS_INVALID);
        }

        FileInfo info;
        try {
            info = ossUtil.stat(record.getFileKey());
        } catch (QiniuException e) {
            log.warn("确认上传失败，对象不存在或查询失败, key={}: {}", record.getFileKey(), e.getMessage());
            throw new BusinessException(CodeEnum.FILE_NOT_UPLOADED);
        }
        if (info == null) {
            throw new BusinessException(CodeEnum.FILE_NOT_UPLOADED);
        }

        if (StringUtils.hasText(request.getHash())
                && !request.getHash().equalsIgnoreCase(info.hash)) {
            reject(record, "文件hash与上传结果不一致",
                    new BusinessException(CodeEnum.FILE_VERIFY_FAILED));
        }

        long limit = sizeLimitBytes(record.getBizType());
        if (info.fsize > limit) {
            reject(record, "文件大小超过限制",
                    new BusinessException(CodeEnum.FILE_UPLOAD_OVER_SIZE.getCode(),
                            "上传文件超过大小限制(" + (limit >> 20) + "MB)"));
        }

        if (ossProperties.isMagicCheckOnConfirm()) {
            String detectedMime = ossUtil.detectRemoteMime(
                    record.getFileKey(), ossProperties.getMagicCheckBytes());
            if (!ossUtil.isMimeConsistent(record.getMimeType(), detectedMime)) {
                reject(record, "文件内容与扩展名不符: " + detectedMime,
                        new BusinessException(CodeEnum.NOT_SUPPORTED_FILE_TYPE));
            }
            record.setDetectedMime(detectedMime);
        }

        // 令牌签发时按客户端声明大小预占配额，这里用真实大小补差
        reconcileQuota(record.getUserId(), record.getFileSize(), info.fsize);

        record.setFileSize(info.fsize);
        record.setHash(info.hash);
        record.setStatus(StatusConstant.FILE_SUCCESS);
        record.setErrorMsg(null);
        updateById(record);

        log.info("直传确认成功: userId={}, fileId={}, key={}, size={}, mime={}",
                userId, record.getId(), record.getFileKey(), info.fsize, record.getDetectedMime());

        return FileRecordVO.from(record);
    }

    @Override
    public void abandonUpload(UploadAbandonRequest request) {
        Long userId = currentUserId();
        FileRecord record = getById(request.getFileId());
        if (record == null || !Objects.equals(record.getUserId(), userId)) {
            throw new BusinessException(CodeEnum.FILE_RECORD_NOT_FOUND);
        }
        if (StatusConstant.FILE_PENDING.equals(record.getStatus())) {
            // 对象可能已经上传完成，尽力删除；清理任务会兜底
            ossUtil.deleteQuietly(record.getFileKey());
            updateStatus(record, StatusConstant.FILE_FAILED, "用户放弃上传");
        }
    }

    @Override
    public PageInfo<FileRecordVO> list(FileQuery query) {
        Page<FileRecord> recordPage = page(
                Page.of(query.getCurrent(), query.getSize()),
                new LambdaQueryWrapper<FileRecord>()
                        .eq(query.getUserId() != null, FileRecord::getUserId, query.getUserId())
                        .eq(StringUtils.hasText(query.getBizType()), FileRecord::getBizType, query.getBizType())
                        .eq(query.getStatus() != null, FileRecord::getStatus, query.getStatus())
                        .like(StringUtils.hasText(query.getOriginalName()), FileRecord::getOriginalName,
                                query.getOriginalName())
                        .ge(query.getCreateTimeStart() != null, FileRecord::getCreateTime,
                                query.getCreateTimeStart())
                        .le(query.getCreateTimeEnd() != null, FileRecord::getCreateTime,
                                query.getCreateTimeEnd())
                        .orderByDesc(FileRecord::getCreateTime)
                        .orderByDesc(FileRecord::getId)
        );

        if (recordPage.getTotal() == 0) {
            return PageInfo.empty(query.getCurrent(), query.getSize());
        }

        List<FileRecordVO> records = recordPage.getRecords().stream()
                .map(FileRecordVO::from)
                .toList();
        Page<FileRecordVO> voPage = Page.of(
                recordPage.getCurrent(), recordPage.getSize(), recordPage.getTotal());
        voPage.setRecords(records);
        return PageInfo.of(voPage);
    }

    @Override
    public void deleteFile(Long id) {
        FileRecord record = getById(id);
        if (record == null) {
            throw new BusinessException(CodeEnum.FILE_RECORD_NOT_FOUND);
        }
        try {
            ossUtil.delete(record.getFileKey());
        } catch (QiniuException e) {
            int statusCode = e.response != null ? e.response.statusCode : -1;
            // 对象已经不存在视为删除成功；其它错误不删记录，避免留下无法追踪的孤儿对象
            if (statusCode != QINIU_NO_SUCH_FILE && statusCode != 404) {
                log.error("管理员删除文件失败: fileId={}, key={}, statusCode={}",
                        id, record.getFileKey(), statusCode, e);
                throw new BusinessException(CodeEnum.FILE_UPLOAD_FAIL.getCode(), "删除存储对象失败，请稍后重试");
            }
        }
        removeById(id);
        log.info("管理员删除文件: fileId={}, key={}", id, record.getFileKey());
    }

    @Override
    public void recordProxyUpload(String url, String key, String originalName,
                                  String type, long fileSize, String mimeType) {
        String resolvedKey = StringUtils.hasText(key) ? key : url;
        FileRecord record = FileRecord.builder()
                .userId(BaseContext.getCurrentUserId())
                .fileKey(resolvedKey)
                .url(url)
                .originalName(normalizeOriginalName(originalName))
                .mimeType(mimeType)
                .fileSize(fileSize)
                .bizType(normalizeType(type))
                .uploadSource(StatusConstant.FILE_SOURCE_PROXY)
                .status(StatusConstant.FILE_SUCCESS)
                .deleted(StatusConstant.NOT_DELETED)
                .build();
        save(record);
    }

    @Override
    public BackfillResultVO backfillLegacyFiles(String prefix, boolean dryRun, int maxObjects) {
        int limit = Math.max(1, Math.min(maxObjects, 20000));
        List<FileInfo> objects = ossUtil.listObjects(prefix, Math.min(20000, limit + 1));
        boolean hasMore = objects.size() > limit;
        if (hasMore) {
            objects = new ArrayList<>(objects.subList(0, limit));
        }

        int insertedRecords = 0;
        int existingRecords = 0;
        int protectedRefs = 0;

        for (FileInfo info : objects) {
            if (!StringUtils.hasText(info.key)) {
                continue;
            }
            FileRecord record = getOne(new LambdaQueryWrapper<FileRecord>()
                    .eq(FileRecord::getFileKey, info.key)
                    .last("LIMIT 1"));

            if (record == null) {
                insertedRecords++;
                if (!dryRun) {
                    FileRecord created = FileRecord.builder()
                            .fileKey(info.key)
                            .url(buildUrl(info.key))
                            .originalName(deriveOriginalName(info.key))
                            .mimeType(info.mimeType)
                            .fileSize(info.fsize)
                            .hash(info.hash)
                            .bizType(deriveBizType(info.key))
                            .uploadSource(StatusConstant.FILE_SOURCE_LEGACY)
                            .status(StatusConstant.FILE_SUCCESS)
                            .deleted(StatusConstant.NOT_DELETED)
                            .build();
                    try {
                        save(created);
                        record = created;
                    } catch (DuplicateKeyException e) {
                        // 并发或历史软删除记录占用 key 时，不阻断整体回填
                        record = getOne(new LambdaQueryWrapper<FileRecord>()
                                .eq(FileRecord::getFileKey, info.key)
                                .last("LIMIT 1"));
                        insertedRecords--;
                        existingRecords++;
                    }
                }
            } else {
                existingRecords++;
            }

            if (!dryRun && record != null && record.getId() != null
                    && ensureLegacyKeepRef(record.getId())) {
                protectedRefs++;
            }
        }

        int scannedArticles = 0;
        int articleRefs = 0;
        int scannedUsers = 0;
        int avatarRefs = 0;
        int missing = 0;

        if (!dryRun) {
            List<Article> articles = articleMapper.selectList(
                    new LambdaQueryWrapper<Article>()
                            .select(Article::getId, Article::getCreateBy,
                                    Article::getCoverImage, Article::getContent));
            scannedArticles = articles.size();
            for (Article article : articles) {
                FileRefService.BackfillRefResult result = fileRefService.backfillArticleRefs(
                        article.getId(), article.getCreateBy(),
                        article.getCoverImage(), article.getContent());
                articleRefs += result.created();
                missing += result.missing();
            }

            List<User> users = userMapper.selectList(
                    new LambdaQueryWrapper<User>().select(User::getId, User::getAvatar));
            scannedUsers = users.size();
            for (User user : users) {
                if (!StringUtils.hasText(user.getAvatar())) {
                    continue;
                }
                FileRefService.BackfillRefResult result = fileRefService.backfillUserAvatar(
                        user.getId(), user.getAvatar());
                avatarRefs += result.created();
                missing += result.missing();
            }
        }

        log.info("历史文件回填完成: dryRun={}, objects={}, inserted={}, existing={}, keepRefs={}, "
                        + "articles={}, articleRefs={}, users={}, avatarRefs={}, missing={}, hasMore={}",
                dryRun, objects.size(), insertedRecords, existingRecords, protectedRefs,
                scannedArticles, articleRefs, scannedUsers, avatarRefs, missing, hasMore);

        return BackfillResultVO.builder()
                .dryRun(dryRun)
                .totalObjects(objects.size())
                .insertedRecords(insertedRecords)
                .existingRecords(existingRecords)
                .protectedRefs(protectedRefs)
                .scannedArticles(scannedArticles)
                .articleRefs(articleRefs)
                .scannedUsers(scannedUsers)
                .avatarRefs(avatarRefs)
                .missingReferences(missing)
                .hasMore(hasMore)
                .build();
    }

    /**
     * 为历史文件建立保护引用，返回是否新建。
     */
    private boolean ensureLegacyKeepRef(Long fileId) {
        long exists = fileRefService.count(new LambdaQueryWrapper<FileRef>()
                .eq(FileRef::getFileId, fileId)
                .eq(FileRef::getTargetType, FileRefServiceImpl.LEGACY_KEEP)
                .eq(FileRef::getTargetId, 0L));
        if (exists > 0) {
            return false;
        }
        try {
            fileRefService.save(FileRef.builder()
                    .fileId(fileId)
                    .targetType(FileRefServiceImpl.LEGACY_KEEP)
                    .targetId(0L)
                    .build());
            return true;
        } catch (DuplicateKeyException ignored) {
            return false;
        }
    }

    /**
     * 历史 key 形如 {type}/{uuid}_{原始文件名}，尽量还原可读文件名。
     */
    private String deriveOriginalName(String key) {
        String name = key.substring(key.lastIndexOf('/') + 1);
        int underscore = name.indexOf('_');
        if (underscore == 36) {
            name = name.substring(underscore + 1);
        }
        return name.isBlank() ? "unnamed" : name;
    }

    private String deriveBizType(String key) {
        int slash = key.indexOf('/');
        String type = slash > 0 ? key.substring(0, slash) : "file";
        return ALLOWED_BIZ_TYPES.contains(type) ? type : "file";
    }

    private Long currentUserId() {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(CodeEnum.NOT_LOGGED_IN);
        }
        return userId;
    }

    private String normalizeType(String type) {
        return type != null && ALLOWED_BIZ_TYPES.contains(type) ? type : "file";
    }

    /**
     * 保留可读的原始文件名用于展示/审计：去掉客户端路径和控制字符，限制到 255 字符。
     * 该值不参与对象 key，因此可以保留中文、空格等字符。
     */
    private String normalizeOriginalName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "unnamed";
        }
        String normalized = fileName.replace('\\', '/');
        int separator = normalized.lastIndexOf('/');
        if (separator >= 0) {
            normalized = normalized.substring(separator + 1);
        }
        normalized = normalized.replaceAll("\\p{Cntrl}", "");
        if (normalized.isBlank()) {
            return "unnamed";
        }
        return normalized.length() > 255 ? normalized.substring(0, 255) : normalized;
    }

    private void validateMime(String mimeType) {
        List<String> allowedTypes = ossProperties.getAllowedMediaTypes();
        if (allowedTypes == null || allowedTypes.isEmpty()) {
            log.warn("未配置 oss.allowed-media-types，跳过文件类型白名单校验");
            return;
        }
        boolean allowed = allowedTypes.stream().anyMatch(item -> item.equalsIgnoreCase(mimeType));
        if (!allowed) {
            throw new BusinessException(CodeEnum.NOT_SUPPORTED_FILE_TYPE);
        }
    }

    private long sizeLimitBytes(String type) {
        DataSize limit = ossProperties.getSizeLimits().get(type);
        if (limit == null) {
            limit = ossProperties.getDefaultSizeLimit();
        }
        return limit == null ? DataSize.ofMegabytes(40).toBytes() : limit.toBytes();
    }

    private void checkQuota(Long userId, long fileSize) {
        String day = LocalDate.now().format(DAY_FORMATTER);

        String countKey = RedisPrefixConstant.FILE_UPLOAD_DAILY_COUNT + userId + ":" + day;
        Long count;
        try {
            count = redisHelper.increment(countKey);
            if (count != null && count == 1L) {
                redisHelper.expire(countKey, Duration.ofDays(2));
            }
        } catch (Exception e) {
            // Redis 不可用时放行：上传功能优先，配额由清理任务和七牛侧限制兜底
            log.warn("上传次数配额检查失败，放行本次请求: userId={}, error={}", userId, e.getMessage());
            return;
        }
        if (count != null && count > ossProperties.getDailyCountLimit()) {
            throw new BusinessException(CodeEnum.FILE_UPLOAD_LIMIT_EXCEEDED.getCode(),
                    "今日上传次数已达上限(" + ossProperties.getDailyCountLimit() + "次)");
        }

        String bytesKey = RedisPrefixConstant.FILE_UPLOAD_DAILY_BYTES + userId + ":" + day;
        Long bytes;
        try {
            bytes = redisHelper.increment(bytesKey, fileSize);
            if (bytes != null && bytes == fileSize) {
                redisHelper.expire(bytesKey, Duration.ofDays(2));
            }
        } catch (Exception e) {
            log.warn("上传流量配额检查失败，放行本次请求: userId={}, error={}", userId, e.getMessage());
            return;
        }
        long dailyBytesLimit = ossProperties.getDailyBytesLimit().toBytes();
        if (bytes != null && bytes > dailyBytesLimit) {
            throw new BusinessException(CodeEnum.FILE_UPLOAD_LIMIT_EXCEEDED.getCode(),
                    "今日上传流量已达上限(" + ossProperties.getDailyBytesLimit().toMegabytes() + "MB)");
        }
    }

    private void reconcileQuota(Long userId, Long reserved, long actual) {
        if (userId == null || reserved == null || actual <= reserved) {
            return;
        }
        try {
            String day = LocalDate.now().format(DAY_FORMATTER);
            String bytesKey = RedisPrefixConstant.FILE_UPLOAD_DAILY_BYTES + userId + ":" + day;
            redisHelper.increment(bytesKey, actual - reserved);
            redisHelper.expire(bytesKey, Duration.ofDays(2));
        } catch (Exception e) {
            log.warn("上传流量配额补差失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    private void checkPending(Long userId) {
        long pendingCount = count(new LambdaQueryWrapper<FileRecord>()
                .eq(FileRecord::getUserId, userId)
                .eq(FileRecord::getStatus, StatusConstant.FILE_PENDING)
                .ge(FileRecord::getCreateTime,
                        LocalDateTime.now().minusHours(ossProperties.getPendingExpireHours())));
        if (pendingCount >= Math.max(1, ossProperties.getMaxPendingPerUser())) {
            throw new BusinessException(CodeEnum.FILE_UPLOAD_LIMIT_EXCEEDED.getCode(),
                    "待确认的上传任务过多，请稍后重试");
        }
    }

    private String buildKey(String type, Long userId, String safeFileName) {
        String extension = ossUtil.extensionOf(safeFileName);
        String suffix = extension.isEmpty() ? "" : "." + extension;
        return type + "/" + userId + "/"
                + LocalDate.now().format(MONTH_FORMATTER) + "/"
                + UUID.randomUUID() + suffix;
    }

    private String buildUrl(String key) {
        String baseUrl = ossProperties.getOssUrl();
        if (!StringUtils.hasText(baseUrl)) {
            throw new BusinessException(CodeEnum.FILE_UPLOAD_FAIL.getCode(), "未配置 oss.oss-url");
        }
        return baseUrl.endsWith("/") ? baseUrl + key : baseUrl + "/" + key;
    }

    private String buildMimeLimit(String type) {
        List<String> allowedTypes = ossProperties.getAllowedMediaTypes();
        if (allowedTypes == null || allowedTypes.isEmpty()) {
            return null;
        }
        return allowedTypes.stream()
                .filter(mime -> !IMAGE_BIZ_TYPES.contains(type)
                        || mime.toLowerCase(Locale.ROOT).startsWith("image/"))
                .collect(Collectors.joining(";"));
    }

    private void reject(FileRecord record, String reason, BusinessException exception) {
        ossUtil.deleteQuietly(record.getFileKey());
        updateStatus(record, StatusConstant.FILE_REJECTED, reason);
        throw exception;
    }

    private void updateStatus(FileRecord record, Integer status, String errorMsg) {
        record.setStatus(status);
        record.setErrorMsg(errorMsg);
        updateById(record);
    }
}
