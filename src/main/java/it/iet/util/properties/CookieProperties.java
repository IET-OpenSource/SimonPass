package it.iet.util.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "customcookie")
@Getter
@Setter
@ToString
public class CookieProperties {
    private int ACTmaxAge;
    private int RFTmaxAge;
    private int LTmaxAge;
    private boolean secure;
    private boolean httpOnly;
    private String path;


}
