package it.iet.util;

import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Constants {

    private static String frontEndUrl;

    @Value("${frontend.url}")
    private void setFrontEndUrl(String frontEndUr){
        Constants.frontEndUrl = frontEndUr;
        Constants.RESET_PASSWORD_BODY = "This is your reset password link " + frontEndUrl + "/reset-password?token="; //this message MUST finish with ?token= otherwise it will not work
        Constants.MAIL_VERIFICATION_BODY = "This is your mail verification link " + frontEndUrl + "/verify-email?token="; //this message MUST finish with ?token= otherwise it will not work
    }

    private Constants() {
    }

    /*------------------------         MESSAGES        ------------------------*/
    public static final String USER_ALREADY_EXISTS = "User with this email already exists";
    public static final String ACCESS_DENIED = "Access denied";
    public static final String USER_NOT_FOUND = "User Not Found";
    public static final String ROLE_NOT_FOUND = "Role Not Found";
    public static final String REFRESH_TOKEN_ERROR = "Error Refreshing token";
    public static final String PASSWORD_NOT_VALID = "Password does not match all the rules";
    public static final String VALIDATION_FAILED = "Validation process failed";
    public static final String PARSING_EXCEPTION = "Parsing exception";

    /*----------------------   TOKEN INFO AND DURATIONS  ----------------------*/
    public static final SignatureAlgorithm cryptAlgorithm = SignatureAlgorithm.HS256;
    public static final String ACCESS_TOKEN_COOKIE_NAME = "ACT";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "RFT";
    public static final String LOGIN_COOKIE_NAME = "LGC";
    public static final String ACCESS_KEY = "access_token";
    public static final String REFRESH_KEY = "refresh_token";
    public static final String LOGIN_KEY = "login_token";
    public static final String USER_KEY = "usr";
    public static final String SERIES_KEY = "series";
    public static final String TOKEN_TYPE_KEY = "token_type";

    public static final int ACCESS_TOKEN_LIFESPAN = 900; //30 seconds
    public static final int LOGIN_TOKEN_LIFESPAN = 900;
    public static final int REFRESH_COOKIE_LIFESPAN = 604800; //one week
    public static final int REFRESH_TOKEN_LIFESPAN = 604800; //one week
    public static final int ONE_MONTH = 2592000; //one month

    /*----------------------- ALLOWED ORIGINS ------------------------*/
    public static final String[] origins = {"http://localhost:4200", "http://localhost:3000", "*"};
    public static final String GATEWAY_URI = "/gateway";

    public static final String RESET_PASSWORD_SUBJECT = "Reset Password";
    public static String RESET_PASSWORD_BODY;
    public static final long RESET_PASSWORD_TOKEN_EXPIRATION = 1800;

    public static final String MAIL_VERIFICATION_SUBJECT = "Mail verification";
    public static String MAIL_VERIFICATION_BODY;

    public static final long MAIL_VERIFICATION_TOKEN_EXPIRATION = 1800;

    /*-------------- OPENAPI JSON LIST --------------*/
    public static final String[] openaApiJsonNameList = {"openapi.json"};
    public static final String andRolesKey = "andRoles";
    public static final String adminRole = "ROLE_ADMIN";
    public static final String userRole = "ROLE_USER";
}
