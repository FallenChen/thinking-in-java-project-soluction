package org.garry.transaction;

/**
 * Exception thrown when attempting to work with a nested transaction
 * but nested transactions are not supported by the underlying backend.
 */
public class NestedTransactionNotSupportedException extends CannotCreateTransactionException{

    public NestedTransactionNotSupportedException(String msg) {
        super(msg);
    }

    public NestedTransactionNotSupportedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
