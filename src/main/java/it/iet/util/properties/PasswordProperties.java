package it.iet.util.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "password")
@Getter
@Setter
@ToString
public class PasswordProperties {
    private boolean lowerCase;
    private boolean upperCase;
    private boolean number;
    private boolean special;
    private int minLength;
    private int maxLength;
}
