package cn.lilicould.liliblog.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "http-only-cookies")
@Data
public class HttpOnlyCookiesProperties {
    private boolean ssl;
}
