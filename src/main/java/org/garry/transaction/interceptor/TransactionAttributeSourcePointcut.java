package org.garry.transaction.interceptor;

import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @ClassName TransactionAttributeSourcePointcut
 * @Description Inner class that implements a Pointcut that matches if the underlying
 * {@link TransactionAttribute} has an attribute for a given method
 * @Author cy
 * @Date 2021/4/21 19:59
 */
abstract class TransactionAttributeSourcePointcut extends StaticMethodMatcherPointcut implements Serializable {

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
       if(targetClass != null && TransactionalProxy.class.isAssignableFrom(targetClass))
       {
           return false;
       }

        TransactionAttributeSource tas = getTransactionAttributeSource();
       return (tas == null || tas.getTransactionAttribute(method,targetClass) != null);
    }

    @Override
    public boolean equals(Object other) {
        if(this == other)
        {
            return true;
        }

        if(!(other instanceof TransactionAttributeSourcePointcut))
        {
            return false;
        }

        TransactionAttributeSourcePointcut otherPc = (TransactionAttributeSourcePointcut) other;
        return ObjectUtils.nullSafeEquals(getTransactionAttributeSource(),otherPc.getTransactionAttributeSource());
    }

    @Override
    public int hashCode() {
       return TransactionAttributeSourcePointcut.class.hashCode();
    }

    @Override
    public String toString() {
       return getClass().getName() + ": " + getTransactionAttributeSource();
    }

    /**
     * Obtain the underlying TransactionAttributeSource
     * To be implemented by subclasses
     * @return
     */
    protected abstract TransactionAttributeSource getTransactionAttributeSource();
}
