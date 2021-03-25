package org.garry.transaction.support;

import org.garry.transaction.TransactionTimedOutException;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;
import java.util.Date;

/**
 * Convenient base class for resource holders.
 *
 * Features rollback-only support for participating transactions.
 * Can expire after a certain number of seconds or milliseconds
 * in order to determine a transactional timeout
 */
public abstract class ResourceHolderSupport implements ResourceHolder {

    private boolean synchronizedWithTransaction = false;

    private boolean rollbackOnly = false;

    @Nullable
    private Date deadline;

    private int referenceCount = 0;

    private boolean isVoid = false;

    /**
     * Mark the resource as synchronized with a transaction
     * @param synchronizedWithTransaction
     */
    public void setSynchronizedWithTransaction(boolean synchronizedWithTransaction)
    {
        this.synchronizedWithTransaction = synchronizedWithTransaction;
    }

    /**
     * Return whether the resource is synchronized with a transaction
     * @return
     */
    public boolean isSynchronizedWithTransaction()
    {
        return this.synchronizedWithTransaction;
    }

    /**
     * Mark the resource transaction as rollback-only
     */
    public void setRollbackOnly()
    {
        this.rollbackOnly = true;
    }

    /**
     * Reset the rollback-only status for this resource transaction.
     * Only really intended to be called after custom rollback steps which
     * keep the original resources in action, e.g. in case of a savepoint
     */
    public void resetRollbackOnly()
    {
        this.rollbackOnly = false;
    }

    /**
     * Return whether the resource transaction is marked as rollback-only
     * @return
     */
    public boolean isRollbackOnly() {
        return this.rollbackOnly;
    }

    /**
     * Set the timeout for this object in seconds
     * @param seconds
     */
    public void setTimeoutSeconds(int seconds)
    {
        setTimeoutInMillis(seconds * 1000);
    }

    /**
     * Set the timeout for this object in milliseconds
     * @param millis
     */
    public void setTimeoutInMillis(long millis)
    {
        this.deadline = new Date(System.currentTimeMillis() + millis);
    }

    /**
     * Return whether this object has an associated timeout
     * @return
     */
    public boolean hasTimeout()
    {
        return (this.deadline != null);
    }

    /**
     * Return the expiration deadline of this object
     * @return
     */
    public Date getDeadline()
    {
        return this.deadline;
    }

    /**
     * Return the time to live for this object in seconds
     * Rounds up eagerly, e.g. 9.00001 still to 10
     * @return
     */
    public int getTimeToLiveSeconds()
    {
        double diff = ((double) getTimeToLiveMillis())/1000;
        int secs = (int) Math.ceil(diff);
        checkTransactionTimeout(secs <=0);
        return secs;
    }

    /**
     * Return the time to live for this object in milliseconds
     * @return
     * @throws TransactionTimedOutException
     */
    public long getTimeToLiveMillis() throws TransactionTimedOutException
    {
        if(this.deadline == null)
        {
            throw new IllegalStateException("No timeout specified for this resource holder");
        }
        long timeToLive = this.deadline.getTime() - System.currentTimeMillis();
        checkTransactionTimeout(timeToLive <= 0);
        return timeToLive;
    }

    /**
     * Set the transaction rollback-only if the deadline has been reached,
     * and throw a TransactionTimedOutException.
     * @param deadlineReached
     */
    private void checkTransactionTimeout(boolean deadlineReached)
    {
        if (deadlineReached)
        {
            setRollbackOnly();
            throw new TransactionTimedOutException("Transaction timed out: deadline was "+ this.deadline);
        }
    }

    /**
     * Increase the reference count by one because the holder has been requested
     * (i.e. someone requested the resource held by it)
     */
    public void requested()
    {
        this.referenceCount++;
    }

    /**
     * Decrease the reference count by one because the holder has been released
     * (i.e. someone released the resource held by it)
     */
    public void released()
    {
        this.referenceCount--;
    }

    /**
     * Return whether there are still open references to this holder
     * @return
     */
    public boolean isOpen()
    {
        return (this.referenceCount > 0);
    }

    /**
     * Clear the transactional state of this resource holder
     */
    public void clear()
    {
        this.synchronizedWithTransaction = false;
        this.rollbackOnly = false;
        this.deadline = null;
    }

    /**
     * Reset this resource holder - transactional state as well as reference count
     */
    @Override
    public void reset() {
        clear();
        this.referenceCount = 0;
    }

    @Override
    public void unbound() {
        this.isVoid = true;
    }

    @Override
    public boolean isVoid() {
        return this.isVoid;
    }
}
