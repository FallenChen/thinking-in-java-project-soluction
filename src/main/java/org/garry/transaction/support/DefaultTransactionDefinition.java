package org.garry.transaction.support;

import org.garry.transaction.TransactionDefinition;

import java.io.Serializable;

/**
 * Default implementation of the {@link TransactionDefinition} interface,
 * offering bean-style configuration and sensible default values
 * (PROPAGATION_REQUIRED, ISOLATION_DEFAULT, TIMEOUT_DEFAULT, readOnly = false)
 */
public class DefaultTransactionDefinition implements TransactionDefinition, Serializable {

    @Override
    public int getIsolationLevel() {
        return 0;
    }

    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getPropagationBehavior() {
        return 0;
    }
}
