package org.garry.transaction.support;

/**
 * Default implementation of the {@link org.garry.transaction.TransactionStatus}
 * interface, used by
 */
public class DefaultTransactionStatus extends AbstractTransactionStatus{

    @Override
    public boolean isNewTransaction() {
        return false;
    }

}
