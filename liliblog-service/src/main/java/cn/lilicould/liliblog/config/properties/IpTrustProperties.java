package cn.lilicould.liliblog.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "ip-trust")
@Data
public class IpTrustProperties {
    private List<String> trustedProxies = new ArrayList<>();
}
