package cn.lilicould.liliblog.util;

import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.exception.BusinessException;
import cn.lilicould.liliblog.config.properties.OssProperties;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class OssUtil {
    /** 文件流缓冲区大小，同时也是 Tika 魔数检测的 mark/reset 深度 */
    private static final int BUFFER_SIZE = 64 * 1024;

    /** OSS key 中原始文件名保留的最大长度，避免 URL 撑爆 varchar(255) */
    private static final int MAX_FILENAME_LENGTH = 60;

    /** 视为扩展名的最长长度，超过则不当作扩展名 */
    private static final int MAX_EXTENSION_LENGTH = 12;

    /** 文件名中保留的字符：字母、数字、下划线、点、连字符（其余一律替换为下划线） */
    private static final Pattern UNSAFE_FILENAME_CHARS = Pattern.compile("[^\\w.\\-]");

    /** 文件名为空时的占位名 */
    private static final String FALLBACK_FILENAME = "unnamed";

    /** 魔数无法精确识别时的回退类型 */
    private static final String OCTET_STREAM = "application/octet-stream";

    /** 文本类 application 子类型（无固定魔数，魔数检测可能返回 text/plain 或自身） */
    private static final Set<String> TEXT_APPLICATION_TYPES = Set.of(
            "application/json", "application/xml", "application/javascript"
    );

    /** 文件长度未知时的哨兵值，跳过业务级大小校验，由容器层 max-file-size 兜底 */
    private static final long UNKNOWN_FILE_SIZE = -1L;

    /** 七牛要求分片大小在 1MB~1GB 之间 */
    private static final DataSize MIN_BLOCK_SIZE = DataSize.ofMegabytes(1);
    private static final DataSize MAX_BLOCK_SIZE = DataSize.ofMegabytes(1024);

    private final OssProperties ossProperties;

    private final Tika tika = new Tika();

    private UploadManager uploadManager;

    /**
     * 并发上传闸门。
     * <p>SDK 每个在途上传会驻留一个分片（{@code oss.block-size}）大小的堆内存，
     * 而 Tomcat 默认有 200 个工作线程，若不加限制堆峰值会线性放大到不可接受。
     * 这里用信号量把「同时在传的文件数」限定住，替代原先线程池顺带起到的那层限流作用。
     * <p>与原线程池方案的关键区别：等待许可的时间由 {@code oss.queue-wait-seconds} 控制且默认宽松，
     * 不会像原先那样把排队耗时算进一个短超时里而产生大量误报。</p>
     */
    private Semaphore uploadPermits;

    @PostConstruct
    public void init() {
        Configuration cfg = Configuration.create(Region.createWithRegionId(ossProperties.getRegion()));
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;
        cfg.connectTimeout = ossProperties.getConnectTimeout();
        cfg.writeTimeout = ossProperties.getWriteTimeout();
        cfg.resumableUploadAPIV2BlockSize = resolveBlockSizeBytes();
        this.uploadManager = new UploadManager(cfg);
        this.uploadPermits = new Semaphore(Math.max(1, ossProperties.getMaxConcurrentUploads()));
        log.info("OSS 上传初始化完成: region={}, 分片大小={}MB, 并发上传上限={}",
                ossProperties.getRegion(), cfg.resumableUploadAPIV2BlockSize >> 20,
                ossProperties.getMaxConcurrentUploads());
    }

    /**
     * 分片大小必须在七牛允许的区间内，否则分片上传会被服务端拒绝
     */
    private int resolveBlockSizeBytes() {
        DataSize configured = ossProperties.getBlockSize();
        if (configured == null) {
            return (int) MIN_BLOCK_SIZE.toBytes();
        }
        if (configured.compareTo(MIN_BLOCK_SIZE) < 0 || configured.compareTo(MAX_BLOCK_SIZE) > 0) {
            log.warn("oss.block-size={} 超出七牛允许的 1MB~1GB 区间，回退为 1MB", configured);
            return (int) MIN_BLOCK_SIZE.toBytes();
        }
        return (int) configured.toBytes();
    }

    /**
     * 申请上传许可。等待时间由配置决定，避免把排队误报成上传失败
     */
    private void acquireUploadPermit() {
        try {
            if (!uploadPermits.tryAcquire(ossProperties.getQueueWaitSeconds(), TimeUnit.SECONDS)) {
                log.warn("等待上传许可超时: 并发上限={}, 排队上限={}s",
                        ossProperties.getMaxConcurrentUploads(), ossProperties.getQueueWaitSeconds());
                throw new BusinessException(CodeEnum.FILE_UPLOAD_FAIL.getCode(),
                        "当前上传任务较多，请稍后重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(CodeEnum.FILE_UPLOAD_FAIL.getCode(), "文件上传被中断");
        }
    }

    /**
     * 生成上传凭证
     * @return 上传凭证
     */
    public String getAuthToken() {
        Auth auth = Auth.create(ossProperties.getAccessKey(), ossProperties.getSecretKey());
        return auth.uploadToken(ossProperties.getBucket());
    }

    /**
     * 上传文件到 OSS（流式处理，不将文件全量读入内存）
     * <p>校验阶段用 BufferedInputStream 的 mark/reset 机制，Tika 魔数检测只读取文件头部，
     * 之后 reset 回退流直接交给 UploadManager，全程内存占用为常量级。</p>
     * <p>上传在当前线程同步执行：调用方本来就在阻塞等待结果，套一层线程池并不会腾出请求线程，
     * 反而会引入排队、并发上限（max 5），以及「排队耗时被算进等待预算」和
     * 「超时后线程无法中断导致池被长期占用」两个问题。</p>
     *
     * @param inputStream 文件输入流
     * @param fileName    原始文件名
     * @param type        目录类型（cover/avatar/image/file）
     * @return 文件访问 URL
     */
    public String uploadFile(InputStream inputStream, String fileName, String type) {
        return uploadFile(inputStream, fileName, type, UNKNOWN_FILE_SIZE);
    }

    /**
     * 上传文件到 OSS（流式处理，不将文件全量读入内存）
     *
     * @param inputStream 文件输入流
     * @param fileName    原始文件名
     * @param type        目录类型（cover/avatar/image/file）
     * @param fileSize    文件字节数，传入负数表示未知（跳过业务级大小校验）
     * @return 文件访问 URL
     */
    public String uploadFile(InputStream inputStream, String fileName, String type, long fileSize) {
        String safeFileName = sanitizeFileName(fileName);
        validateFileSize(fileSize, type);

        // 包装为 BufferedInputStream，支持 mark/reset，杜绝全量缓存
        BufferedInputStream bufferedStream = new BufferedInputStream(inputStream, BUFFER_SIZE);

        // ── 校验阶段：流仅由当前线程持有，异常时需手动关闭 ──
        String mimeType;
        try {
            bufferedStream.mark(BUFFER_SIZE);
            mimeType = validateFileType(bufferedStream, safeFileName);
            bufferedStream.reset();
        } catch (Exception e) {
            // 校验失败或流 reset 失败 → 关闭流，避免文件描述符泄漏
            closeQuietly(bufferedStream);
            if (e instanceof BusinessException be) {
                throw be;
            }
            log.error("文件流重置失败, fileName={}: {}", safeFileName, e.getMessage(), e);
            throw new BusinessException(CodeEnum.FILE_UPLOAD_FAIL.getCode(), "文件读取异常，请稍后重试");
        }
        // 校验通过，流已回退到起始位置

        final String ossKey = type + "/" + UUID.randomUUID() + "_" + safeFileName;
        // 校验已通过、堆内已有缓冲，此时才申请许可，避免白占名额
        acquireUploadPermit();
        try {
            Response response = uploadManager.put(bufferedStream, ossKey, getAuthToken(), null, mimeType);
            if (!response.isOK()) {
                log.error("OSS 上传响应异常, ossKey={}, statusCode={}", ossKey, response.statusCode);
                throw new BusinessException(CodeEnum.FILE_UPLOAD_FAIL.getCode(), "文件上传失败，服务响应异常");
            }
            log.debug("OSS 上传成功, ossKey={}, mimeType={}", ossKey, mimeType);
            return ossProperties.getOssUrl() + "/" + ossKey;
        } catch (QiniuException e) {
            log.error("OSS 上传失败, ossKey={}: {}", ossKey, e.getMessage(), e);
            throw new BusinessException(CodeEnum.FILE_UPLOAD_FAIL.getCode(), "文件上传失败: " + e.getMessage());
        } finally {
            uploadPermits.release();
            closeQuietly(bufferedStream);
        }
    }

    /**
     * 安静关闭流，忽略异常
     */
    private static void closeQuietly(InputStream stream) {
        try {
            stream.close();
        } catch (IOException ignored) {
            // 流可能已被框架或 SDK 关闭，忽略二次关闭异常
        }
    }

    /**
     * 清洗原始文件名：剥离客户端可能携带的路径成分，替换不安全字符，并限制长度
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return FALLBACK_FILENAME;
        }
        // 统一分隔符后取最后一段，挡掉 ../ 、..\\ 、C:\xxx\ 这类路径注入
        String normalized = fileName.replace('\\', '/');
        int lastSeparator = normalized.lastIndexOf('/');
        if (lastSeparator >= 0) {
            normalized = normalized.substring(lastSeparator + 1);
        }
        String safe = UNSAFE_FILENAME_CHARS.matcher(normalized).replaceAll("_");
        if (safe.isBlank()) {
            return FALLBACK_FILENAME;
        }
        return truncateKeepingExtension(safe);
    }

    /**
     * 超长文件名截断，优先保留扩展名（CDN 依赖它决定 Content-Type）
     */
    private static String truncateKeepingExtension(String safeFileName) {
        if (safeFileName.length() <= MAX_FILENAME_LENGTH) {
            return safeFileName;
        }
        int dotIndex = safeFileName.lastIndexOf('.');
        String extension = "";
        if (dotIndex > 0 && safeFileName.length() - dotIndex <= MAX_EXTENSION_LENGTH) {
            extension = safeFileName.substring(dotIndex);
        }
        int keep = Math.max(1, MAX_FILENAME_LENGTH - extension.length());
        return safeFileName.substring(0, keep) + extension;
    }

    /**
     * 业务级大小校验，在真正读取文件之前快速失败，避免浪费上行带宽
     */
    private void validateFileSize(long fileSize, String type) {
        if (fileSize < 0) {
            // 长度未知，跳过；由容器层 spring.servlet.multipart.max-file-size 兜底
            return;
        }
        DataSize limit = ossProperties.getSizeLimits().get(type);
        if (limit == null) {
            limit = ossProperties.getDefaultSizeLimit();
        }
        if (limit == null || fileSize <= limit.toBytes()) {
            return;
        }
        throw new BusinessException(CodeEnum.FILE_UPLOAD_OVER_SIZE.getCode(),
                "上传文件超过大小限制(" + limit.toMegabytes() + "MB)");
    }

    /**
     * 三重文件类型校验：扩展名解析 → 白名单比对 → 魔数验证
     *
     * @return 校验通过的文件声称 MIME 类型，用于写入 OSS 的 Content-Type
     */
    private String validateFileType(InputStream bufferedStream, String fileName) {
        // 第1层：基于扩展名解析 MIME 类型（Tika 内置覆盖上千种类型，无需手动维护映射表）
        String claimedType;
        try {
            claimedType = tika.detect(fileName);
        } catch (Exception e) {
            log.warn("文件扩展名解析失败, fileName={}: {}", fileName, e.getMessage());
            throw new BusinessException(CodeEnum.NOT_SUPPORTED_FILE_TYPE);
        }
        log.debug("OSS 上传文件声称类型: {}", claimedType);

        // 第2层：白名单比对
        List<String> allowedTypes = ossProperties.getAllowedMediaTypes();
        if (allowedTypes == null || allowedTypes.isEmpty()) {
            log.warn("未配置 oss.allowed-media-types，跳过文件类型白名单校验, fileName={}", fileName);
            return claimedType;
        }
        boolean inWhitelist = allowedTypes.stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(claimedType));
        if (!inWhitelist) {
            throw new BusinessException(CodeEnum.NOT_SUPPORTED_FILE_TYPE);
        }

        // 第3层：Magic Bytes 魔数校验
        validateMagicBytes(bufferedStream, claimedType, fileName);
        return claimedType;
    }

    /**
     * Magic Bytes 魔数校验，拦截「改扩展名伪装类型」的文件
     */
    private void validateMagicBytes(InputStream bufferedStream, String claimedType, String fileName) {
        String detectedType;
        try {
            detectedType = tika.detect(bufferedStream);
        } catch (IOException e) {
            log.error("魔数检测读取失败: {}", e.getMessage(), e);
            throw new BusinessException(CodeEnum.NOT_SUPPORTED_FILE_TYPE);
        }
        log.debug("OSS 上传文件魔数检测类型: {}", detectedType);

        if (isTextBasedMime(claimedType)) {
            // 声称是文本类：魔数结果必须是 text/* 或文本类 application，否则是二进制伪装
            if (!detectedType.startsWith("text/") && !TEXT_APPLICATION_TYPES.contains(detectedType)) {
                log.warn("文件内容非文本，疑似伪装: 声称类型={}, 魔数检测类型={}, fileName={}", claimedType, detectedType, fileName);
                throw new BusinessException(CodeEnum.NOT_SUPPORTED_FILE_TYPE);
            }
            log.debug("文件类型 {} 为文本类，魔数验证通过（检测为 {}）", claimedType, detectedType);
        } else {
            // 非文本类：魔数结果必须与声称类型一致
            if (!claimedType.equals(detectedType) && !OCTET_STREAM.equals(detectedType)) {
                log.warn("文件类型伪造检测: 声称类型={}, 魔数检测类型={}, fileName={}", claimedType, detectedType, fileName);
                throw new BusinessException(CodeEnum.NOT_SUPPORTED_FILE_TYPE);
            }
        }
    }

    /**
     * 判断声称的 MIME 类型是否为文本类（无固定魔数）
     */
    private boolean isTextBasedMime(String mimeType) {
        if (mimeType == null) return false;
        String lower = mimeType.toLowerCase();
        return lower.startsWith("text/") || TEXT_APPLICATION_TYPES.contains(lower);
    }
}
