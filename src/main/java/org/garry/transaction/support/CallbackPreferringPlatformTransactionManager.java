package org.garry.transaction.support;

import org.garry.transaction.PlatformTransactionManager;
import org.garry.transaction.TransactionDefinition;
import org.springframework.lang.Nullable;

/**
 * Extension of the {@link org.garry.transaction.PlatformTransactionManager}
 * interface, exposing a method for executing a given callback within a transaction.
 *
 * Implementors of this interface automatically express a preference for
 * callbacks over programmatic {@code getTransaction}, {@code commit},
 * and {@code rollback} calls.Calling code may check whether a given
 * transaction manager implements this interface to choose to prepare a
 * callback instead of explicit transaction demarcation control
 */
public interface CallbackPreferringPlatformTransactionManager extends PlatformTransactionManager {

    /**
     * Execute the action specified by the given callback object within a transaction.
     * Allows for returning a result object created within the transaction, that is,
     * a domain object or a collection of domain objects.
     * @param definition
     * @param callback
     * @param <T>
     * @return
     */
    @Nullable
    <T> T execute(@Nullable TransactionDefinition definition, TransactionCallback<T> callback);
}
