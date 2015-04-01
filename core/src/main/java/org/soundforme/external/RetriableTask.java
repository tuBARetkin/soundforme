package org.soundforme.external;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class RetriableTask<T> implements Callable<T> {
    private final Logger logger = LoggerFactory.getLogger(RetriableTask.class);

    private final Callable<T> task;
    public static final long DEFAULT_NUMBER_OF_RETRIES = 10;
    public static final int DEFAULT_WAIT_TIME = 5000;

    private long numberOfRetries; // total number of tries
    private long numberOfTriesLeft; // number left
    private int timeToWait; // wait interval

    public RetriableTask(Callable<T> task) {
        this(DEFAULT_NUMBER_OF_RETRIES, null, task);
    }

    public RetriableTask(long numberOfRetries, Integer timeToWait, Callable<T> task) {
        this.numberOfRetries = numberOfRetries;
        numberOfTriesLeft = numberOfRetries + 1;
        this.timeToWait = ObjectUtils.defaultIfNull(timeToWait, DEFAULT_WAIT_TIME);
        this.task = task;
    }

    @Override
    public T call() throws Exception {
        while(true) {
            try {
                return task.call();
            } catch (DiscogsConnectionException e) {
                numberOfTriesLeft--;
                if (numberOfRetries != -1) {
                    if (numberOfTriesLeft == 0) {
                        throw e;
                    }
                    logger.info("Retrying {}/{} in {}", numberOfRetries - numberOfTriesLeft + 1, numberOfRetries, timeToWait);
                }
                Thread.sleep(timeToWait);
            }
        }
    }
}