package it.iet.util;

import it.iet.config.exceptions.BaseStatusException;
import it.iet.config.exceptions.impl.AlreadyExistingException;
import it.iet.config.exceptions.impl.BadCredentialsException;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class implementing both exception handling for the whole project,
 * and the creation of a ResponseEntity to be sent back according to the
 * kinds of exceptions thrown.
 */

@Data
@NoArgsConstructor
public class ResponseWrapper implements Serializable {

    private static ConcurrentHashMap<Class<?>, HttpStatus> existingExceptionToCode = new ConcurrentHashMap<>() {
        {
            put(NullPointerException.class, HttpStatus.NOT_FOUND);
        }
    };

    private String exception = null;
    private String info = null;
    private Object data = null;
    private Integer errorID = 0; // used to distinguish known exceptions which need to be managed from FE

    /**
     * @param info Controller method description
     * @param ret  Lambda function wrapping a call to the Service layer
     * @return A REST HTTP Response, whose body is a ResponseWrapper
     */
    public static ResponseEntity<ResponseWrapper> format(String info, Callable<?> ret) {

        var obj = new ResponseWrapper();
        obj.setInfo(info);

        try {

            obj.setData(ret.call());
            return ResponseEntity
                    .ok()
                    .body(obj);
        } catch (BadCredentialsException badCredentialsException) {
            // If it is bad credentials we know it from errorID set to 1
            obj.setException(badCredentialsException.getMessage());
            obj.setErrorID(1);
            return ResponseEntity
                    .status(badCredentialsException.getStatusCode())
                    .body(obj);

        } catch (AlreadyExistingException alreadyExistingException) {
            // If it is already existing we know from errorID set to 2
            obj.setException(alreadyExistingException.getMessage());
            obj.setErrorID(2);
            return ResponseEntity
                    .status(alreadyExistingException.getStatusCode())
                    .body(obj);
        } catch (BaseStatusException e) {

            obj.setException(e.getMessage());
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(obj);

        }
        catch (Throwable e) {

            obj.setException(e.getMessage());
            return ResponseEntity
                    .status(
                            existingExceptionToCode.getOrDefault(
                                    e.getClass(),
                                    HttpStatus.INTERNAL_SERVER_ERROR
                            )
                    )
                    .body(obj);
        }
    }

    public static void addExceptionHandling(Class<?> exceptionClass, HttpStatus status) {
        existingExceptionToCode.put(exceptionClass, status);
    }
}
