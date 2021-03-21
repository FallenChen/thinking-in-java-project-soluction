package org.garry.transaction;

import org.springframework.core.NestedRuntimeException;

public abstract class TransactionException extends NestedRuntimeException {

    public TransactionException(String msg) {
        super(msg);
    }

    public TransactionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
