package org.garry.transaction.support;

/**
 * a start for custom transaction
 * manager implementations
 */
public class SimpleTransactionStatus extends AbstractTransactionStatus{

    private final boolean newTransaction;

    public SimpleTransactionStatus() {
        this(true);
    }

    public SimpleTransactionStatus(boolean newTransaction) {
        this.newTransaction = newTransaction;
    }

    @Override
    public boolean isNewTransaction() {
       return this.newTransaction;
    }
}
