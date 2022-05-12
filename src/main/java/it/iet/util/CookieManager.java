package it.iet.util;

import it.iet.infrastructure.mongo.entity.token.TokenType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class CookieManager {

    @Value("${customcookie.ACTmaxAge}")
    private int ACTmaxAge;
    @Value("${customcookie.RFTmaxAge}")
    private int RFTmaxAge;
    @Value("${customcookie.LTmaxAge}")
    private int LTmaxAge;
    @Value("${customcookie.secure}")
    private boolean secure;
    @Value("${customcookie.httpOnly}")
    private boolean httpOnly;
    @Value("${customcookie.path}")
    private String path;


    public static void generateAndAssignCookie(HttpServletResponse response, String name, String value, TokenType type) {
        var cookie = CookieManager.generateStandardCookie(name, value, type);
        response.addCookie(cookie);
    }

    public static Cookie retrieveCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0)
            for (Cookie c : cookies) {
                if (c.getName().equals(cookieName)) {
                    return c;
                }
            }
        return null;
    }


    private static Cookie generateStandardCookie(String name, String value, TokenType type) {
        var cookie = new Cookie(name, value);
        switch (type) {
            case ACCESS:
            case REFRESH: {
                cookie.setMaxAge(Constants.REFRESH_COOKIE_LIFESPAN); // access and refresh COOKIE must have the same expiration date, tokens lifespan can be different
                break;
            }
            case LOGIN: {
                cookie.setMaxAge(Constants.LOGIN_TOKEN_LIFESPAN);
                break;
            }
            default: {
                log.error("Case Still Unimplemented");
            }
        }
        cookie.setPath("/"); // global cookie accessible everywhere
        return cookie;
    }

    private static Cookie genereteDeleteCookie(String name) {
        var cookie = new Cookie(name, null);
        cookie.setMaxAge(0); // expires immediately
        cookie.setSecure(false);
        cookie.setHttpOnly(false);
        cookie.setPath("/"); // global cookie accessible every where
        return cookie;
    }

}
