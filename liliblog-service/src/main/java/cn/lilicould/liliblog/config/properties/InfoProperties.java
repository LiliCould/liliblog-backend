package cn.lilicould.liliblog.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.info")
@Data
public class InfoProperties {

    private String adminEmail;
}
