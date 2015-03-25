package org.soundforme.collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author NGorelov
 */
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
