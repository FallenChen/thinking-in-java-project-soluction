package org.garry.transaction.support;

import org.garry.transaction.TransactionDefinition;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * {@link TransactionDefinition} implementation that delegates all calls to a given target
 * {@link TransactionDefinition} instance. Abstract because it is meant to be subclassed,
 * with subclasses overriding specific methods that are not supposed to simply delegate
 * to the target instance
 */
public abstract class DelegatingTransactionDefinition implements TransactionDefinition, Serializable {

    private final TransactionDefinition targetDefinition;

    public DelegatingTransactionDefinition(TransactionDefinition targetDefinition) {
        Assert.notNull(targetDefinition,"Target definition must not be null");
        this.targetDefinition = targetDefinition;
    }

    @Override
    public int getPropagationBehavior() {
        return this.targetDefinition.getPropagationBehavior();
    }

    @Override
    public int getIsolationLevel() {
        return this.targetDefinition.getIsolationLevel();
    }

    @Override
    public int getTimeout() {
        return this.targetDefinition.getTimeout();
    }

    @Override
    public boolean isReadOnly() {
        return this.targetDefinition.isReadOnly();
    }

    @Override
    @Nullable
    public String getName() {
        return this.targetDefinition.getName();
    }

    @Override
    public boolean equals(Object obj) {
        return this.targetDefinition.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.targetDefinition.hashCode();
    }

    @Override
    public String toString() {
        return this.targetDefinition.toString();
    }
}
