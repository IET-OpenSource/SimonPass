package it.iet;

import it.iet.util.properties.CookieProperties;
import it.iet.util.properties.EmailProperties;
import it.iet.util.properties.FrontendProperties;
import it.iet.util.properties.PasswordProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@EnableConfigurationProperties({PasswordProperties.class, EmailProperties.class, CookieProperties.class, FrontendProperties.class})
public class AuthFrameworkApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(AuthFrameworkApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(AuthFrameworkApplication.class);
    }
}
