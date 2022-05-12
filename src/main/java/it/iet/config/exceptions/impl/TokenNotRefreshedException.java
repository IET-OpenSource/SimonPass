package it.iet.config.exceptions.impl;

import it.iet.config.exceptions.BaseStatusException;
import org.springframework.http.HttpStatus;

public class TokenNotRefreshedException extends BaseStatusException {

    public TokenNotRefreshedException(String errorMessage) {
        super(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public TokenNotRefreshedException(String errorMessage, HttpStatus code) {
        super(errorMessage, code);
    }
}
