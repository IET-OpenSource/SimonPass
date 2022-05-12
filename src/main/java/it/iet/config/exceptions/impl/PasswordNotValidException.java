package it.iet.config.exceptions.impl;

import it.iet.config.exceptions.BaseStatusException;
import org.springframework.http.HttpStatus;

public class PasswordNotValidException extends BaseStatusException {

    public PasswordNotValidException(String errorMessage) {
        super(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public PasswordNotValidException(String errorMessage, HttpStatus code) {
        super(errorMessage, code);
    }
}
