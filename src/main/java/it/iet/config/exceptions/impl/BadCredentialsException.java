package it.iet.config.exceptions.impl;

import it.iet.config.exceptions.BaseStatusException;
import org.springframework.http.HttpStatus;

public class BadCredentialsException extends BaseStatusException {

    public BadCredentialsException(String errorMessage) {
        super(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public BadCredentialsException(String errorMessage, HttpStatus code) {
        super(errorMessage, code);
    }
}

