package it.iet;

import it.iet.config.security.JwtAuthenticationRequest;
import it.iet.config.security.JwtTokenUtil;
import it.iet.infrastructure.mongo.entity.user.User;
import it.iet.interfaces.facade.UserService;
import it.iet.interfaces.facade.dto.user.LoginServiceDTO;
import it.iet.interfaces.facade.dto.user.SignUpDTO;
import it.iet.interfaces.web.AuthenticationPaths;
import it.iet.model.repository.BaseTokenRepository;
import it.iet.model.repository.SecurityTripletRepository;
import it.iet.model.repository.UserRepository;
import it.iet.util.Constants;
import it.iet.util.ResponseWrapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;

import java.net.URI;
import java.util.Objects;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthFrameworkApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BaseTokenRepository tokenRepository;

    @Autowired
    private SecurityTripletRepository securityTripletRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private static final String tokenForRegistration = "QqfPlV9VdOzb6MZtji";
    private static final String cookieHeader = "Set-Cookie";
    private static final String firstName = "vbB4Bszu1MMLNgW2SCx2";
    private static final String lastName = "vbB4Bszu1MMLNgW2SCx2";
    private static final String email = "vbB4Bszu1MMLNgW2SCx2@email.it";
    private static final String password = "vbB4Bszu1MMLNgW2SCx2";

    private static final String user_firstName = "nreornowininwepn";
    private static final String user_lastName = "nreornowininwepn";
    private static final String user_email = "nreornowininwepn@email.it";
    private static final String user_password = "nreornowininwepn";


    @Test
    @Order(1)
    void contextLoads() {
    }


    @Test
    @Order(2)
    void createUserAdmin() throws Exception {
        SignUpDTO userToBeRegistered = new SignUpDTO(firstName, lastName, email, password, "");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Sign-Token", tokenForRegistration);

        HttpEntity<SignUpDTO> request = new HttpEntity<>(userToBeRegistered, headers);
        ResponseEntity<ResponseWrapper> result = restTemplate.postForEntity(composePath(AuthenticationPaths.REGISTRATION), request, ResponseWrapper.class);

        Assertions.assertEquals(Boolean.TRUE, result.getBody().getData());

    }

    @Test
    @Order(2)
    @Disabled
    void createUser() throws Exception {
        SignUpDTO userToBeRegistered = new SignUpDTO(user_firstName, user_lastName, user_email, user_password, "");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Sign-Token", tokenForRegistration);

        HttpEntity<SignUpDTO> request = new HttpEntity<>(userToBeRegistered, headers);
        ResponseEntity<ResponseWrapper> result = restTemplate.postForEntity(composePath(AuthenticationPaths.REGISTRATION), request, ResponseWrapper.class);

        Assertions.assertEquals(Boolean.TRUE, result.getBody().getData());

    }

    @Test
    @Order(3)
    void loginUserThroughServiceWithoutRememberMe() {
        JwtAuthenticationRequest authenticationRequest = new JwtAuthenticationRequest(email, password, false);
        LoginServiceDTO response = userService.loginUser(authenticationRequest);
        String accessToken = response.getAccessToken();
        String refreshToken = response.getRefreshToken();
        Assertions.assertNotNull(accessToken);
        Assertions.assertNotNull(refreshToken);

        tokenRepository.deleteTokenByContent(accessToken);
        tokenRepository.deleteTokenByContent(refreshToken);

    }

    @Test
    @Order(4)
    void loginUserThroughServiceWithRememberMe() {
        JwtAuthenticationRequest authenticationRequest = new JwtAuthenticationRequest(email, password, true);
        LoginServiceDTO response = userService.loginUser(authenticationRequest);

        String accessToken = response.getAccessToken();
        String refreshToken = response.getRefreshToken();
        Assertions.assertNotNull(accessToken);
        Assertions.assertNotNull(refreshToken);

        tokenRepository.deleteTokenByContent(accessToken);
        tokenRepository.deleteTokenByContent(refreshToken);

        String cryptedTriplet = response.getCryptedTriplet();
        Assertions.assertNotNull(cryptedTriplet);
        securityTripletRepository.deleteByToken(jwtTokenUtil.getLoginTokenFromToken(cryptedTriplet));
    }

    @Test
    @Order(5)
    void loginThroughControllerWithoutRememberMe() throws Exception {
        JwtAuthenticationRequest authenticationRequest = new JwtAuthenticationRequest(email, password, false);

        var request = new HttpEntity(authenticationRequest);

        ResponseEntity<ResponseWrapper> result = restTemplate.postForEntity(composePath(AuthenticationPaths.LOGIN), request, ResponseWrapper.class);

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getBody());
        Assertions.assertNotNull(result.getHeaders());
        Assertions.assertNotNull(result.getHeaders().get(cookieHeader));

        String one = result.getHeaders().get(cookieHeader).get(0);
        Assertions.assertNotNull(one);

        String two = result.getHeaders().get(cookieHeader).get(1);
        Assertions.assertNotNull(two);

        Assertions.assertEquals(Boolean.TRUE, result.getBody().getData());
        Assertions.assertTrue(result.getHeaders().get(cookieHeader).get(0).contains(Constants.ACCESS_TOKEN_COOKIE_NAME));
        Assertions.assertTrue(result.getHeaders().get(cookieHeader).get(1).contains(Constants.REFRESH_TOKEN_COOKIE_NAME));

        tokenRepository.deleteTokenByContent(one.substring(4, one.indexOf(';'))); //because we take the value from header so it has also the cookie details
        tokenRepository.deleteTokenByContent(two.substring(4, two.indexOf(';')));

    }

    @Test
    @Order(6)
    void loginThroughControllerWithRememberMe() throws Exception {
        JwtAuthenticationRequest authenticationRequest = new JwtAuthenticationRequest(email, password, true);

        var request = new HttpEntity(authenticationRequest);

        ResponseEntity<ResponseWrapper> result = restTemplate.postForEntity(composePath(AuthenticationPaths.LOGIN), request, ResponseWrapper.class);

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getBody());
        Assertions.assertNotNull(result.getHeaders());
        Assertions.assertNotNull(result.getHeaders().get(cookieHeader));
        String one = result.getHeaders().get(cookieHeader).get(0);
        Assertions.assertNotNull(one);

        String two = result.getHeaders().get(cookieHeader).get(1);
        Assertions.assertNotNull(two);

        String three = result.getHeaders().get(cookieHeader).get(2);
        Assertions.assertNotNull(three);

        Assertions.assertEquals(Boolean.TRUE, result.getBody().getData());
        Assertions.assertTrue(result.getHeaders().get(cookieHeader).get(0).contains(Constants.ACCESS_TOKEN_COOKIE_NAME));
        Assertions.assertTrue(result.getHeaders().get(cookieHeader).get(1).contains(Constants.REFRESH_TOKEN_COOKIE_NAME));
        Assertions.assertTrue(result.getHeaders().get(cookieHeader).get(2).contains(Constants.LOGIN_COOKIE_NAME));

        tokenRepository.deleteTokenByContent(one.substring(4, one.indexOf(';'))); //because we take the value from header so it has also the cookie details
        tokenRepository.deleteTokenByContent(two.substring(4, two.indexOf(';')));
        securityTripletRepository.deleteByToken(jwtTokenUtil.getLoginTokenFromToken(three.substring(4, three.indexOf(';'))));

    }

    @Test
    @Order(7)
    void testCallWithoutAccessToken() throws Exception {
        ResponseEntity<ResponseWrapper> httpCall = restTemplate.getForEntity(composePath(AuthenticationPaths.GET_ALL_USERS), ResponseWrapper.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, httpCall.getStatusCode());
    }

    @Test
    @Order(8)
    void testCallWithAccessToken() throws Exception {
        JwtAuthenticationRequest authenticationRequest = new JwtAuthenticationRequest(email, password, false);
        var req = new HttpEntity(authenticationRequest);
        ResponseEntity<ResponseWrapper> intermediate = restTemplate.postForEntity(composePath(AuthenticationPaths.LOGIN), req, ResponseWrapper.class);

        Assertions.assertNotNull(intermediate.getHeaders().get(cookieHeader));
        String one = intermediate.getHeaders().get(cookieHeader).get(0);
        Assertions.assertNotNull(one);

        String two = intermediate.getHeaders().get(cookieHeader).get(1);
        Assertions.assertNotNull(two);

        HttpHeaders httpHeaders = new HttpHeaders();
        String accessToken = one.substring((Constants.ACCESS_TOKEN_COOKIE_NAME.length() + 1), one.indexOf(';')); //substring forced due to cookie management missing in JUnit
        httpHeaders.add(Constants.ACCESS_TOKEN_COOKIE_NAME, accessToken);
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        ResponseEntity<ResponseWrapper> result = restTemplate.exchange(composePath(AuthenticationPaths.GET_ALL_USERS), HttpMethod.GET, entity, ResponseWrapper.class);


        Assertions.assertNull(Objects.requireNonNull(result.getBody()).getException());
        Assertions.assertNotSame(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());

        tokenRepository.deleteTokenByContent(one.substring(4, one.indexOf(';'))); //because we take the value from header so it has also the cookie details
        tokenRepository.deleteTokenByContent(two.substring(4, two.indexOf(';')));
    }

    @Test
    @Order(10)
    void deleteUser() {
        User user = userRepository.findByEmail(email).orElse(null);
        Assertions.assertNotNull(user);
        String id = user.get_id();
        Assertions.assertNotNull(user);
        userRepository.deleteUser(user);
        User newUserFromDb = userRepository.findByEmail(email).orElse(null);
        Assertions.assertNull(newUserFromDb);
    }

    private URI composePath(String path) throws Exception {
        return new URI("http://localhost:" + port + path);
    }

}
