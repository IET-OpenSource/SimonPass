package it.iet.config.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * Simple exception, storing a status code for exception handling.
 * This implementation is intentionally vague about the status code nature,
 * to give the developer the possibility to use it in more complex ways than the class
 * was originally thought of (that is, handling HTTP status code for each error).
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseStatusException extends RuntimeException implements Serializable {

    /**
     * Status code for a certain exception.
     * Its meaning is user-defined.
     */
    private final HttpStatus code;

    /**
     * Default constructor, creating an exception with the specified message and code.
     * @param code The status code for a particular exception
     * @param message The message describing the exception
     */
    protected BaseStatusException(String message, HttpStatus code) {
        super(message);
        this.code = code;
    }

    /**
     * Returns the status code for this particular exception.
     * @return The status code
     */
    public HttpStatus getStatusCode() {
        return code;
    }
}

