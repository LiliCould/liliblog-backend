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

    /**
     * 是否开启前端直传（关闭后只保留后端中转上传）
     */
    private boolean directUploadEnabled = true;

    /**
     * 七牛上传域名。z0 区域为 https://upload-z0.qiniup.com，需与 region 匹配
     */
    private String uploadUrl = "https://upload-z0.qiniup.com";

    /**
     * 直传令牌有效期（秒）
     */
    private long tokenExpireSeconds = 600;

    /**
     * 待确认记录保留时长（小时），超过后由清理任务删除对象并标记过期
     */
    private long pendingExpireHours = 24;

    /**
     * 确认上传时是否拉取对象头部做魔数校验
     */
    private boolean magicCheckOnConfirm = true;

    /**
     * 魔数校验读取的最大字节数
     */
    private int magicCheckBytes = 64 * 1024;

    /**
     * 每个用户每天允许申请的上传令牌数
     */
    private int dailyCountLimit = 100;

    /**
     * 每个用户每天允许上传的总字节数
     */
    private DataSize dailyBytesLimit = DataSize.ofGigabytes(2);

    /**
     * 每个用户同时挂起的待确认记录上限
     */
    private int maxPendingPerUser = 20;

    /**
     * 魔数校验使用的下载基础地址，默认使用 ossUrl。
     * 私有桶或开启防盗链时可配置为可访问的源站域名
     */
    private String checkBaseUrl;

    /**
     * 是否自动清理无引用的孤儿文件。默认关闭，建议先观察日志再开启
     */
    private boolean orphanCleanEnabled = false;

    /**
     * 孤儿文件保留天数：成功上传后超过该天数仍无任何引用才会被清理
     */
    private int orphanRetentionDays = 7;
}
