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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class OssUtil {
    private final OssProperties ossProperties;

    private UploadManager uploadManager;

    private final Tika tika = new Tika();

    /** 魔数无法精确识别时的回退类型 */
    private static final String OCTET_STREAM = "application/octet-stream";

    /** 文本类 application 子类型（无固定魔数，魔数检测可能返回 text/plain 或自身） */
    private static final Set<String> TEXT_APPLICATION_TYPES = Set.of(
            "application/json", "application/xml", "application/javascript"
    );

    /** 专用上传线程池，避免占用 Tomcat 工作线程 */
    private final ExecutorService uploadExecutor = new ThreadPoolExecutor(
            2,                      // 核心线程数
            5,                      // 最大线程数
            60L, TimeUnit.SECONDS,  // 空闲线程存活时间
            new LinkedBlockingQueue<>(50),               // 任务队列
            new ThreadPoolExecutor.CallerRunsPolicy()    // 队列满时由调用线程执行，起到背压效果
    );

    @PostConstruct
    public void init() {
        Configuration cfg = Configuration.create(Region.createWithRegionId("z0")); // 华东地区
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;
        cfg.connectTimeout    = 10;   // 连接超时 10 秒
        cfg.writeTimeout      = 30;   // 写入超时 30 秒
        this.uploadManager = new UploadManager(cfg);
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
     * 上传文件到 OSS
     * <p>上传任务在专用线程池中执行，Controller 线程通过 CompletableFuture.get(timeout) 等待，
     * 超过 30 秒未返回则主动取消，防止线程无限期阻塞。</p>
     *
     * @param inputStream 文件输入流
     * @param fileName    原始文件名
     * @param type        目录类型（cover/avatar/image/file）
     * @return 文件访问 URL
     */
    public String uploadFile(InputStream inputStream, String fileName, String type) {
        // 将流缓存到内存，以便 Tika 魔数检测后仍可重复读取
        byte[] fileBytes;
        try {
            fileBytes = inputStream.readAllBytes();
        } catch (IOException e) {
            log.error("读取上传文件失败: {}", e.getMessage(), e);
            throw new BusinessException(CodeEnum.FILE_UPLOAD_FAIL.getCode(), "读取上传文件失败");
        }

        // 三重校验：扩展名解析 → 白名单比对 → 魔数验证
        validateFileType(new ByteArrayInputStream(fileBytes), fileName);

        String upToken = getAuthToken();
        // 生成新的文件名：type/UUID_原文件名
        final String ossKey = type + "/" + UUID.randomUUID() + "_" + fileName;

        // 将上传任务提交到专用线程池
        CompletableFuture<Response> future = CompletableFuture.supplyAsync(() -> {
            try {
                return uploadManager.put(new ByteArrayInputStream(fileBytes), ossKey, upToken, null, null);
            } catch (QiniuException e) {
                log.error("OSS 上传失败: {}", e.getMessage(), e);
                throw new BusinessException(CodeEnum.FILE_UPLOAD_FAIL.getCode(), "文件上传失败: " + e.getMessage());
            }
        }, uploadExecutor);

        try {
            // 最多等待 30 秒，防止无限阻塞
            Response response = future.get(30, TimeUnit.SECONDS);
            if (!response.isOK()) {
                log.error("OSS 上传响应异常，状态码: {}", response.statusCode);
                throw new BusinessException(CodeEnum.FILE_UPLOAD_FAIL.getCode(), "文件上传失败，服务响应异常");
            }
            return ossProperties.getOssUrl() + "/" + ossKey;
        } catch (TimeoutException e) {
            future.cancel(true);
            log.error("OSS 上传超时，ossKey={}", ossKey);
            throw new BusinessException(CodeEnum.FILE_UPLOAD_FAIL.getCode(), "文件上传超时，请稍后重试");
        } catch (ExecutionException e) {
            // 内部已包装为 BusinessException，直接提取并抛出
            Throwable cause = e.getCause();
            if (cause instanceof BusinessException be) {
                throw be;
            }
            log.error("OSS 上传执行异常: {}", cause != null ? cause.getMessage() : e.getMessage(), e);
            throw new BusinessException(CodeEnum.FILE_UPLOAD_FAIL.getCode(), "文件上传失败，请稍后重试");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("OSS 上传被中断，ossKey={}", ossKey);
            throw new BusinessException(CodeEnum.FILE_UPLOAD_FAIL.getCode(), "文件上传被中断");
        }
    }

    /**
     * 三重文件类型校验：扩展名解析 → 白名单比对 → 魔数验证
     */
    private void validateFileType(InputStream bufferedStream, String fileName) {
        // 第1层：基于扩展名解析 MIME 类型（Tika 内置覆盖上千种类型，无需手动维护映射表）
        String claimedType;
        try {
            claimedType = tika.detect(fileName);
        } catch (Exception e) {
            log.warn("文件扩展名解析失败, fileName={}: {}", fileName, e.getMessage());
            throw new BusinessException(CodeEnum.NOT_SUPPORTED_FILE_TYPE);
        }
        log.info("OSS 上传文件声称类型: {}", claimedType);

        // 第2层：白名单比对
        boolean inWhitelist = ossProperties.getAllowedMediaTypes().stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(claimedType));
        if (!inWhitelist) {
            throw new BusinessException(CodeEnum.NOT_SUPPORTED_FILE_TYPE);
        }

        // 第3层：Magic Bytes 魔数校验
        try {
            String detectedType = tika.detect(bufferedStream);
            log.info("OSS 上传文件魔数检测类型: {}", detectedType);

            if (isTextBasedMime(claimedType)) {
                // 声称是文本类：魔数结果必须是 text/* 或文本类 application，否则是二进制伪装
                if (!detectedType.startsWith("text/") && !TEXT_APPLICATION_TYPES.contains(detectedType)) {
                    log.warn("文件内容非文本，疑似伪装: 声称类型={}, 魔数检测类型={}, fileName={}", claimedType, detectedType, fileName);
                    throw new BusinessException(CodeEnum.NOT_SUPPORTED_FILE_TYPE);
                }
                log.info("文件类型 {} 为文本类，魔数验证通过（检测为 {}）", claimedType, detectedType);
            } else {
                // 非文本类：魔数结果必须与声称类型一致
                if (!claimedType.equals(detectedType) && !OCTET_STREAM.equals(detectedType)) {
                    log.warn("文件类型伪造检测: 声称类型={}, 魔数检测类型={}, fileName={}", claimedType, detectedType, fileName);
                    throw new BusinessException(CodeEnum.NOT_SUPPORTED_FILE_TYPE);
                }
            }
        } catch (IOException e) {
            log.error("魔数检测读取失败: {}", e.getMessage(), e);
            throw new BusinessException(CodeEnum.NOT_SUPPORTED_FILE_TYPE);
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
