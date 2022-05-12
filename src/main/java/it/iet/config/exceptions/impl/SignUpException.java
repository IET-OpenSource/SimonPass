package it.iet.config.exceptions.impl;

import it.iet.config.exceptions.BaseStatusException;
import org.springframework.http.HttpStatus;

public class SignUpException extends BaseStatusException {

    public SignUpException(String errorMessage) {
        super(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public SignUpException(String errorMessage, HttpStatus code) {
        super(errorMessage, code);
    }
}
