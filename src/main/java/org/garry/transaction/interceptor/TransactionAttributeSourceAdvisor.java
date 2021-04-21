package org.garry.transaction.interceptor;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.lang.Nullable;

/**
 * @ClassName TransactionAttributeSourceAdvisor
 * @Description TODO
 * @Author cy
 * @Date 2021/4/21 19:51
 */
public class TransactionAttributeSourceAdvisor extends AbstractPointcutAdvisor {

    @Nullable
    private TransactionInterceptor transactionInterceptor;


    @Override
    public Pointcut getPointcut() {
        return null;
    }

    @Override
    public Advice getAdvice() {
        return null;
    }
}
