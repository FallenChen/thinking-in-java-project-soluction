package org.garry.transaction;

/**
 * transaction suspension is not supported by the underlying backend
 */
public class TransactionSuspensionNotSupportedException extends CannotCreateTransactionException{

    public TransactionSuspensionNotSupportedException(String msg) {
        super(msg);
    }

    public TransactionSuspensionNotSupportedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
