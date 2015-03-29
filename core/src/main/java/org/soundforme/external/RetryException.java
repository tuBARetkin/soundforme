package org.soundforme.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author NGorelov
 */
public class RetryException extends Exception {

    private long numberOfRetries;
    private int timeBetween;


    public RetryException(String message, Throwable cause, long numberOfRetries, int timeBetween) {
        super(message, cause);
        this.numberOfRetries = numberOfRetries;
        this.timeBetween = timeBetween;
    }


    public long getNumberOfRetries() {
        return numberOfRetries;
    }


    public int getTimeBetween() {
        return timeBetween;
    }

}
