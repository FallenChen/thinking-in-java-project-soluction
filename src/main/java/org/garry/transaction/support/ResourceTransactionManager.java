package org.garry.transaction.support;

import org.garry.transaction.PlatformTransactionManager;

/**
 * Extension of the {@link PlatformTransactionManager}
 * interface, indicating a native resource transaction manager, operating on a single
 * target resource.Such transaction managers differ from JTA transaction managers in
 * that they do not use XA transaction enlistment for an open number of resources but
 * rather focus on leveraging the native power and simplicity of a single target resource.
 *
 * This interface is mainly used for abstract introspection of a transaction manager,
 * giving clients a hint on what kind of transaction manager they have been given
 * and on what concrete resource the transaction manager is operating on
 */
public interface ResourceTransactionManager extends PlatformTransactionManager {

    /**
     * Return the resource factory that this transaction manager operates on,
     * e.g. a JDBC DataSource or a JMS ConnectionFactory
     * This target resource factory is usually used as resource key for
     * {@link TransactionSynchronizationManager}'s resource bindings per thread.
     * @return the target resource factory(never {@code null})
     */
    Object getResourceFactory();
}
