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
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class OssUtil {
    private final OssProperties ossProperties;

    private UploadManager uploadManager;

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
        if (!isValidFileName(fileName)) {
            throw new BusinessException(CodeEnum.NOT_SUPPORTED_FILE_TYPE);
        }

        String upToken = getAuthToken();
        // 生成新的文件名：type/UUID_原文件名
        final String ossKey = type + "/" + UUID.randomUUID() + "_" + fileName;

        // 将上传任务提交到专用线程池
        CompletableFuture<Response> future = CompletableFuture.supplyAsync(() -> {
            try {
                return uploadManager.put(inputStream, ossKey, upToken, null, null);
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

    private boolean isValidFileName(String fileName) {
        // 通过 MediaTypeFactory 根据文件名解析 MIME 类型
        Optional<MediaType> mediaTypeOpt = MediaTypeFactory.getMediaType(fileName);
        log.info("OSS 上传文件类型: {}", mediaTypeOpt.orElse(null));
        if (mediaTypeOpt.isEmpty()) {
            return false;
        }
        String mimeType = mediaTypeOpt.get().toString();
        // 与配置的白名单比对
        return ossProperties.getAllowedMediaTypes().stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(mimeType));
    }
}
