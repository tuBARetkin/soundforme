package org.soundforme.external;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

public class RetriableTask<T> implements Callable<T> {
    private final Logger logger = LoggerFactory.getLogger(RetriableTask.class);

    private Callable<T> task;
    public static final long DEFAULT_NUMBER_OF_RETRIES = 10;
    public static final int DEFAULT_WAIT_TIME = 5000;

    public static final long INFINITE = -1;

    private long numberOfRetries; // total number of tries
    private long numberOfTriesLeft; // number left
    private int timeToWait; // wait interval


    public RetriableTask(Callable<T> task) {
        this(DEFAULT_NUMBER_OF_RETRIES, null, task);
    }


    public RetriableTask(long numberOfRetries, Callable<T> task) {
        this(numberOfRetries, null, task);
    }


    public RetriableTask(long numberOfRetries, Integer timeToWait, Callable<T> task) {
        this.numberOfRetries = numberOfRetries;
        numberOfTriesLeft = numberOfRetries + 1;
        this.timeToWait = (int) ObjectUtils.defaultIfNull(timeToWait, DEFAULT_WAIT_TIME);
        this.task = task;
    }

    @Override
    public T call() throws Exception {
        while(true) {
            try {
                return task.call();
            }
            catch (InterruptedException | CancellationException e) {
                throw e;
            } catch (Exception e) {
                numberOfTriesLeft--;
                if (numberOfRetries != -1) {
                    if (numberOfTriesLeft == 0) {
                        throw new RetryException(numberOfRetries + " attempts to retry failed at " + timeToWait + "ms interval", e, numberOfRetries, timeToWait);
                    }
                    logger.info("Retrying {}/{} in {}", numberOfRetries - numberOfTriesLeft + 1, numberOfRetries, timeToWait);
                }
                else {
                    logger.info("Retrying {} in {}", Math.abs(numberOfTriesLeft + 1), timeToWait);
                }
                Thread.sleep(timeToWait);
            }
        }
    }
}