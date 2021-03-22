package org.garry.transaction.support;

import org.garry.transaction.TransactionException;
import org.springframework.lang.Nullable;

/**
 * Interface specifying basic transaction execution operations
 *
 */
public interface TransactionOperations {

    @Nullable
    <T> T execute(TransactionCallback<T> action) throws TransactionException;
}
