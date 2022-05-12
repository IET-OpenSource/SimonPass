package it.iet.config.exceptions.impl;

import it.iet.config.exceptions.BaseStatusException;
import org.springframework.http.HttpStatus;

public class AlreadyExistingException extends BaseStatusException {

    public AlreadyExistingException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public AlreadyExistingException(String message, HttpStatus code) {
        super(message, code);
    }
}
