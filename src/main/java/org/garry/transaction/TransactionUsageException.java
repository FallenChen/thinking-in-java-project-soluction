package org.garry.transaction;

/**
 * Superclass for exceptions caused by inappropriate usage of
 * a Spring transaction API
 */
public class TransactionUsageException extends TransactionException{

    public TransactionUsageException(String msg) {
        super(msg);
    }

    public TransactionUsageException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
