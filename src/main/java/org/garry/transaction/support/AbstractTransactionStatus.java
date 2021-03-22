package org.garry.transaction.support;

import org.garry.transaction.SavepointManager;
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


    /**
     * Return a SavepointManager for the underlying transaction, if possible
     * @return
     */
    protected SavepointManager getSavepointManager()
    {
        return null;
    }
}
