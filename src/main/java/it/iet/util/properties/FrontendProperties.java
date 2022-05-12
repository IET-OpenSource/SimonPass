package it.iet.util.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "frontend")
@Getter
@Setter
@ToString
public class FrontendProperties {

    private String url;
}
