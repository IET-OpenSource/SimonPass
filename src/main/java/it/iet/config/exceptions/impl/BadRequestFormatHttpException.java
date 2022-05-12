package it.iet.config.exceptions.impl;

import it.iet.config.exceptions.BaseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when the HTTP request doesn't satisfy certain format rules.
 */
public class BadRequestFormatHttpException extends BaseStatusException {

    /**
     * Constructor specifying a message.
     * @param message Exception message
     */
    public BadRequestFormatHttpException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Costructor sepcifying a message and a HTTP status code.
     * @param message Exception message
     * @param code HTTP status code
     */
    public BadRequestFormatHttpException(String message, HttpStatus code) {
        super(message, code);
    }
}
