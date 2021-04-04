package org.garry.transaction;

/**
 * Exception that gets thrown when an invalid timeout is specified,
 * that is, the specified timeout valid is out of range or the
 * transaction manager implementation doesn't support timeouts
 */
public class InvalidTimeoutException extends TransactionUsageException{

    private int timeout;

    public InvalidTimeoutException(String msg, int timeout)
    {
        super(msg);
        this.timeout = timeout;
    }

    public int getTimeout()
    {
        return timeout;
    }
}
