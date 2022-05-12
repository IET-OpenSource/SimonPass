package it.iet.config.exceptions.impl;

import it.iet.config.exceptions.BaseStatusException;
import org.springframework.http.HttpStatus;

public class TokenExpiredException extends BaseStatusException {
    public TokenExpiredException(String message, HttpStatus code) {
        super(message, code);
    }
}
