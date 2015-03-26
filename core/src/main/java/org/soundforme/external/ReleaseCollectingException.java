package org.soundforme.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author NGorelov
 */
public class ReleaseCollectingException extends RuntimeException {
    public ReleaseCollectingException() {
        super();
    }

    public ReleaseCollectingException(String message) {
        super(message);
    }

    public ReleaseCollectingException(String message, Throwable cause) {
        super(message, cause);
    }
}
