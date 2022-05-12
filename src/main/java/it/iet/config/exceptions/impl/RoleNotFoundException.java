package it.iet.config.exceptions.impl;

import it.iet.config.exceptions.BaseStatusException;
import org.springframework.http.HttpStatus;

public class RoleNotFoundException extends BaseStatusException {

    public RoleNotFoundException(String errorMessage) {
        super(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public RoleNotFoundException(String errorMessage, HttpStatus code) {
        super(errorMessage, code);
    }
}
