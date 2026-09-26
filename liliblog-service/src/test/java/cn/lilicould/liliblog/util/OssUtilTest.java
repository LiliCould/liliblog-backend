package cn.lilicould.liliblog.util;

import cn.lilicould.liliblog.config.properties.OssProperties;
import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.exception.BusinessException;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.unit.DataSize;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OssUtil七牛云文件上传工具测试类
 *
 * @author lilicould
 */
@ExtendWith(MockitoExtension.class)
class OssUtilTest {

    private static final String OSS_URL = "https://cdn.lilicould.cn";

    /** 1x1 透明 PNG 魔数安全内容 */
    private static final byte[] PNG_BYTES = new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
            (byte) 0x89, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x44, 0x41,
            0x54, 0x78, (byte) 0x9C, 0x62, 0x00, 0x01, 0x00, 0x00,
            0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00,
            0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, (byte) 0x44, (byte) 0xAE,
            0x42, 0x60, (byte) 0x82
    };

    @Mock
    private UploadManager uploadManager;

    @Mock
    private Response response;

    private OssProperties ossProperties;

    private OssUtil ossUtil;

    @BeforeEach
    void setUp() {
        ossProperties = new OssProperties();
        ossProperties.setAccessKey("test-access-key");
        ossProperties.setSecretKey("test-secret-key");
        ossProperties.setBucket("test-bucket");
        ossProperties.setOssUrl(OSS_URL);
        ossProperties.setAllowedMediaTypes(List.of("image/png", "text/plain"));
        ossProperties.setSizeLimits(Map.of("avatar", DataSize.ofMegabytes(2), "cover", DataSize.ofMegabytes(5)));
        ossProperties.setDefaultSizeLimit(DataSize.ofMegabytes(40));
        ossProperties.setMaxConcurrentUploads(5);
        ossProperties.setQueueWaitSeconds(1);
        ossProperties.setBlockSize(DataSize.ofMegabytes(1));

        ossUtil = new OssUtil(ossProperties);
        // 单测不走 @PostConstruct，手动触发以初始化信号量；必须在注入 mock 之前，
        // 否则 init() 会用真实的 UploadManager 覆盖掉 mock
        ossUtil.init();
        ReflectionTestUtils.setField(ossUtil, "uploadManager", uploadManager);
    }

    @Test
    void getAuthTokenReturnsToken() {
        String token = ossUtil.getAuthToken();

        assertNotNull(token);
        assertTrue(token.length() > 10);
    }

    @Test
    void uploadFileImageSuccess() throws Exception {
        when(response.isOK()).thenReturn(true);
        when(uploadManager.put(any(InputStream.class), anyString(), anyString(), isNull(), anyString()))
                .thenReturn(response);

        String url = ossUtil.uploadFile(new ByteArrayInputStream(PNG_BYTES), "photo.png", "cover");

        assertTrue(url.startsWith(OSS_URL + "/cover/"));
        assertTrue(url.endsWith("_photo.png"));
        verify(uploadManager).put(any(InputStream.class), anyString(), anyString(), isNull(), eq("image/png"));
    }

    @Test
    void uploadFileTextSuccess() throws Exception {
        when(response.isOK()).thenReturn(true);
        when(uploadManager.put(any(InputStream.class), anyString(), anyString(), isNull(), anyString()))
                .thenReturn(response);

        String url = ossUtil.uploadFile(
                new ByteArrayInputStream("hello world".getBytes(StandardCharsets.UTF_8)), "notes.txt", "file");

        assertTrue(url.startsWith(OSS_URL + "/file/"));
    }

    @Test
    void uploadFileRejectsUnsupportedExtension() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> ossUtil.uploadFile(new ByteArrayInputStream(PNG_BYTES), "malware.exe", "file"));

        assertEquals(CodeEnum.NOT_SUPPORTED_FILE_TYPE.getCode(), e.getCode());
    }

    @Test
    void uploadFileRejectsFakeMagicBytes() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> ossUtil.uploadFile(
                        new ByteArrayInputStream("not a real image".getBytes(StandardCharsets.UTF_8)),
                        "photo.png", "cover"));

        assertEquals(CodeEnum.NOT_SUPPORTED_FILE_TYPE.getCode(), e.getCode());
    }

    @Test
    void uploadFileResponseNotOk() throws Exception {
        when(response.isOK()).thenReturn(false);
        when(uploadManager.put(any(InputStream.class), anyString(), anyString(), isNull(), anyString()))
                .thenReturn(response);

        BusinessException e = assertThrows(BusinessException.class,
                () -> ossUtil.uploadFile(new ByteArrayInputStream(PNG_BYTES), "photo.png", "cover"));

        assertEquals(CodeEnum.FILE_UPLOAD_FAIL.getCode(), e.getCode());
        assertEquals("文件上传失败，服务响应异常", e.getMessage());
    }

    @Test
    void uploadFileQiniuExceptionMappedToBusinessException() throws QiniuException {
        when(uploadManager.put(any(InputStream.class), anyString(), anyString(), isNull(), anyString()))
                .thenThrow(new QiniuException(new IOException("network down")));

        BusinessException e = assertThrows(BusinessException.class,
                () -> ossUtil.uploadFile(new ByteArrayInputStream(PNG_BYTES), "photo.png", "cover"));

        assertEquals(CodeEnum.FILE_UPLOAD_FAIL.getCode(), e.getCode());
        assertTrue(e.getMessage().startsWith("文件上传失败:"));
    }

    // ── 以下为文件上传性能/健壮性改造引入的用例 ──

    @Test
    void uploadFileRejectsOversizeBeforeReadingStream() {
        // 传入一个「读完即抛」的流：若实现真的先读了文件再校验大小，这个用例就会失败
        InputStream exploding = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new AssertionError("超限文件不应被读取");
            }
        };

        BusinessException e = assertThrows(BusinessException.class,
                () -> ossUtil.uploadFile(exploding, "big.png", "cover", DataSize.ofMegabytes(6).toBytes()));

        assertEquals(CodeEnum.FILE_UPLOAD_OVER_SIZE.getCode(), e.getCode());
        assertTrue(e.getMessage().contains("5MB"), "提示语应带上实际配置的上限, 实际=" + e.getMessage());
    }

    @Test
    void uploadFileUsesDefaultSizeLimitForUnconfiguredType() {
        InputStream exploding = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new AssertionError("超限文件不应被读取");
            }
        };

        BusinessException e = assertThrows(BusinessException.class,
                () -> ossUtil.uploadFile(exploding, "big.png", "file", DataSize.ofMegabytes(41).toBytes()));

        assertEquals(CodeEnum.FILE_UPLOAD_OVER_SIZE.getCode(), e.getCode());
        assertTrue(e.getMessage().contains("40MB"), "提示语应回落到 defaultSizeLimit, 实际=" + e.getMessage());
    }

    @Test
    void uploadFileAcceptsSizeWithinLimit() throws Exception {
        when(response.isOK()).thenReturn(true);
        when(uploadManager.put(any(InputStream.class), anyString(), anyString(), isNull(), anyString()))
                .thenReturn(response);

        String url = ossUtil.uploadFile(new ByteArrayInputStream(PNG_BYTES), "photo.png", "avatar",
                PNG_BYTES.length);

        assertTrue(url.startsWith(OSS_URL + "/avatar/"));
    }

    @Test
    void uploadFileStripsPathTraversalFromFileName() throws Exception {
        when(response.isOK()).thenReturn(true);
        when(uploadManager.put(any(InputStream.class), anyString(), anyString(), isNull(), anyString()))
                .thenReturn(response);

        String url = ossUtil.uploadFile(new ByteArrayInputStream(PNG_BYTES), "../../../etc/passwd.png", "cover");

        assertTrue(url.startsWith(OSS_URL + "/cover/"), "目录前缀必须仍是 type, 实际=" + url);
        assertTrue(url.endsWith("_passwd.png"), "应只保留最后一段文件名, 实际=" + url);
        assertFalse(url.contains(".."), "key 中不应残留路径分隔, 实际=" + url);
    }

    @Test
    void uploadFileTruncatesOverlongFileNameKeepingExtension() throws Exception {
        when(response.isOK()).thenReturn(true);
        when(uploadManager.put(any(InputStream.class), anyString(), anyString(), isNull(), anyString()))
                .thenReturn(response);

        String longName = "a".repeat(200) + ".png";
        String url = ossUtil.uploadFile(new ByteArrayInputStream(PNG_BYTES), longName, "cover");

        String key = url.substring((OSS_URL + "/cover/").length());
        String uuid = key.substring(0, key.indexOf('_'));
        assertEquals(36, uuid.length(), "UUID 段长度应为 36, 实际=" + key);
        assertTrue(key.endsWith(".png"), "截断后必须保留扩展名, 实际=" + key);
        // type(6) + / + uuid(36) + _ + 文件名，最坏情况需能落进 varchar(255)
        assertTrue(url.length() <= 255, "URL 长度应留出 varchar(255) 余量, 实际=" + url.length());
    }

    @Test
    void uploadFileReplacesUnsafeCharsKeepingExtension() throws Exception {
        when(response.isOK()).thenReturn(true);
        when(uploadManager.put(any(InputStream.class), anyString(), anyString(), isNull(), anyString()))
                .thenReturn(response);

        String url = ossUtil.uploadFile(new ByteArrayInputStream(PNG_BYTES), "我的 照片(1).png", "cover");

        String key = url.substring((OSS_URL + "/cover/").length());
        assertTrue(key.endsWith(".png"), "清洗后必须保留扩展名, 实际=" + key);
        assertTrue(key.matches("[\\w.\\-]+"), "清洗后只应剩字母数字下划线点连字符, 实际=" + key);
        verify(uploadManager).put(any(InputStream.class), anyString(), anyString(), isNull(), eq("image/png"));
    }

    @Test
    void uploadFileRejectsBlankFileNameWithoutNpe() {
        // 文件名为空时无法做类型校验，应被明确拒绝而不是抛 NPE 或生成畸形 key
        BusinessException e = assertThrows(BusinessException.class,
                () -> ossUtil.uploadFile(new ByteArrayInputStream(PNG_BYTES), "   ", "cover"));

        assertEquals(CodeEnum.NOT_SUPPORTED_FILE_TYPE.getCode(), e.getCode());
    }

    // ── 以下为堆内存保护（并发闸门）相关用例 ──

    @Test
    void uploadFileReleasesPermitOnSuccess() throws Exception {
        when(response.isOK()).thenReturn(true);
        when(uploadManager.put(any(InputStream.class), anyString(), anyString(), isNull(), anyString()))
                .thenReturn(response);

        // 连续上传远超并发上限的次数：若成功路径漏放许可，第 6 次就会开始失败
        for (int i = 0; i < 20; i++) {
            assertNotNull(ossUtil.uploadFile(new ByteArrayInputStream(PNG_BYTES), "photo.png", "cover"));
        }
    }

    @Test
    void uploadFileReleasesPermitOnFailure() throws Exception {
        when(response.isOK()).thenReturn(false);
        when(uploadManager.put(any(InputStream.class), anyString(), anyString(), isNull(), anyString()))
                .thenReturn(response);

        // 失败路径若漏放许可，信号量会在 max-concurrent-uploads 次后耗尽
        for (int i = 0; i < 20; i++) {
            assertThrows(BusinessException.class,
                    () -> ossUtil.uploadFile(new ByteArrayInputStream(PNG_BYTES), "photo.png", "cover"));
        }
    }

    @Test
    void uploadFileReleasesPermitOnQiniuException() throws QiniuException {
        when(uploadManager.put(any(InputStream.class), anyString(), anyString(), isNull(), anyString()))
                .thenThrow(new QiniuException(new IOException("network down")));

        for (int i = 0; i < 20; i++) {
            assertThrows(BusinessException.class,
                    () -> ossUtil.uploadFile(new ByteArrayInputStream(PNG_BYTES), "photo.png", "cover"));
        }
    }

    @Test
    void uploadFileReleasesPermitWhenTypeValidationFails() {
        // 校验失败发生在申请许可之前，不应占用名额
        for (int i = 0; i < 20; i++) {
            assertThrows(BusinessException.class,
                    () -> ossUtil.uploadFile(new ByteArrayInputStream(PNG_BYTES), "malware.exe", "file"));
        }
    }

    @Test
    void uploadFileFailsFastWhenAllPermitsHeld() throws QiniuException {
        // 故意不 stub uploadManager：本用例要断言排队失败时根本没走到 SDK
        Semaphore permits = (Semaphore) ReflectionTestUtils.getField(ossUtil, "uploadPermits");
        assertNotNull(permits, "uploadPermits 应已在 init() 中初始化");
        // 预占满全部许可，模拟并发上传已饱和
        for (int i = 0; i < ossProperties.getMaxConcurrentUploads(); i++) {
            assertTrue(permits.tryAcquire());
        }

        try {
            BusinessException e = assertThrows(BusinessException.class,
                    () -> ossUtil.uploadFile(new ByteArrayInputStream(PNG_BYTES), "photo.png", "cover"));
            assertEquals(CodeEnum.FILE_UPLOAD_FAIL.getCode(), e.getCode());
            assertTrue(e.getMessage().contains("较多"), "排队耗尽应提示繁忙而非上传失败, 实际=" + e.getMessage());
            // 排队失败不应消耗上游资源
            verify(uploadManager, never()).put(any(InputStream.class), anyString(), anyString(),
                    isNull(), anyString());
        } finally {
            // 归还预占的许可，避免影响其它用例
            permits.release(ossProperties.getMaxConcurrentUploads());
        }
    }
}