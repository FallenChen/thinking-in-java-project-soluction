package org.garry.transaction.support;

import org.garry.transaction.TransactionException;
import org.springframework.lang.Nullable;

/**
 * Interface specifying basic transaction execution operations
 *
 */
public interface TransactionOperations {

    /**
     * Execute the action specified by the given callback object within a transaction.
     * Allows for returning a result object created within the transaction, that is,
     * a domain object or a collection of domain objects.
     * @param action
     * @param <T>
     * @return
     * @throws TransactionException
     */
    @Nullable
    <T> T execute(TransactionCallback<T> action) throws TransactionException;
}
