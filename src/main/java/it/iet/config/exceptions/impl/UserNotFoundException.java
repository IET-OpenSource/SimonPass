package it.iet.config.exceptions.impl;

import it.iet.config.exceptions.BaseStatusException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseStatusException {
    public UserNotFoundException(String message, HttpStatus code) {
        super(message, code);
    }
}
