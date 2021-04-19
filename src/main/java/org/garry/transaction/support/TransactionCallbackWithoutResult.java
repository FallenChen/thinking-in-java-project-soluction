package org.garry.transaction.support;

import org.garry.transaction.TransactionStatus;
import org.springframework.lang.Nullable;

/**
 * Simple convenience class for TransactionCallback implementation.
 * Allows for implementing a doInTransaction version without result,
 * i.e. without the need for a return statement
 */
public abstract class TransactionCallbackWithoutResult implements TransactionCallback<Object>{

    @Nullable
    @Override
    public Object doInTransaction(TransactionStatus status) {
        doInTransactionWithoutResult(status);
        return null;
    }

    /**
     * Gets called by {@code TransactionTemplate.execute} within a transactional
     * context. Does not need to care about transactions itself, although it can retrieve
     * and influence the status of the current transaction via the given status object,
     * e.g. setting rollback-only.
     * @param status
     */
    protected abstract void doInTransactionWithoutResult(TransactionStatus status);

}
