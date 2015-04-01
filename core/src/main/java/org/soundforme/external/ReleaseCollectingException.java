package org.soundforme.external;

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
