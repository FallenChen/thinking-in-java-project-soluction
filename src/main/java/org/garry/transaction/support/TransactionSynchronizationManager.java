package org.garry.transaction.support;

/**
 * Central delegate that manages resources and transaction synchronizations per thread.
 * To be used by resource management code but not by typical application code.
 *
 * Supports one resource per key without overwriting, that is , a resource needs
 * to be removed before a new one can be set for the same key.
 * Supports a list of transaction synchronizations if synchronization is active.
 *
 * Resource management code should check for thread-bound resources,e.g. JDBC
 * Connections or Hibernate Sessions, via {@code getResource}.Such code is
 * normally not supposed to bind resources to threads, as this is the responsibility
 * of transaction managers.A further option is to lazily bind on first use if
 * transaction synchronization is active, for performing transactions that span
 * an arbitrary number of resources.
 *
 * Transaction synchronization must be activated and deactivated by a transaction
 * manager via {@link #initSynchronization()} and {@link #clearSynchronization()}.
 * This is automatically supported by {@link AbstractTransactionManager},
 * and thus by all standard Spring transaction managers, such as
 * {@link org.springframework.transaction.jta.JtaTransactionManager} and
 * {@link org.springframework.jdbc.datasource.DataSourceTransactionManager}.
 *
 * Resource management code should only register synchronizations when this
 * manager is active, which can be checked via {@link #isSynchronizationActice};
 * it should perform immediate resource cleanup else.If transaction synchronization
 * isn't active, there is either  no current transaction, or the transaction manager
 * doesn't support transaction synchronization
 *
 * Synchronization is for example used to always return the same resources
 * within a JTA transaction, e.g. a JDBC Connection or a Hibernate Session for
 * any given DataSource or SessionFactory, respectively
 */
public abstract class TransactionSynchronizationManager {
}
