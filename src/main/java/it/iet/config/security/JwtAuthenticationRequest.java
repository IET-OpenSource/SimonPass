package it.iet.config.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
@AllArgsConstructor
public class JwtAuthenticationRequest implements Serializable {

    private String email;
    private String password;
    private boolean rememberMe;

    public JwtAuthenticationRequest() {
        super();
    }

    public JwtAuthenticationRequest(String username, String password) {
        this.setEmail(username);
        this.setPassword(password);
        this.rememberMe = false;
    }

}
