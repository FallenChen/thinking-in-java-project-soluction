package org.garry.transaction.interceptor;

/**
 * @ClassName AbstractFallbackTransactionAttributeSource
 * @Description Abstract implementation of {@link TransactionAttributeSource} that caches
 * attributes for methods and implements a fallback policy: 1. specific target
 * method.2.target class;3. declaring method;4. declaring class/interface
 *
 *
 * @Author cy
 * @Date 2021/4/23 20:10
 */
public abstract class AbstractFallbackTransactionAttributeSource implements TransactionAttributeSource {

    private final static TransactionAttribute NULL_TRANSACTION_ATTRIBUTE = new DefaultTransactionAttribute();
}
