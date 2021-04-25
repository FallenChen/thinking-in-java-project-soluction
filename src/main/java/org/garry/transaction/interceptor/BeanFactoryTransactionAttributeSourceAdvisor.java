package org.garry.transaction.interceptor;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;
import org.springframework.lang.Nullable;

/**
 * @ClassName BeanFactoryTransactionAttributeSourceAdvisor
 * @Description Advisor driven by a {@link TransactionAttributeSource}, used to include
 * a transaction advice bean for methods that are transactional
 * @Author cy
 * @Date 2021/4/25 20:05
 */
public class BeanFactoryTransactionAttributeSourceAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    @Nullable
    private TransactionAttributeSource transactionAttributeSource;

    private final TransactionAttributeSourcePointcut pointcut = new TransactionAttributeSourcePointcut() {
        @Override
        protected TransactionAttributeSource getTransactionAttributeSource() {
            return transactionAttributeSource;
        }
    };

    /**
     * Set the transaction attribute source which is used to find transaction
     * attributes. This should usually be identical to the source reference
     * set on the transaction interceptor itself
     * @param transactionAttributeSource
     */
    public void setTransactionAttributeSource(@Nullable TransactionAttributeSource transactionAttributeSource) {
        this.transactionAttributeSource = transactionAttributeSource;
    }

    /**
     *
     * @param classFilter
     */
    public void setClassFilter(ClassFilter classFilter)
    {
        this.pointcut.setClassFilter(classFilter);
    }

    @Override
    public Pointcut getPointcut() {
        return null;
    }
}
