package org.garry.transaction.interceptor;

import org.garry.transaction.TransactionDefinition;
import org.garry.transaction.support.DelegatingTransactionDefinition;
import org.springframework.lang.Nullable;

import java.io.Serializable;

/**
 * {@link TransactionAttribute} implementation that delegates all calls to a given target
 * {@link TransactionAttribute} instance. Abstract because it is meant to be subclassed,
 * with subclasses overriding specific methods that are not supposed to simply delegate
 * to the target instance
 */
public abstract class DelegatingTransactionAttribute extends DelegatingTransactionDefinition
        implements TransactionAttribute, Serializable {

    private final TransactionAttribute targetAttribute;

    public DelegatingTransactionAttribute(TransactionAttribute targetAttribute) {
        super(targetAttribute);
        this.targetAttribute = targetAttribute;
    }

    @Nullable
    @Override
    public String getQualifier() {
        return this.targetAttribute.getQualifier();
    }

    @Override
    public boolean rollbackOn(Throwable ex) {
       return this.targetAttribute.rollbackOn(ex);
    }
}
