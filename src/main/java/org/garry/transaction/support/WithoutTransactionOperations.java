package org.garry.transaction.support;

import org.garry.transaction.TransactionException;

/**
 * @ClassName WithoutTransactionOperations
 * @Description TODO
 * @Author cy
 * @Date 2021/5/7 16:00
 */
public class WithoutTransactionOperations implements TransactionOperations{

    static final WithoutTransactionOperations INSTANCE = new WithoutTransactionOperations();

    private WithoutTransactionOperations()
    {

    }

    @Override
    public <T> T execute(TransactionCallback<T> action) throws TransactionException {
       return action.doInTransaction(new SimpleTransactionStatus(false));
    }
}
