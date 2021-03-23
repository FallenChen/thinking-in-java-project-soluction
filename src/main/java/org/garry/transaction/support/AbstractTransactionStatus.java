package org.garry.transaction.support;

import org.garry.transaction.SavepointManager;
import org.garry.transaction.TransactionException;
import org.garry.transaction.TransactionStatus;
import org.springframework.lang.Nullable;

/**
 * Pre-implements the handling of local rollback-only and completed flags,
 * and delegation to an underlying {@link org.garry.transaction.SavepointManager}
 * Also offers the option of a holding a savepoint within the transaction
 */
public abstract class AbstractTransactionStatus implements TransactionStatus {

    private boolean rollbackOnly = false;

    private boolean completed = false;

    @Nullable
    private Object savepoint;

    public void setRollbackOnly(boolean rollbackOnly) {
        this.rollbackOnly = true;
    }

    /**
     * Determine the rollback-only flag via checking both the local rollback-only flag
     * of this TransactionStatus and global rollback-only flag of the underlying transaction, if any
     * @return
     */
    public boolean isRollbackOnly()
    {
        return (isLocalRollbackOnly() || isGlobalRollbackOnly());
    }

    /**
     * Determine the rollback-only flag via checking this TransactionStatus
     * Will only return "true" if the application called {@code setRollbackOnly}
     * on this TransactionStatus object
     * @return
     */
    public boolean isLocalRollbackOnly()
    {
        return this.rollbackOnly;
    }

    /**
     * Template method for determining the global rollback-only flag of the
     * underlying transaction, if any
     * @return
     */
    public boolean isGlobalRollbackOnly()
    {
        return false;
    }

    /**
     * This implementations is empty, considering flush as a no-op
     */
    public void flush()
    {

    }

    /**
     * Mark this transaction as completed, that is , committed or rolled back
     */
    public void setCompleted()
    {
        this.completed = true;
    }

    /**
     * Set a savepoint for this transaction. Useful for PROPAGATION_NESTED
     * @param savepoint
     */
    protected void setSavepoint(@Nullable Object savepoint)
    {
        this.savepoint = savepoint;
    }

    /**
     * Get the savepoint for this transaction, if any
     * @return
     */
    @Nullable
    protected Object getSavepoint()
    {
        return this.savepoint;
    }

    @Override
    public boolean hasSavepoint() {
       return (this.savepoint != null);
    }

    /**
     * Create a savepoint and hold it for the transaction
     *
     * if the underlying transaction does not support savepoints
     */
    public void createAndHoldSavepoint()
    {
       setSavepoint(getSavepointManager().createSavepoint());
    }

    /**
     * Roll back to the savepoint that is held for the transaction
     * and release the savepoint right afterwards
     */
    public void rollbackToHeldSavepoint()
    {
        Object savepoint = getSavepoint();
        if(savepoint == null)
        {
            // throw
        }
        getSavepointManager().rollbackToSavepoint(savepoint);
        getSavepointManager().releaseSavepoint(savepoint);
        setSavepoint(null);
    }

    /**
     * Release the savepoint that is held for the transaction
     */
    public void releaseHeldSavepoint()
    {
        Object savepoint = getSavepoint();
        if(savepoint == null)
        {
            // throw
        }
        getSavepointManager().releaseSavepoint(savepoint);
        setSavepoint(null);
    }

    //
    // Implementation of SavepointManager
    //

    /**
     * This implementation delegates to a SavepointManager for the
     * underlying transaction, if possible
     * @return
     * @throws TransactionException
     */
    @Override
    public Object createSavepoint() throws TransactionException {
       return getSavepointManager().createSavepoint();
    }

    /**
     * This implementation delegates to a SavepointManager for the
     * underlying transaction, if possible
     * @param savepoint
     * @throws TransactionException
     */
    @Override
    public void rollbackToSavepoint(Object savepoint) throws TransactionException {
        getSavepointManager().rollbackToSavepoint(savepoint);
    }

    /**
     * This implementation delegates to a SavepointManager for the
     * underlying transaction, if possible
     * @param savepoint
     * @throws TransactionException
     */
    @Override
    public void releaseSavepoint(Object savepoint) throws TransactionException {
        getSavepointManager().releaseSavepoint(savepoint);
    }

    /**
     * Return a SavepointManager for the underlying transaction, if possible
     * @return
     */
    protected SavepointManager getSavepointManager()
    {
        return null;
    }
}
