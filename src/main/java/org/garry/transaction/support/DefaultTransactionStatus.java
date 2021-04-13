package org.garry.transaction.support;

import org.garry.transaction.NestedTransactionNotSupportedException;
import org.garry.transaction.SavepointManager;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Default implementation of the {@link org.garry.transaction.TransactionStatus}
 * interface, used by {@link AbstractPlatformTransactionManager}.Based on the conception
 * of an underlying "transaction object"
 *
 * Holds all status information that {@link AbstractPlatformTransactionManager}
 * needs internally, including a generic transaction object determined by the
 * concrete transaction manager implementation
 *
 * Supports delegating savepoint-related methods to a transaction object
 * that implements the {@link org.garry.transaction.SavepointManager} interface
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

    /**
     * Create a new DefaultTransactionStatus instance
     * @param transaction
     * @param newTransaction
     * @param newSynchronization
     * @param readOnly
     * @param debug
     * @param suspendedResources
     */
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
        return (hasTransaction() && this.newTransaction);
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

    /**
     * Return the holder for resources that have been suspended for this transaction,
     * if any
     * @return
     */
    @Nullable
    public Object getSuspendedResources() {
        return this.suspendedResources;
    }

    //-------------------------------------------------------------------
    // Enable functionality through underlying transaction object
    //-------------------------------------------------------------------


    /**
     * Determine the rollback-only flag via checking both the transaction object,
     * provided that the latter implements the {@link SmartTransactionObject} interface.
     *
     * @return
     */
    @Override
    public boolean isGlobalRollbackOnly() {
        return ((this.transaction instanceof SmartTransactionObject) &&
                ((SmartTransactionObject) this.transaction).isRollbackOnly());
    }

    /**
     * Delegate the flushing to the transaction object,
     * provided that the latter implements the {@link SmartTransactionObject} interface
     */
    @Override
    public void flush() {
       if(this.transaction instanceof SmartTransactionObject)
       {
           ((SmartTransactionObject)this.transaction).flush();
       }
    }

    /**
     * This implementation exposes the SavepointManager interface
     * of the underlying transaction object, if any
     */
    @Override
    protected SavepointManager getSavepointManager() {
       Object transaction = this.transaction;
       if(!(transaction instanceof SavepointManager))
       {
           throw new NestedTransactionNotSupportedException(
                   "Transaction object [" + this.transaction + "] does not support savepoints"
           );
       }
       return (SavepointManager) transaction;
    }

    /**
     * Return whether the underlying transaction implements the
     * SavepointManager interface
     * @return
     */
    public boolean isTransactionSavepointManager()
    {
        return (this.transaction instanceof SavepointManager);
    }
}
