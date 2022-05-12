package it.iet.config.exceptions.impl;

import it.iet.config.exceptions.BaseStatusException;
import org.springframework.http.HttpStatus;

public class AuthenticationFailedException extends BaseStatusException {

    public AuthenticationFailedException(String errorMessage) {
        super(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public AuthenticationFailedException(String errorMessage, HttpStatus code) {
        super(errorMessage, code);
    }
}
