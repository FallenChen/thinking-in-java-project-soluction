package org.garry.transaction;

/**
 * Thrown when an attempt to commit a transaction resulted
 * in an unexpected rollback
 */
public class UnexpectedRollbackException extends TransactionException{

    public UnexpectedRollbackException(String msg) {
        super(msg);
    }

    public UnexpectedRollbackException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
