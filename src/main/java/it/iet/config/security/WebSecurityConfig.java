package it.iet.config.security;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import it.iet.config.JsonTriplet;
import it.iet.config.ReadFromJson;
import it.iet.config.exceptions.impl.RoleNotFoundException;
import it.iet.config.exceptions.impl.StillUnimplementedException;
import it.iet.interfaces.facade.ServerService;
import it.iet.util.Constants;
import it.iet.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ForwardedHeaderFilter;

import javax.servlet.Filter;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMongoAuditing
@EnableGlobalMethodSecurity(prePostEnabled = true)
@OpenAPIDefinition(info = @Info(title = "Authentication Framework API", version = "v1"))
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${email.host}")
    private String host;
    @Value("${email.username}")
    private String username;
    @Value("${email.password}")
    private String password;
    @Value("${email.port}")
    private int port;

    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final UserDetailsService userDetailsService;
    private final ReadFromJson readFromJson;
    private final ServerService serverService;

    public WebSecurityConfig(JwtAuthenticationEntryPoint unauthorizedHandler, UserDetailsService userDetailsService, ReadFromJson readFromJson, ServerService serverService) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.userDetailsService = userDetailsService;
        this.readFromJson = readFromJson;
        this.serverService = serverService;
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Autowired
    public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(this.userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationTokenFilter authenticationTokenFilterBean() {
        return new JwtAuthenticationTokenFilter();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(Constants.origins));
        configuration.setAllowedMethods(Arrays.asList("POST", "PUT", "GET", "DELETE"));
        configuration.addAllowedHeader("*");
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry = httpSecurity.antMatcher("/**").authorizeRequests();

        httpSecurity
                .csrf().disable().cors().and()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests();
        for (String file : Constants.openaApiJsonNameList) {
            var openAPI = readFromJson.getJsonContent(file);
            List<Server> serverList = openAPI.getServers();
            var paths = openAPI.getPaths();
            Set<String> keyPath = paths.keySet();
            LinkedList<JsonTriplet> jsonTriplets = createTriplets(paths, keyPath, serverList.get(0).getUrl());

            for (JsonTriplet jt : jsonTriplets) {
                if (jt.getRoleList().isEmpty()) {
                    registry.antMatchers(Constants.GATEWAY_URI + jt.getPath()).permitAll();
                } else {
                    for (String s : jt.getRoleList()) {
                        registry.antMatchers(Constants.GATEWAY_URI + jt.getPath()).access("hasAuthority('" + s + "')");
                    }
                }
            }
        }

        registry.antMatchers(
                "/",
                "/*.html",
                "/favicon.ico",
                "/*.json",
                "/**/*.html",
                "/**/*.css",
                "/**/*.js",
                "/public/**",
                "**swagger**"
        ).permitAll();

        registry.anyRequest().authenticated();

        httpSecurity.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
        httpSecurity.headers().cacheControl();
    }


    @Bean
    public Filter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @Bean
    public JavaMailSender getJavaMailSender() {
        return createJavaMailSender(host, port, username, password);
    }

    private JavaMailSender createJavaMailSender(String host, int port, String username, String password) {
        var mailSender = new JavaMailSenderImpl();

        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        // these are properties for gmail smtp
        var properties = mailSender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.debug", "false");

        return mailSender;
    }

    private LinkedList<String> returnSplittedRoles(List<SecurityRequirement> securityRequirementList) {
        LinkedList<String> roles = new LinkedList<>();

        if (securityRequirementList != null && Boolean.FALSE.equals(securityRequirementList.isEmpty())) {
            if (securityRequirementList.size() > 1) {
                throw new StillUnimplementedException("The reading of more than one value here is still unimplemented", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            var securityRequirement = securityRequirementList.get(0);
            Set<String> keySet = securityRequirement.keySet();
            for (String key : keySet) {
                if (!key.equals(Constants.andRolesKey)) {
                    throw new RoleNotFoundException("The roles key " + key + " is invalid", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                roles.addAll(securityRequirement.get(key));
            }
        }
        return roles;
    }

    private LinkedList<JsonTriplet> createTriplets(Paths paths, Set<String> keyPath, String url) throws URISyntaxException {
        LinkedList<JsonTriplet> jsonTriplets = new LinkedList<>();
        String oldKey = null;
        for (String key : keyPath) { // key is the http path
            if (!key.equals(oldKey)) {
                String serverName = Utils.getServerName(key); // take the ms name from request mapping url
                this.serverService.addServer(url, serverName);
                oldKey = key;
            }
            var pathItem = paths.get(key);
            if (pathItem.getGet() != null) {
                var triplet = new JsonTriplet();
                triplet.setPath(key);
                triplet.setHttpMethod("GET");
                triplet.setRoleList(returnSplittedRoles(pathItem.getGet().getSecurity()));
                jsonTriplets.add(triplet);
            }
            if (pathItem.getDelete() != null) {
                var triplet = new JsonTriplet();
                triplet.setPath(key);
                triplet.setHttpMethod("DELETE");
                triplet.setRoleList(returnSplittedRoles(pathItem.getDelete().getSecurity()));
                jsonTriplets.add(triplet);
            }
            if (pathItem.getHead() != null) {
                var triplet = new JsonTriplet();
                triplet.setPath(key);
                triplet.setHttpMethod("HEAD");
                triplet.setRoleList(returnSplittedRoles(pathItem.getHead().getSecurity()));
                jsonTriplets.add(triplet);
            }
            if (pathItem.getOptions() != null) {
                var triplet = new JsonTriplet();
                triplet.setPath(key);
                triplet.setHttpMethod("OPTIONS");
                triplet.setRoleList(returnSplittedRoles(pathItem.getOptions().getSecurity()));
                jsonTriplets.add(triplet);
            }
            if (pathItem.getPatch() != null) {
                var triplet = new JsonTriplet();
                triplet.setPath(key);
                triplet.setHttpMethod("PATCH");
                triplet.setRoleList(returnSplittedRoles(pathItem.getPatch().getSecurity()));
                jsonTriplets.add(triplet);
            }
            if (pathItem.getPost() != null) {
                var triplet = new JsonTriplet();
                triplet.setPath(key);
                triplet.setHttpMethod("POST");
                triplet.setRoleList(returnSplittedRoles(pathItem.getPost().getSecurity()));
                jsonTriplets.add(triplet);
            }
            if (pathItem.getPut() != null) {
                var triplet = new JsonTriplet();
                triplet.setPath(key);
                triplet.setHttpMethod("PUT");
                triplet.setRoleList(returnSplittedRoles(pathItem.getPut().getSecurity()));
                jsonTriplets.add(triplet);
            }
            if (pathItem.getTrace() != null) {
                var triplet = new JsonTriplet();
                triplet.setPath(key);
                triplet.setHttpMethod("TRACE");
                triplet.setRoleList(returnSplittedRoles(pathItem.getTrace().getSecurity()));
                jsonTriplets.add(triplet);
            }

        }
        return jsonTriplets;
    }


}
