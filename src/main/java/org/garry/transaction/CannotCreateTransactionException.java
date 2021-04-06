package org.garry.transaction;

/**
 * Exception thrown when a transaction can't be created using an
 * underlying transaction API such as JTA
 */
public class CannotCreateTransactionException extends TransactionException{

    public CannotCreateTransactionException(String msg) {
        super(msg);
    }

    public CannotCreateTransactionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
