package org.garry.transaction.interceptor;

import org.springframework.aop.framework.AbstractSingletonProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class TransactionProxyFactoryBean extends AbstractSingletonProxyFactoryBean
        implements BeanFactoryAware{

    @Override
    protected Object createMainInterceptor() {
        return null;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {

    }
}
