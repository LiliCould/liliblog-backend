package cn.lilicould.liliblog.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "oss")
@Data
public class OssProperties {
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String ossUrl;

    /**
     * 七牛区域 ID，z0=华东 z1=华北 z2=华南 na0=北美。填错会导致每次上传都跨区，显著变慢
     */
    private String region = "z0";

    /**
     * SDK 连接超时（秒）
     */
    private int connectTimeout = 10;

    /**
     * SDK 单次 socket 写超时（秒）。这是「单次写」的上限，不是整个上传的总时长。
     * 传 0 表示不限制（SDK 默认值）
     */
    private int writeTimeout = 60;

    /**
     * 分片上传的分片大小。七牛要求 1MB~1GB。
     * <p>SDK 每次只驻留一个分片，所以这直接决定了「单个在途上传占多少堆内存」。
     * 调小会略微增加分片数（每片多一次往返），但在带宽受限的链路上耗时增量可忽略。</p>
     */
    private DataSize blockSize = DataSize.ofMegabytes(1);

    /**
     * 同时进行的上传数上限。
     * <p>堆内存最坏值 ≈ 该值 × blockSize。这是不用线程池、改为同步上传后唯一的并发闸门，
     * 调高会线性放大内存峰值，调低则请求会在入口排队。</p>
     */
    private int maxConcurrentUploads = 5;

    /**
     * 等待上传许可的最长时间（秒），超时返回「服务器繁忙」。
     * <p>应显著大于单次上传耗时，否则慢上行链路下会出现误判。</p>
     */
    private int queueWaitSeconds = 60;

    /**
     * 各业务类型的大小上限，key 为 cover/avatar/image/file。
     * 未显式配置的类型使用 defaultSizeLimit
     */
    private Map<String, DataSize> sizeLimits = new LinkedHashMap<>();

    /**
     * 未在 sizeLimits 中配置的业务类型的兜底上限
     */
    private DataSize defaultSizeLimit = DataSize.ofMegabytes(40);

    /**
     * 允许上传的 MIME 类型白名单
     */
    private List<String> allowedMediaTypes = new ArrayList<>();
}
