package it.iet.config.exceptions.impl;


import it.iet.config.exceptions.BaseStatusException;
import org.springframework.http.HttpStatus;

public class TokenIntegrityException extends BaseStatusException {

    public TokenIntegrityException(String errorMessage) {
        super(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public TokenIntegrityException(String errorMessage, HttpStatus code) {
        super(errorMessage, code);
    }
}
