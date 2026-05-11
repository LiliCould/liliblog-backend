package cn.lilicould.liliblog.common.util;

import cn.lilicould.liliblog.config.properties.OssProperties;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;

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
    public void uploadFile(InputStream inputStream, String fileName) {

        Configuration cfg = Configuration.create(Region.createWithRegionId("z0")); // 配置华东地区
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
        UploadManager uploadManager = new UploadManager(cfg);

        // 生成上传凭证
        String upToken = getAuthToken();

        try {
            Response response = uploadManager.put(inputStream,fileName,upToken,null, null);
            //解析上传成功的结果
            DefaultPutRet putRet = new ObjectMapper().readValue(response.bodyString(),DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
        } catch (QiniuException ex) {
            if (ex.response != null) {
                log.error(ex.getMessage());
                try {
                    String body = ex.response.toString();
                    System.err.println(body);
                } catch (Exception ignored) {
                    log.error(ignored.getMessage());
                }
            }
        }
    }
}
