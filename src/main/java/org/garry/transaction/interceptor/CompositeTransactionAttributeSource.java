package org.garry.transaction.interceptor;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @ClassName Composite {@link TransactionAttributeSource} implementation that iterates
 * over a given array of {@link TransactionAttributeSource} instances
 * @Description TODO 组合模式？？？
 * @Author cy
 * @Date 2021/4/25 20:13
 */
public class CompositeTransactionAttributeSource implements TransactionAttributeSource, Serializable {

    private final TransactionAttributeSource[] transactionAttributeSources;

    public CompositeTransactionAttributeSource(TransactionAttributeSource[] transactionAttributeSources) {
        Assert.notNull(transactionAttributeSources,"TransactionAttributeSource array must not be null");
        this.transactionAttributeSources = transactionAttributeSources;
    }

    /**
     * Return the transactionAttributeSource instances that this
     * CompositeTransactionAttributeSource combines
     * @return
     */
    public final TransactionAttributeSource[] getTransactionAttributeSources()
    {
        return this.transactionAttributeSources;
    }

    @Nullable
    @Override
    public TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass) {
        for(TransactionAttributeSource tas: this.transactionAttributeSources)
        {
            TransactionAttribute ta = tas.getTransactionAttribute(method, targetClass);
            if(ta != null)
            {
                return ta;
            }
        }
        return null;
    }


}
