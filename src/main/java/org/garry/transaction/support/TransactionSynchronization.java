package org.garry.transaction.support;

import java.io.Flushable;

/**
 * Interface for transaction synchronization callbacks
 * Supported by AbstractPlatformTransactionManager
 *
 * TransactionSynchronization implementations can implement the Ordered interface
 * to influence their execution order.A synchronization that does not implement the
 * Ordered interface is appended to the end of the synchronization chain.
 *
 *System synchronizations performed by Spring itself use specific order values,
 * allowing for fine-grained interaction with their execution order (if necessary)
 *
 */
public interface TransactionSynchronization extends Flushable {

    int STATUS_COMMITTED = 0;

    int STATUS_ROLLED_BACK = 1;

    int STATUS_UNKNOWN = 2;

    /**
     * Suspend this synchronization
     * Supposed to unbind resources from TransactionSynchronizationManager if managing any
     */
    default void suspend()
    {

    }

    /**
     * Resume this synchronization
     * Supposed to rebind resources to TransactionSynchronizationManager if managing any
     */
    default void resume()
    {

    }

    /**
     * Flush the underlying session to the datastore, if applicable:
     * for example, a Hibernate/JPA session
     */
    default void flush()
    {

    }

    /**
     * Invoked before transaction commit (before "beforeCompletion")
     * Can e.g. flush transactional O/R Mapping sessions to the database.
     * This callback does not mean that the transaction will actually be committed.
     * A rollback decision can still occur after this method has been called. This callback
     * is rather meant to perform work that's only relevant if a commit still has a chance
     * to happen, such as flushing SQL statements to the database.
     * Note that exceptions will get propagated to the commit caller and cause a rollback of the transaction
     * @param readOnly
     */
    default void beforeCommit(boolean readOnly)
    {

    }

    /**
     * Invoked before transaction commit/rollback
     * Can perform resource cleanup before transaction completion
     * This method will be invoked after {@code beforeCommit},even when
     * {@code beforeCommit} threw an exception. This callback allows for
     * closing resources before transaction completion, for any outcome.
     */
    default void beforeCompletion()
    {

    }

    /**
     * Invoked after transaction commit. Can perform further operations right
     * after the main transaction has successfully committed.
     * Can e.g. commit further operations that are supposed to follow on a successful
     * commit of the main transaction,like confirmation messages or emails.
     * The transaction will have been committed already, but the
     * transactional resources might still be active and accessible.As a consequence,
     * any data access code triggered at this point will still "participate" in the
     * original transaction, allowing to perform some cleanup (with no commit following
     * anymore!),unless it explicitly declares that it needs to run in a separate
     * transaction
     */
    default void afterCommit()
    {

    }

    /**
     * Invoked after transaction commit/rollback
     * Can perform resource cleanup after transaction completion.
     * NOTE:The transaction will have been committed or rolled back already,
     * but the transactional resources might still be active and accessible.As a
     * consequence, any data access code triggered at this point will still "participate"
     * in the original transaction,allowing to perform some cleanup (with no commit
     * following anymore!),unless it explicitly declares that it needs to run in a
     * separate transaction.Hence:Use {@code PROPAGATION_REQUIRES_NEW}
     * for any transactional operation that is called from here.
     * @param status completion status according to the {@code STATUS_*} constants
     */
    default void afterCompletion(int status)
    {

    }
}
