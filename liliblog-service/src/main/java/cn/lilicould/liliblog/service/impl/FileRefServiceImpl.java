package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.config.properties.OssProperties;
import cn.lilicould.liliblog.constant.StatusConstant;
import cn.lilicould.liliblog.entity.FileRecord;
import cn.lilicould.liliblog.entity.FileRef;
import cn.lilicould.liliblog.mapper.FileRecordMapper;
import cn.lilicould.liliblog.mapper.FileRefMapper;
import cn.lilicould.liliblog.service.FileRefService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文件引用关系 Service 实现。
 * <p>文章正文中的图片/附件以 Markdown 链接或 HTML src 的形式引用文件，
 * 这里在文章保存时登记引用，文章删除时释放引用，供孤儿文件清理任务使用。</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileRefServiceImpl extends ServiceImpl<FileRefMapper, FileRef> implements FileRefService {

    public static final String ARTICLE_COVER = "ARTICLE_COVER";
    public static final String ARTICLE_IMAGE = "ARTICLE_IMAGE";
    public static final String USER_AVATAR = "USER_AVATAR";
    /** 历史文件保护引用：只要存在该引用，孤儿清理就不会删除对应文件 */
    public static final String LEGACY_KEEP = "LEGACY_KEEP";

    /** Markdown 图片/链接：![alt](url) 或 [text](url) */
    private static final Pattern MARKDOWN_URL =
            Pattern.compile("!?\\[[^]]*]\\((https?://[^)\\s]+)\\)");
    /** HTML 引用：src="url" */
    private static final Pattern HTML_SRC =
            Pattern.compile("src=[\"'](https?://[^\"']+)[\"']", Pattern.CASE_INSENSITIVE);

    private final FileRecordMapper fileRecordMapper;
    private final OssProperties ossProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncArticleRefs(Long articleId, Long ownerId, String coverImage, String content) {
        if (articleId == null) {
            return;
        }
        // 全量重建该文章的引用，逻辑简单且幂等
        remove(new LambdaQueryWrapper<FileRef>()
                .eq(FileRef::getTargetId, articleId)
                .in(FileRef::getTargetType, ARTICLE_COVER, ARTICLE_IMAGE));

        String coverUrl = normalizeUrl(coverImage);
        Set<String> contentUrls = extractOssUrls(content);
        Set<String> allUrls = new LinkedHashSet<>(contentUrls);
        if (coverUrl != null) {
            allUrls.add(coverUrl);
        }
        if (allUrls.isEmpty()) {
            return;
        }

        List<FileRecord> records = fileRecordMapper.selectList(
                new LambdaQueryWrapper<FileRecord>()
                        .in(FileRecord::getUrl, allUrls)
                        .eq(ownerId != null, FileRecord::getUserId, ownerId)
                        .eq(FileRecord::getStatus, StatusConstant.FILE_SUCCESS)
        );

        List<FileRef> refs = new ArrayList<>();
        for (FileRecord record : records) {
            if (coverUrl != null && coverUrl.equals(record.getUrl())) {
                refs.add(buildRef(record.getId(), ARTICLE_COVER, articleId));
            }
            if (contentUrls.contains(record.getUrl())) {
                refs.add(buildRef(record.getId(), ARTICLE_IMAGE, articleId));
            }
        }
        if (!refs.isEmpty()) {
            saveBatch(refs);
            log.debug("同步文章文件引用: articleId={}, refs={}", articleId, refs.size());
        }
    }

    @Override
    public void removeArticleRefs(Long articleId) {
        if (articleId == null) {
            return;
        }
        remove(new LambdaQueryWrapper<FileRef>()
                .eq(FileRef::getTargetId, articleId)
                .in(FileRef::getTargetType, ARTICLE_COVER, ARTICLE_IMAGE));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncUserAvatar(Long userId, String avatarUrl) {
        if (userId == null) {
            return;
        }
        remove(new LambdaQueryWrapper<FileRef>()
                .eq(FileRef::getTargetType, USER_AVATAR)
                .eq(FileRef::getTargetId, userId));

        String normalized = normalizeUrl(avatarUrl);
        if (normalized == null) {
            return;
        }
        FileRecord record = fileRecordMapper.selectOne(
                new LambdaQueryWrapper<FileRecord>()
                        .eq(FileRecord::getUrl, normalized)
                        .eq(FileRecord::getUserId, userId)
                        .eq(FileRecord::getStatus, StatusConstant.FILE_SUCCESS)
                        .last("LIMIT 1")
        );
        if (record != null) {
            save(buildRef(record.getId(), USER_AVATAR, userId));
        }
    }

    @Override
    public void removeUserRefs(Long userId) {
        if (userId == null) {
            return;
        }
        remove(new LambdaQueryWrapper<FileRef>()
                .eq(FileRef::getTargetType, USER_AVATAR)
                .eq(FileRef::getTargetId, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BackfillRefResult backfillArticleRefs(Long articleId, Long ownerId,
                                                 String coverImage, String content) {
        if (articleId == null) {
            return new BackfillRefResult(0, 0);
        }
        remove(new LambdaQueryWrapper<FileRef>()
                .eq(FileRef::getTargetId, articleId)
                .in(FileRef::getTargetType, ARTICLE_COVER, ARTICLE_IMAGE));

        String coverUrl = normalizeUrl(coverImage);
        Set<String> contentUrls = extractOssUrls(content);
        Set<String> allUrls = new LinkedHashSet<>(contentUrls);
        if (coverUrl != null) {
            allUrls.add(coverUrl);
        }
        if (allUrls.isEmpty()) {
            return new BackfillRefResult(0, 0);
        }

        // 历史数据的 user_id 可能为空，这里只按 URL 匹配
        List<FileRecord> records = fileRecordMapper.selectList(
                new LambdaQueryWrapper<FileRecord>()
                        .in(FileRecord::getUrl, allUrls)
                        .eq(FileRecord::getStatus, StatusConstant.FILE_SUCCESS)
        );
        Map<String, FileRecord> byUrl = records.stream()
                .collect(Collectors.toMap(FileRecord::getUrl, Function.identity(), (a, b) -> a));

        List<FileRef> refs = new ArrayList<>();
        int missing = 0;
        for (String url : allUrls) {
            FileRecord record = byUrl.get(url);
            if (record == null) {
                missing++;
                continue;
            }
            if (coverUrl != null && coverUrl.equals(url)) {
                refs.add(buildRef(record.getId(), ARTICLE_COVER, articleId));
            }
            if (contentUrls.contains(url)) {
                refs.add(buildRef(record.getId(), ARTICLE_IMAGE, articleId));
            }
            if (record.getUserId() == null && ownerId != null) {
                record.setUserId(ownerId);
                fileRecordMapper.updateById(record);
            }
        }
        if (!refs.isEmpty()) {
            saveBatch(refs);
        }
        log.debug("回填文章文件引用: articleId={}, refs={}, missing={}",
                articleId, refs.size(), missing);
        return new BackfillRefResult(refs.size(), missing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BackfillRefResult backfillUserAvatar(Long userId, String avatarUrl) {
        if (userId == null) {
            return new BackfillRefResult(0, 0);
        }
        remove(new LambdaQueryWrapper<FileRef>()
                .eq(FileRef::getTargetType, USER_AVATAR)
                .eq(FileRef::getTargetId, userId));

        String normalized = normalizeUrl(avatarUrl);
        if (normalized == null) {
            return new BackfillRefResult(0, 0);
        }
        FileRecord record = fileRecordMapper.selectOne(
                new LambdaQueryWrapper<FileRecord>()
                        .eq(FileRecord::getUrl, normalized)
                        .eq(FileRecord::getStatus, StatusConstant.FILE_SUCCESS)
                        .last("LIMIT 1")
        );
        if (record == null) {
            return new BackfillRefResult(0, 1);
        }
        if (record.getUserId() == null) {
            record.setUserId(userId);
            fileRecordMapper.updateById(record);
        }
        save(buildRef(record.getId(), USER_AVATAR, userId));
        return new BackfillRefResult(1, 0);
    }

    private Set<String> extractOssUrls(String content) {
        Set<String> urls = new LinkedHashSet<>();
        if (!StringUtils.hasText(content)) {
            return urls;
        }
        Matcher markdownMatcher = MARKDOWN_URL.matcher(content);
        while (markdownMatcher.find()) {
            addIfOssUrl(urls, markdownMatcher.group(1));
        }
        Matcher htmlMatcher = HTML_SRC.matcher(content);
        while (htmlMatcher.find()) {
            addIfOssUrl(urls, htmlMatcher.group(1));
        }
        return urls;
    }

    private void addIfOssUrl(Set<String> urls, String rawUrl) {
        String normalized = normalizeUrl(rawUrl);
        if (normalized != null && isOssUrl(normalized)) {
            urls.add(normalized);
        }
    }

    /**
     * 去掉 query/fragment，统一成数据库里存储的 URL 形式
     */
    private String normalizeUrl(String rawUrl) {
        if (!StringUtils.hasText(rawUrl)) {
            return null;
        }
        String url = rawUrl.trim();
        int cutIndex = url.length();
        int queryIndex = url.indexOf('?');
        int fragmentIndex = url.indexOf('#');
        if (queryIndex >= 0) {
            cutIndex = Math.min(cutIndex, queryIndex);
        }
        if (fragmentIndex >= 0) {
            cutIndex = Math.min(cutIndex, fragmentIndex);
        }
        url = cutIndex == url.length() ? url : url.substring(0, cutIndex);
        // Markdown 自动链接可能形如 <https://...>，去掉首尾包裹字符
        while (url.endsWith(">") || url.endsWith("\"") || url.endsWith("'")) {
            url = url.substring(0, url.length() - 1);
        }
        if (url.startsWith("<")) {
            url = url.substring(1);
        }
        return url;
    }

    private boolean isOssUrl(String url) {
        String baseUrl = ossProperties.getOssUrl();
        if (!StringUtils.hasText(baseUrl)) {
            return false;
        }
        String prefix = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        return url.startsWith(prefix);
    }

    private FileRef buildRef(Long fileId, String targetType, Long targetId) {
        return FileRef.builder()
                .fileId(fileId)
                .targetType(targetType)
                .targetId(targetId)
                .build();
    }
}
