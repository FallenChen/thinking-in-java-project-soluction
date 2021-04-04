package org.garry.transaction;

/**
 * Exception thrown when the existence or non-existence of a transaction
 * amounts to an illegal state according to the transaction propagation
 * behavior that applies.
 */
public class IllegalTransactionStateException extends TransactionUsageException{

    public IllegalTransactionStateException(String msg) {
        super(msg);
    }

    public IllegalTransactionStateException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
