package it.iet.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import it.iet.config.exceptions.impl.AuthenticationFailedException;
import it.iet.config.exceptions.impl.StillUnimplementedException;
import it.iet.config.exceptions.impl.TokenNotRefreshedException;
import it.iet.infrastructure.mongo.entity.SecurityTriplet;
import it.iet.infrastructure.mongo.entity.token.BaseToken;
import it.iet.infrastructure.mongo.entity.token.TokenType;
import it.iet.infrastructure.mongo.entity.user.User;
import it.iet.interfaces.facade.dto.user.LoginServiceDTO;
import it.iet.model.repository.BaseTokenRepository;
import it.iet.model.repository.SecurityTripletRepository;
import it.iet.model.repository.UserRepository;
import it.iet.util.Constants;
import it.iet.util.CookieManager;
import it.iet.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenUtil implements Serializable {

    static final String CLAIM_KEY_SUB = "sub";
    static final String CLAIM_KEY_AUDIENCE = "audience";
    static final String CLAIM_KEY_CREATED = "iat"; //issued at, creation time
    static final String CLAIM_KEY_AUTHORITIES = "roles"; //roles
    static final String CLAIM_KEY_IS_ENABLED = "isEnabled"; //user is enabled
    static final String CLAIM_KEY_USER = "user";
    static final String CLAIM_KEY_TOKEN_LOGIN = "login_token";
    static final String CLAIM_KEY_TOKEN_SERIES = "token_series";

    private static final String AUDIENCE_MOBILE = "mobile";
    private static final String AUDIENCE_TABLET = "tablet";

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final BaseTokenRepository tokenRepository;
    private final SecurityTripletRepository repository;

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    public JwtTokenUtil(AuthenticationManager authenticationManager, UserRepository userRepository, BaseTokenRepository tokenRepository, SecurityTripletRepository repository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.repository = repository;
    }

    public Authentication getAuthentication(JwtAuthenticationRequest authenticationRequest) {
        try {
            return authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(), authenticationRequest.getPassword()));
        } catch (org.springframework.security.authentication.DisabledException disabledException) {
            log.error("--------------------------------------------------------------------------DISABLED EXCEPTION");
        } catch (org.springframework.security.authentication.LockedException lockedException) {
            log.error("--------------------------------------------------------------------------LOCKED EXCEPTION");
        } catch (org.springframework.security.authentication.BadCredentialsException badCredentialsException) {
            log.error("--------------------------------------------------------------------------BAD CRED EXCEPTION");
        }
        throw new AuthenticationFailedException("Authentication failed", HttpStatus.UNAUTHORIZED);
    }

    public String getUserIdFromToken(String token) {
        String userId;
        try {
            final var claims = getClaimsFromToken(token);
            userId = claims.getSubject();
        } catch (Exception e) {
            userId = null;
        }
        return userId;
    }

    public JwtUser getUserDetails(String token) {
        if (token == null) {
            return null;
        }
        try {
            final var claims = getClaimsFromToken(token);
            List<SimpleGrantedAuthority> authorities = null;
            if (claims.get(CLAIM_KEY_AUTHORITIES) != null)
                authorities = ((List<String>) claims.get(CLAIM_KEY_AUTHORITIES)).stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
            return new JwtUser(claims.getSubject(), "", authorities, (boolean) claims.get(CLAIM_KEY_IS_ENABLED));
        } catch (Exception e) {
            return null;
        }
    }

    public String getLoginTokenFromToken(String token) {
        String result;
        try {
            final var claims = getClaimsFromToken(token);
            result = (String) claims.get(CLAIM_KEY_TOKEN_LOGIN);
        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    public String getTokenSeriesFromToken(String token) {
        String result;
        try {
            final var claims = getClaimsFromToken(token);
            result = (String) claims.get(CLAIM_KEY_TOKEN_SERIES);
        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    public Date getCreatedDateFromToken(String token) {
        Date created;
        try {
            final var claims = getClaimsFromToken(token);
            created = new Date((Long) claims.get(CLAIM_KEY_CREATED));
        } catch (Exception e) {
            created = null;
        }
        return created;
    }

    public Date getExpirationDateFromToken(String token) {
        Date expire;
        try {
            final var claims = getClaimsFromToken(token);
            expire = claims.getExpiration();
        } catch (Exception e) {
            expire = null;
        }
        return expire;
    }

    public String getAudienceFromToken(String token) {
        String audience;
        try {
            final var claims = getClaimsFromToken(token);
            audience = (String) claims.get(CLAIM_KEY_AUDIENCE);
        } catch (Exception e) {
            audience = null;
        }
        return audience;
    }

    public Claims getClaimsFromToken(String token) throws TokenNotRefreshedException {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            throw new TokenNotRefreshedException("Error Refreshing token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return claims;
    }

    public String generateToken(UserDetails userDetails, TokenType type) {
        Map<String, Object> claims = initializeToken(userDetails);
        Optional<User> opt = userRepository.findByEmail(userDetails.getUsername());
        opt.ifPresent(user -> claims.put(CLAIM_KEY_SUB, user.get_id())); // we put only the user id in the token
        return generateToken(claims, type);
    }

    public String generateLoginToken(UserDetails userDetails, SecurityTriplet triplet) {
        Map<String, Object> claims = initializeToken(userDetails);
        Optional<User> opt = userRepository.findByEmail(userDetails.getUsername());
        opt.ifPresent(user -> claims.put(CLAIM_KEY_SUB, user.get_id())); // we put only the user id in the token
        claims.put(CLAIM_KEY_TOKEN_LOGIN, triplet.getToken());
        claims.put(CLAIM_KEY_TOKEN_SERIES, triplet.getIdentifier());
        return generateToken(claims, TokenType.LOGIN);
    }

    public Boolean canTokenBeRefreshed(String accessToken, String refreshToken) {
        // condition to refresh token, if the refresh token is valid
        // To validate refresh_token and create a new access_token check that refresh_token is in db, is valid and not expired
        try {
            getClaimsFromToken(refreshToken);
            return (!isTokenExpired(refreshToken) || ignoreTokenExpiration(refreshToken)) && findAndCheck(accessToken, refreshToken);
        } catch (TokenNotRefreshedException e) {
            return false;
        }
    }

    public LoginServiceDTO refreshToken(String accessToken, String refreshToken) throws TokenNotRefreshedException {
        if (Boolean.FALSE.equals(canTokenBeRefreshed(accessToken, refreshToken))){
            throw new TokenNotRefreshedException(Constants.REFRESH_TOKEN_ERROR, HttpStatus.BAD_REQUEST);
        } else {
            var loginServiceDTO = new LoginServiceDTO();
            try {
                final var claims = getClaimsFromToken(refreshToken);
                claims.put(CLAIM_KEY_CREATED, new Date());
                //create new access token and put in element1
                loginServiceDTO.setAccessToken(generateToken(claims, TokenType.ACCESS));
                //update old refresh token with new access token associated
                loginServiceDTO.setRefreshToken(tokenRepository.updateRefreshToken(refreshToken, loginServiceDTO.getAccessToken(), accessToken).getContent());
                return loginServiceDTO;
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new TokenNotRefreshedException(Constants.REFRESH_TOKEN_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    //validate access token used to make API calls
    public LoginServiceDTO validateToken(HttpServletRequest request, String accessToken, UserDetails userDetails) {
        var logCookie = CookieManager.retrieveCookie(request, Constants.LOGIN_COOKIE_NAME);
        var answer = new LoginServiceDTO();
        if (logCookie != null) { // triplet exists so we need to check it
            String cryptedTriplet = logCookie.getValue();
            if (checkIfLoginCookieContains(cryptedTriplet)) {
                answer.setCryptedTriplet(cryptedTriplet);
            } else {
                //in this case i have to delete everything about user triplets and fail process
                deleteAllTriplets(cryptedTriplet);
            }
        } else {
            JwtUser user = (JwtUser) userDetails;
            final String username = getUserIdFromToken(accessToken);
            try {
                getClaimsFromToken(accessToken);
                if (username.equals(user.getUsername()) && Boolean.FALSE.equals(isTokenExpired(accessToken))) { //check also signature
                    answer.setAccessToken(accessToken);
                } else {
                    answer.setAccessToken(null);
                }
            } catch (TokenNotRefreshedException e) {
                answer.setAccessToken(null);
            }
        }
        return answer;
    }


    public String restoreCookie(HttpServletRequest request, UserDetails userDetails) {
        var logCookie = CookieManager.retrieveCookie(request, Constants.LOGIN_COOKIE_NAME);
        if (logCookie != null) {
            String cryptedTriplet = logCookie.getValue();
            String identifier = this.getTokenSeriesFromToken(cryptedTriplet);
            String usr = this.getUserIdFromToken(cryptedTriplet);
            String token = this.getLoginTokenFromToken(cryptedTriplet);
            Optional<SecurityTriplet> opt = repository.retrieveSingleTriplet(usr, token, identifier);
            if (opt.isPresent()) {
                var newToken = Utils.generateCryptString(UUID.randomUUID().toString());
                var securityTriplet = repository.createTriplet(usr, newToken, identifier);
                return this.generateLoginToken(userDetails, securityTriplet);
            } else {
                throw new TokenNotRefreshedException("Security Triplet not found", HttpStatus.INTERNAL_SERVER_ERROR);
                // is impossible to have opt empty because i have checked it previously
            }
        } else {
            throw new TokenNotRefreshedException("Security Triplet not found", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void deleteAllTriplets(String cryptedTriplet) {
        String identifier = this.getTokenSeriesFromToken(cryptedTriplet);
        String usr = this.getUserIdFromToken(cryptedTriplet);
        String token = this.getLoginTokenFromToken(cryptedTriplet);
        repository.deleteAll(usr, identifier, token);
    }

    private boolean checkIfLoginCookieContains(String cryptedTriplet) {
        String identifier = this.getTokenSeriesFromToken(cryptedTriplet);
        String usr = this.getUserIdFromToken(cryptedTriplet);
        String token = this.getLoginTokenFromToken(cryptedTriplet);
        // check if there is in db a security triplet which matches all the fields
        List<SecurityTriplet> list = repository.retrieveTriplets(usr, token).stream().filter(securityTriplet ->
                securityTriplet.getToken().equals(token) && securityTriplet.getUserId().equals(usr) && securityTriplet.getIdentifier().equals(identifier)).collect(Collectors.toList());
        return !list.isEmpty();
    }

    private Map<String, Object> initializeToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_CREATED, new Date());
        List<String> auth = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        claims.put(CLAIM_KEY_AUTHORITIES, auth);
        claims.put(CLAIM_KEY_IS_ENABLED, userDetails.isEnabled());
        return claims;
    }

    private String generateToken(Map<String, Object> claims, TokenType type) {
        Date exp = null;
        switch (type) {
            case ACCESS: {
                exp = generateExpirationDate(Constants.ACCESS_TOKEN_LIFESPAN);
                break;
            }
            case REFRESH: {
                exp = generateExpirationDate(Constants.REFRESH_TOKEN_LIFESPAN);
                break;
            }
            case LOGIN: {
                exp = generateExpirationDate(Constants.LOGIN_TOKEN_LIFESPAN);
                break;
            }
            default: {
                log.error("CASE NOT SUPPORTED");
                throw new StillUnimplementedException("Case not Supported");
            }
        }
        return Jwts.builder().setClaims(claims).setExpiration(exp).signWith(Constants.cryptAlgorithm, secret).compact(); // using SHA-256 algorithm according to AGID rules
    }

    private Date generateExpirationDate(int expiration) {
        return new Date(System.currentTimeMillis() + (1000L * expiration));
    }

    private Boolean isTokenExpired(String token) {
        final var expire = getExpirationDateFromToken(token);
        return expire.before(new Date());
    }

    private Boolean ignoreTokenExpiration(String token) {
        String audience = getAudienceFromToken(token);
        return (AUDIENCE_MOBILE.equals(audience) || AUDIENCE_TABLET.equals(audience));
    }

    private Boolean findAndCheck(String accessToken, String refreshToken) {
        Optional<BaseToken> tokenFromDb = tokenRepository.findByContentAndType(refreshToken, TokenType.REFRESH);
        if (tokenFromDb.isPresent()) {
            BaseToken t = tokenFromDb.get();
            return t.getTokenAssociated().equals(accessToken);
        } else {
            throw new TokenNotRefreshedException("Token not refreshed", HttpStatus.BAD_REQUEST);
        }
    }
}
