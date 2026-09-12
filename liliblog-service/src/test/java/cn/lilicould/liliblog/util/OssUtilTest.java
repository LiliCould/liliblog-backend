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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
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

        ossUtil = new OssUtil(ossProperties);
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
        when(uploadManager.put(any(InputStream.class), anyString(), anyString(), isNull(), isNull()))
                .thenReturn(response);

        String url = ossUtil.uploadFile(new ByteArrayInputStream(PNG_BYTES), "photo.png", "cover");

        assertTrue(url.startsWith(OSS_URL + "/cover/"));
        assertTrue(url.endsWith("_photo.png"));
        verify(uploadManager).put(any(InputStream.class), anyString(), anyString(), isNull(), isNull());
    }

    @Test
    void uploadFileTextSuccess() throws Exception {
        when(response.isOK()).thenReturn(true);
        when(uploadManager.put(any(InputStream.class), anyString(), anyString(), isNull(), isNull()))
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
        when(uploadManager.put(any(InputStream.class), anyString(), anyString(), isNull(), isNull()))
                .thenReturn(response);

        BusinessException e = assertThrows(BusinessException.class,
                () -> ossUtil.uploadFile(new ByteArrayInputStream(PNG_BYTES), "photo.png", "cover"));

        assertEquals(CodeEnum.FILE_UPLOAD_FAIL.getCode(), e.getCode());
        assertEquals("文件上传失败，服务响应异常", e.getMessage());
    }

    @Test
    void uploadFileQiniuExceptionMappedToBusinessException() throws QiniuException {
        when(uploadManager.put(any(InputStream.class), anyString(), anyString(), isNull(), isNull()))
                .thenThrow(new QiniuException(new IOException("network down")));

        BusinessException e = assertThrows(BusinessException.class,
                () -> ossUtil.uploadFile(new ByteArrayInputStream(PNG_BYTES), "photo.png", "cover"));

        assertEquals(CodeEnum.FILE_UPLOAD_FAIL.getCode(), e.getCode());
        assertTrue(e.getMessage().startsWith("文件上传失败:"));
    }
}