package org.garry.transaction;

import java.io.Flushable;
import java.io.IOException;

/**
 * Representation of the status of a transaction
 *
 *
 */
public interface TransactionStatus extends SavepointManager, Flushable {

    boolean isNewTransaction();

    boolean hasSavepoint();

    void setRollbackOnly();

    boolean isRollbackOnly();

    @Override
    void flush() throws IOException;

    boolean isCompleted();
}
