package cn.lilicould.liliblog.common.util;

import cn.lilicould.liliblog.common.enums.CodeEnum;
import cn.lilicould.liliblog.common.exception.BusinessException;
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

    // todo 1.使用UUID生成新文件名并上传，同时返回URL
    // todo 2.异常处理
    public String uploadFile(InputStream inputStream, String fileName,String type) throws QiniuException {

        Configuration cfg = Configuration.create(Region.createWithRegionId("z0")); // 配置华东地区
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
        UploadManager uploadManager = new UploadManager(cfg);

        // 生成上传凭证
        String upToken = getAuthToken();

        // 生成新的文件名，type为目录，文件名为UUID
        fileName = type + "/" + UUID.randomUUID().toString() + "_" + fileName;


        Response response = uploadManager.put(inputStream,fileName,upToken,null, null);
        //解析上传成功的结果
        if (!response.isOK()) {
            throw new BusinessException(CodeEnum.SYSTEM_ERROR);
        }

        // 返回文件的URL
        return "https://oss.lilicould.cn" + "/" + fileName;
    }
}
