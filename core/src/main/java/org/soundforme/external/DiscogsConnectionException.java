package org.soundforme.external;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author NGorelov
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Error on connection to discogs")
public class DiscogsConnectionException extends RuntimeException {
    public DiscogsConnectionException() {
        super();
    }

    public DiscogsConnectionException(String message) {
        super(message);
    }

    public DiscogsConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
