package org.garry.transaction.support;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Default implementation of the {@link org.garry.transaction.TransactionStatus}
 * interface, used by
 */
public class DefaultTransactionStatus extends AbstractTransactionStatus{

    @Nullable
    private final Object transaction;

    private final boolean newTransaction;

    private final boolean newSynchronization;

    private final boolean readOnly;

    private final boolean debug;

    @Nullable
    private final Object suspendedResources;

    public DefaultTransactionStatus(
            @Nullable Object transaction, boolean newTransaction, boolean newSynchronization,
            boolean readOnly, boolean debug, @Nullable Object suspendedResources) {
        this.transaction = transaction;
        this.newTransaction = newTransaction;
        this.newSynchronization = newSynchronization;
        this.readOnly = readOnly;
        this.debug = debug;
        this.suspendedResources = suspendedResources;
    }

    /**
     * Return the underlying transactino object
     * @return
     */
    @Nullable
    public Object getTransaction() {
        Assert.state(this.transaction != null, "No transaction active");
        return this.transaction;
    }

    /**
     * Return whether there is an actual transaction active
     * @return
     */
    public boolean hasTransaction()
    {
        return (this.transaction != null);
    }

    public boolean isNewSynchronization() {
        return this.newSynchronization;
    }

    @Override
    public boolean isNewTransaction() {
        return false;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    /**
     * Return whether the progress of this transaction is debugged.This is used
     * by AbstractPlatformTransactionManager as an optimization, to prevent repeated
     * calls to logger.isDebug().Not really intended for client code
     * @return
     */
    public boolean isDebug() {
        return this.debug;
    }
}
