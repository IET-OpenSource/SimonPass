package it.iet.config.exceptions.impl;

import it.iet.config.exceptions.BaseStatusException;
import org.springframework.http.HttpStatus;

public class CookieNotFoundException extends BaseStatusException {

    public CookieNotFoundException(String errorMessage) {
        super(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public CookieNotFoundException(String errorMessage, HttpStatus code) {
        super(errorMessage, code);
    }
}
