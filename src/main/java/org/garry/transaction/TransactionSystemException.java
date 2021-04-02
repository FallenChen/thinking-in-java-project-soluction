package org.garry.transaction;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Exception thrown when a general transaction system error is encountered,
 * like on commit or rollback
 */
public class TransactionSystemException extends TransactionException {


    @Nullable
    private Throwable applicationException;

    public TransactionSystemException(String msg) {
        super(msg);
    }

    public TransactionSystemException(String msg, Throwable cause)
    {
        super(msg,cause);
    }

    /**
     * Set an application exception that was thrown before this transaction exception,
     * preserving the original exception despite the overriding TransactionSystemException.
     * @param ex
     */
    public void initApplicationException(Throwable ex)
    {
        Assert.notNull(ex,"Application exception must not be null");
        if(this.applicationException != null)
        {
            throw new IllegalStateException("Already holding an application exception: " + this.applicationException);
        }
        this.applicationException = ex;
    }

    @Nullable
    public Throwable getApplicationException() {
        return this.applicationException;
    }

    @Override
    public boolean contains(Class<?> exType) {
       return super.contains(exType) || (exType != null && exType.isInstance(this.applicationException));
    }
}
