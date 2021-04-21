package org.garry.transaction.interceptor;

import org.garry.transaction.PlatformTransactionManager;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.AbstractSingletonProxyFactoryBean;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.lang.Nullable;

import java.util.Properties;

/**
 * Proxy factory bean for simplified declarative transaction handling.
 * This is a convenient alternative to a standard AOP
 *
 */
public class TransactionProxyFactoryBean extends AbstractSingletonProxyFactoryBean
        implements BeanFactoryAware{

    private final TransactionInterceptor transactionInterceptor = new TransactionInterceptor();

    @Nullable
    private Pointcut pointcut;

    public void setTransactionManager(PlatformTransactionManager transactionManager)
    {
        this.transactionInterceptor.setTransactionManager(transactionManager);
    }

    public void setTransactionAttributes(Properties transactionAttributes)
    {
        this.transactionInterceptor.setTransactionAttributes(transactionAttributes);
    }

    public void setTransactionAttributeSource(TransactionAttributeSource transactionAttributeSource)
    {
        this.transactionInterceptor.setTransactionAttributeSource(transactionAttributeSource);
    }

    public void setPointcut(Pointcut pointcut)
    {
        this.pointcut = pointcut;
    }



    @Override
    protected Object createMainInterceptor() {
        this.transactionInterceptor.afterPropertiesSet();
        if (this.pointcut != null)
        {
            return new DefaultPointcutAdvisor(this.pointcut, this.transactionInterceptor);
        }
        else
        {
            // Rely on default pointcut
            return new TransactionAttributeSourceAdvisor(this.transactionInterceptor);
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.transactionInterceptor.setBeanFactory(beanFactory);
    }

    @Override
    protected void postProcessProxyFactory(ProxyFactory proxyFactory) {
       proxyFactory.addInterface(TransactionalProxy.class);
    }
}
