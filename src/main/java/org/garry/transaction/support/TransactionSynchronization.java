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


}
