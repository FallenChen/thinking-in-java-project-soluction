package org.garry.transaction.support;

import java.io.Flushable;
import java.io.IOException;

/**
 * Interface to be implemented by transaction objects that are able to
 * return an internal rollback-only marker,typically from a another
 * transaction that has participated and marked it as rollback-only.
 */
public interface SmartTransactionObject extends Flushable {

    /**
     * Return whether the transaction is internally marked as rollback-only
     * Can, for example, check the JTA UserTransaction.
     * @return
     */
    boolean isRollbackOnly();

    /**
     * Flush the underlying sessions to the datastore, if applicable:
     * for example, all affected Hibernate/JPA sessions
     * @throws IOException
     */
    @Override
    void flush();
}
