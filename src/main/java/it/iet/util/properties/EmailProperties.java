package it.iet.util.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "email")
@Getter
@Setter
@ToString
public class EmailProperties {
    private String host;
    private String username;
    private String password;
    private int port;
}
