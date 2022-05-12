package it.iet.config.exceptions.impl;

import it.iet.config.exceptions.BaseStatusException;
import org.springframework.http.HttpStatus;

public class TokenNotFoundException extends BaseStatusException {
    public TokenNotFoundException(String message, HttpStatus code) {
        super(message, code);
    }
}
