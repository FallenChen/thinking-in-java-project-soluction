package org.garry.transaction.support;

import org.garry.transaction.TransactionStatus;
import org.springframework.lang.Nullable;

/**
 * Callback interface for transactional code.
 *
 * Typically used to assemble various calls to transaction-unaware data access
 * services into a higher-level service method with transaction demarcation
 * @param <T>
 */
@FunctionalInterface
public interface TransactionCallback<T>{

    /**
     * Gets called by {@link TransactionTemplate#execute(TransactionCallback)} within a transactional context.
     * Does not need to care about transactions itself, although it can retrieve and
     * influence the status of the current transaction via the given status object,
     * @param status
     * @return
     */
    @Nullable
    T doInTransaction(TransactionStatus status);
}
