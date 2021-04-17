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
     *
     * @param status
     * @return
     */
    @Nullable
    T doInTransaction(TransactionStatus status);
}
