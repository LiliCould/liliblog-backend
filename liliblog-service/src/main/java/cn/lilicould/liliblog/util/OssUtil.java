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

import java.io.BufferedInputStream;
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
            new ThreadPoolExecutor.AbortPolicy()         // 队列满时拒绝，而非反噬 Tomcat 线程
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
     * 上传文件到 OSS（流式处理，不再将文件全量读入内存）
     * <p>用 BufferedInputStream 的 mark/reset 机制，Tika 魔数检测只读取文件头部（最多 64KB），
     * 之后 reset 回退流，直接传给 UploadManager 流式上传，全程内存占用为常量级。</p>
     * <p>上传任务在专用线程池中执行（AbortPolicy 拒绝策略，队列满时返回友好错误），
     * Controller 线程通过 CompletableFuture.get(timeout) 等待，超时直接返回错误。</p>
     *
     * @param inputStream 文件输入流
     * @param fileName    原始文件名
     * @param type        目录类型（cover/avatar/image/file）
     * @return 文件访问 URL
     */
    public String uploadFile(InputStream inputStream, String fileName, String type) {
        // 包装为 BufferedInputStream，支持 mark/reset，杜绝全量缓存
        BufferedInputStream bufferedStream = new BufferedInputStream(inputStream, 65536);

        // ── 校验阶段：流仅由调用线程持有，异常时需手动关闭 ──
        try {
            bufferedStream.mark(65536);
            validateFileType(bufferedStream, fileName);
            bufferedStream.reset();
        } catch (Exception e) {
            // 校验失败或流 reset 失败 → 关闭流，避免文件描述符泄漏
            closeQuietly(bufferedStream);
            if (e instanceof BusinessException be) {
                throw be;
            }
            log.error("文件流重置失败: {}", e.getMessage(), e);
            throw new BusinessException(CodeEnum.FILE_UPLOAD_FAIL.getCode(), "文件读取异常，请稍后重试");
        }
        // 校验通过，流已回退到起始位置，后续所有权转移给线程池

        String upToken = getAuthToken();
        final String ossKey = type + "/" + UUID.randomUUID() + "_" + fileName;

        // 将流式上传任务提交到专用线程池（AbortPolicy：队列满时直接拒绝，不反噬 Tomcat 线程）
        CompletableFuture<Response> future;
        try {
            future = CompletableFuture.supplyAsync(() -> {
                try {
                    return uploadManager.put(bufferedStream, ossKey, upToken, null, null);
                } catch (QiniuException e) {
                    log.error("OSS 上传失败: {}", e.getMessage(), e);
                    throw new BusinessException(CodeEnum.FILE_UPLOAD_FAIL.getCode(), "文件上传失败: " + e.getMessage());
                }
            }, uploadExecutor);
        } catch (RejectedExecutionException e) {
            // 线程池拒绝 → 任务未执行，需手动关闭流
            closeQuietly(bufferedStream);
            log.error("上传线程池已满，拒绝新任务: {}", e.getMessage());
            throw new BusinessException(CodeEnum.FILE_UPLOAD_FAIL.getCode(), "服务器繁忙，请稍后重试");
        }

        try {
            // 最多等待 30 秒，防止无限阻塞
            Response response = future.get(30, TimeUnit.SECONDS);
            if (!response.isOK()) {
                log.error("OSS 上传响应异常，状态码: {}", response.statusCode);
                throw new BusinessException(CodeEnum.FILE_UPLOAD_FAIL.getCode(), "文件上传失败，服务响应异常");
            }
            return ossProperties.getOssUrl() + "/" + ossKey;
        } catch (TimeoutException e) {
            // 不调用 cancel(true)——上传线程不响应中断，cancel 只是虚假安慰。
            // 30s 后仅向客户端返回超时，后台线程继续完成或自然结束。
            log.error("OSS 上传超时，ossKey={}", ossKey);
            throw new BusinessException(CodeEnum.FILE_UPLOAD_FAIL.getCode(), "文件上传超时，请稍后重试");
        } catch (ExecutionException e) {
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

    /** 安静关闭流，忽略异常 */
    private static void closeQuietly(InputStream stream) {
        try {
            stream.close();
        } catch (IOException ignored) {
            // 流可能已被框架或 SDK 关闭，忽略二次关闭异常
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
