package org.garry.transaction.interceptor;

import org.garry.transaction.PlatformTransactionManager;
import org.garry.transaction.TransactionStatus;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.NamedThreadLocal;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;

/**
 * Base class for transactional aspects
 *
 * This
 */
public abstract class TransactionAspectSupport implements BeanFactoryAware, InitializingBean {


    private static final ThreadLocal<TransactionInfo> transactionInfoHolder =
            new NamedThreadLocal<>("Current aspect-driven transaction");

    @Nullable
    private String transactionManagerBeanName;

    @Nullable
    private PlatformTransactionManager transactionManager;

    @Nullable
    private TransactionAttributeSource transactionAttributeSource;

    @Nullable
    private BeanFactory beanFactory;

    private final ConcurrentMap<Object, PlatformTransactionManager> transactionManagerCache =
            new ConcurrentHashMap<>(4);

    protected static TransactionInfo currentTransactionInfo()
    {
        return null;
    }

    public static TransactionStatus currentTransactionStatus()
    {
       return null;
    }


    @Nullable
    public String getTransactionManagerBeanName() {
        return this.transactionManagerBeanName;
    }

    public void setTransactionManagerBeanName(@Nullable String transactionManagerBeanName) {
        this.transactionManagerBeanName = transactionManagerBeanName;
    }

    public void setTransactionManager(@Nullable PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Nullable
    public PlatformTransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    public void setTransactionAttributes(Properties transactionAttributes)
    {
    }

    public void setTransactionAttributeSources(TransactionAttributeSource... transactionAttributeSources)
    {

    }

    public void setTransactionAttributeSource(@Nullable TransactionAttributeSource transactionAttributeSource) {
        this.transactionAttributeSource = transactionAttributeSource;
    }

    @Nullable
    public TransactionAttributeSource getTransactionAttributeSource() {
        return this.transactionAttributeSource;
    }

    @Override
    public void setBeanFactory(@Nullable BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Nullable
    public BeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    protected Object invokeWithinTransaction(Method method, @Nullable Class<?> targetClass,
                                             final InvocationCallback invocation)
    {
        return null;
    }

    protected void clearTransactionManagerCache()
    {

    }

    protected PlatformTransactionManager determineTransactionManager(@Nullable TransactionAttribute txAttr)
    {

    }

    @FunctionalInterface
    protected interface InvocationCallback
    {
        Object proceedWithInvocation() throws Throwable;
    }

    /**
     * Opaque object used to hold Transaction information. Subclasses
     * must pass it back to methods on this class, but not see its internals
     */
    protected final class TransactionInfo{

    }
}
