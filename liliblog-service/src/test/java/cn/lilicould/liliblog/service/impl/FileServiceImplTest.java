package cn.lilicould.liliblog.service.impl;

import cn.lilicould.liliblog.cache.RedisHelper;
import cn.lilicould.liliblog.config.properties.OssProperties;
import cn.lilicould.liliblog.constant.StatusConstant;
import cn.lilicould.liliblog.domain.security.SecurityUser;
import cn.lilicould.liliblog.entity.FileRecord;
import cn.lilicould.liliblog.entity.User;
import cn.lilicould.liliblog.enums.CodeEnum;
import cn.lilicould.liliblog.exception.BusinessException;
import cn.lilicould.liliblog.mapper.FileRecordMapper;
import cn.lilicould.liliblog.request.UploadConfirmRequest;
import cn.lilicould.liliblog.request.UploadTokenRequest;
import cn.lilicould.liliblog.response.FileRecordVO;
import cn.lilicould.liliblog.response.UploadTokenVO;
import cn.lilicould.liliblog.util.OssUtil;
import com.qiniu.storage.model.FileInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.unit.DataSize;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * FileServiceImpl 单元测试：覆盖令牌签发和上传确认的关键权限边界
 */
@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private FileRecordMapper fileRecordMapper;
    @Mock
    private OssUtil ossUtil;
    @Mock
    private RedisHelper redisHelper;

    private OssProperties properties;
    private FileServiceImpl fileService;

    @BeforeEach
    void setUp() {
        properties = new OssProperties();
        properties.setBucket("test-bucket");
        properties.setOssUrl("https://oss.test");
        properties.setUploadUrl("https://upload.test");
        properties.setAllowedMediaTypes(List.of("image/png"));
        properties.setSizeLimits(Map.of("cover", DataSize.ofMegabytes(5)));
        properties.setDefaultSizeLimit(DataSize.ofMegabytes(40));
        properties.setDirectUploadEnabled(true);
        properties.setTokenExpireSeconds(600);
        properties.setPendingExpireHours(24);
        properties.setMaxPendingPerUser(20);
        properties.setDailyCountLimit(100);
        properties.setDailyBytesLimit(DataSize.ofGigabytes(2));
        properties.setMagicCheckOnConfirm(false);

        fileService = new FileServiceImpl(properties, ossUtil, redisHelper);
        ReflectionTestUtils.setField(fileService, "baseMapper", fileRecordMapper);
        loginAs(7L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void issueUploadTokenCreatesPendingRecordScopedToUser() {
        when(ossUtil.sanitizeFileName("photo.png")).thenReturn("photo.png");
        when(ossUtil.extensionOf("photo.png")).thenReturn("png");
        when(ossUtil.generateUploadToken(anyString(), anyLong(), anyString(), anyLong()))
                .thenReturn("upload-token");
        when(fileRecordMapper.insert(any(FileRecord.class))).thenAnswer(invocation -> {
            FileRecord record = invocation.getArgument(0);
            record.setId(1L);
            return 1;
        });

        UploadTokenRequest request = new UploadTokenRequest();
        request.setType("cover");
        request.setFileName("photo.png");
        request.setFileSize(100L);

        UploadTokenVO token = fileService.issueUploadToken(request);

        assertEquals(1L, token.getFileId());
        assertEquals("upload-token", token.getToken());
        assertEquals("https://upload.test", token.getUploadUrl());
        assertTrue(token.getKey().startsWith("cover/7/"));
        assertTrue(token.getKey().endsWith(".png"));
        assertTrue(token.getUrl().equals("https://oss.test/" + token.getKey()));

        ArgumentCaptor<FileRecord> captor = ArgumentCaptor.forClass(FileRecord.class);
        verify(fileRecordMapper).insert(captor.capture());
        FileRecord saved = captor.getValue();
        assertEquals(7L, saved.getUserId());
        assertEquals(StatusConstant.FILE_PENDING, saved.getStatus());
        assertEquals(StatusConstant.FILE_SOURCE_DIRECT, saved.getUploadSource());
        assertEquals("image/png", saved.getMimeType());
    }

    @Test
    void confirmUploadMarksSuccessAfterStat() throws Exception {
        FileRecord record = pendingRecord(7L);
        when(fileRecordMapper.selectById(1L)).thenReturn(record);
        when(ossUtil.stat(record.getFileKey())).thenReturn(fileInfo("etag", 100L));
        when(fileRecordMapper.updateById(any(FileRecord.class))).thenReturn(1);

        FileRecordVO result = fileService.confirmUpload(confirmRequest("etag"));

        assertEquals(StatusConstant.FILE_SUCCESS, result.getStatus());
        assertEquals("etag", result.getHash());
        assertEquals(100L, result.getFileSize());
        verify(fileRecordMapper).updateById(record);
    }

    @Test
    void confirmUploadRejectsOtherUsersRecord() {
        when(fileRecordMapper.selectById(1L)).thenReturn(pendingRecord(8L));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> fileService.confirmUpload(confirmRequest("etag")));

        assertEquals(CodeEnum.FILE_RECORD_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void confirmUploadRejectsHashMismatchAndDeletesObject() throws Exception {
        FileRecord record = pendingRecord(7L);
        when(fileRecordMapper.selectById(1L)).thenReturn(record);
        when(ossUtil.stat(record.getFileKey())).thenReturn(fileInfo("real-etag", 100L));
        when(fileRecordMapper.updateById(any(FileRecord.class))).thenReturn(1);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> fileService.confirmUpload(confirmRequest("fake-etag")));

        assertEquals(CodeEnum.FILE_VERIFY_FAILED.getCode(), exception.getCode());
        verify(ossUtil).deleteQuietly(record.getFileKey());
        assertEquals(StatusConstant.FILE_REJECTED, record.getStatus());
    }

    @Test
    void confirmUploadRejectsMimeMismatch() throws Exception {
        properties.setMagicCheckOnConfirm(true);
        FileRecord record = pendingRecord(7L);
        when(fileRecordMapper.selectById(1L)).thenReturn(record);
        when(ossUtil.stat(record.getFileKey())).thenReturn(fileInfo("etag", 100L));
        when(ossUtil.detectRemoteMime(record.getFileKey(), properties.getMagicCheckBytes()))
                .thenReturn("text/plain");
        when(ossUtil.isMimeConsistent("image/png", "text/plain")).thenReturn(false);
        when(fileRecordMapper.updateById(any(FileRecord.class))).thenReturn(1);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> fileService.confirmUpload(confirmRequest("etag")));

        assertEquals(CodeEnum.NOT_SUPPORTED_FILE_TYPE.getCode(), exception.getCode());
        verify(ossUtil).deleteQuietly(record.getFileKey());
        assertEquals(StatusConstant.FILE_REJECTED, record.getStatus());
    }

    private UploadConfirmRequest confirmRequest(String hash) {
        UploadConfirmRequest request = new UploadConfirmRequest();
        request.setFileId(1L);
        request.setHash(hash);
        return request;
    }

    private FileRecord pendingRecord(Long userId) {
        return FileRecord.builder()
                .id(1L)
                .userId(userId)
                .fileKey("cover/7/202610/abc.png")
                .url("https://oss.test/cover/7/202610/abc.png")
                .originalName("abc.png")
                .mimeType("image/png")
                .fileSize(100L)
                .bizType("cover")
                .uploadSource(StatusConstant.FILE_SOURCE_DIRECT)
                .status(StatusConstant.FILE_PENDING)
                .deleted(StatusConstant.NOT_DELETED)
                .build();
    }

    private FileInfo fileInfo(String hash, long size) {
        FileInfo info = new FileInfo();
        info.hash = hash;
        info.fsize = size;
        info.mimeType = "image/png";
        return info;
    }

    private void loginAs(Long userId) {
        User user = User.builder()
                .id(userId)
                .username("user" + userId)
                .role(1)
                .status(1)
                .build();
        SecurityUser securityUser = new SecurityUser(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        securityUser, null, securityUser.getAuthorities()));
    }
}
