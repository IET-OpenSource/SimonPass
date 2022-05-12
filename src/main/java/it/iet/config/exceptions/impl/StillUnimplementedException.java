package it.iet.config.exceptions.impl;

import it.iet.config.exceptions.BaseStatusException;
import org.springframework.http.HttpStatus;

public class StillUnimplementedException extends BaseStatusException {
    public StillUnimplementedException(String errorMessage) {
        super(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public StillUnimplementedException(String errorMessage, HttpStatus code) {
        super(errorMessage, code);
    }

}

