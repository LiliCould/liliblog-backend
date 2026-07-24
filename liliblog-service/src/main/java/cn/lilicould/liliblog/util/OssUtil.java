package cn.lilicould.liliblog.util;

import cn.lilicould.enums.CodeEnum;
import cn.lilicould.exception.BusinessException;
import cn.lilicould.liliblog.config.properties.OssProperties;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OssUtil {
    private final OssProperties ossProperties;

    /**
     * 生成上传凭证
     * @return 上传凭证
     */
    public String getAuthToken() {
        Auth auth = Auth.create(ossProperties.getAccessKey(), ossProperties.getSecretKey());

        return auth.uploadToken(ossProperties.getBucket());
    }

    public String uploadFile(InputStream inputStream, String fileName,String type) throws QiniuException {

        if (!isValidFileName(fileName)) {
            throw new BusinessException(CodeEnum.NOT_SUPPORTED_FILE_TYPE);
        }

        Configuration cfg = Configuration.create(Region.createWithRegionId("z0")); // 配置华东地区
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
        UploadManager uploadManager = new UploadManager(cfg);

        // 生成上传凭证
        String upToken = getAuthToken();

        // 生成新的文件名，type为目录，文件名为UUID
        fileName = type + "/" + UUID.randomUUID() + "_" + fileName;


        Response response = uploadManager.put(inputStream,fileName,upToken,null, null);
        //解析上传成功的结果
        if (!response.isOK()) {
            throw new BusinessException(CodeEnum.SYSTEM_ERROR);
        }

        // 返回文件的URL
        return "https://oss.lilicould.cn" + "/" + fileName;
    }

    private boolean isValidFileName(String fileName) {

        // 提取文件后缀
        String[] fileNameParts = fileName.split("\\.");
        String fileExtension = fileNameParts[fileNameParts.length - 1];

        // 只支持图片、视频、音频、文档
        // svg有xss风险，但是我个人需要，所以这里不进行过滤，一般需要排除
        return fileExtension.matches("(jpg|jpeg|png|gif|mp4|avi|svg|wmv|mp3|doc|docx|pdf|xls|xlsx|ppt|pptx)");
    }
}
