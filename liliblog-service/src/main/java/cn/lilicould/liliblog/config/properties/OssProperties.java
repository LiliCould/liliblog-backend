package cn.lilicould.liliblog.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "oss")
@Data
public class OssProperties {
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String ossUrl;
    /**
     * 允许上传的 MIME 类型白名单
     */
    private List<String> allowedMediaTypes;
}
