package it.iet.config.security;

import it.iet.infrastructure.mongo.entity.token.TokenType;
import it.iet.interfaces.facade.dto.user.LoginServiceDTO;
import it.iet.util.Constants;
import it.iet.util.CookieManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authToken = request.getHeader(Constants.ACCESS_TOKEN_COOKIE_NAME);

        UserDetails userDetails = null;

        if (authToken != null) {
            userDetails = jwtTokenUtil.getUserDetails(authToken);
        }

        LoginServiceDTO result = jwtTokenUtil.validateToken(request, authToken, userDetails);

        if (result.getCryptedTriplet() != null) {
            userDetails = jwtTokenUtil.getUserDetails(result.getCryptedTriplet());
        }

        if (userDetails != null && SecurityContextHolder.getContext().getAuthentication() == null && (Boolean.TRUE.equals(result.getAccessToken() != null || result.getCryptedTriplet() != null))) {
            var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            if (result.getCryptedTriplet() != null) {
                String newLoginCookie = jwtTokenUtil.restoreCookie(request, userDetails);
                CookieManager.generateAndAssignCookie(response, Constants.LOGIN_COOKIE_NAME, newLoginCookie, TokenType.LOGIN);
            }
        }
        if (request.getRequestURI().contains("gateway") && userDetails == null){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Must refresh token");
            return;
        }

        filterChain.doFilter(request, response);
    }

}
































