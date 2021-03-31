package org.garry.transaction.interceptor;

import org.garry.transaction.PlatformTransactionManager;
import org.garry.transaction.TransactionStatus;
import org.garry.transaction.support.CallbackPreferringPlatformTransactionManager;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.NamedThreadLocal;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
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

    /**
     * General delegate for around-advice-based subclasses, delegating to several other template
     * methods on this class.
     * @param method
     * @param targetClass
     * @param invocation
     * @return
     */
    protected Object invokeWithinTransaction(Method method, @Nullable Class<?> targetClass,
                                             final InvocationCallback invocation) throws Throwable
    {
        // If the transaction attribute is null, the method is non-transactional
        TransactionAttributeSource tas = getTransactionAttributeSource();
        final TransactionAttribute txAttr = (tas != null ? tas.getTransactionAttribute(method,targetClass) : null);
        final PlatformTransactionManager tm = determineTransactionManager(txAttr);
        final String jointpointIdentification = methodIdentification(method,targetClass,txAttr);

        if(txAttr == null || !(tm instanceof CallbackPreferringPlatformTransactionManager))
        {
            // Standard transaction demarcation with getTransaction and commit/rollback calls
            TransactionInfo txInfo = createTransactionIfNecessary(tm, txAttr, jointpointIdentification);
            Object retVal = null;
            try
            {
                // This is an around advice: Invoke the next interceptor in the chain
                // This will normally result in a target object being invoked
                retVal = invocation.proceedWithInvocation();
            }
            catch (Throwable ex)
            {
                // target invocation exception
                completeTransactionAfterThrowing(txInfo,ex);
                throw ex;
            }
            finally {
                cleanupTransactionInfo(txInfo);
            }
            commitTransactionAfterReturning(txInfo);
            return retVal;
        }
        else
        {
            // It's a CallbackPreferringPlatformTransactionManager: pass a TransactionCallback in
            try
            {
                Object result = ((CallbackPreferringPlatformTransactionManager) tm).execute(txAttr, status -> {
                    TransactionInfo txInfo = prepareTransactionInfo(tm, txAttr, jointpointIdentification, status);
                    try {
                        return invocation.proceedWithInvocation();
                    } catch (Throwable ex) {
                        if (txAttr.rollbackOn(ex)) {
                            // A RuntimeException: will lead to a rollback
                            if (ex instanceof RuntimeException) {
                                throw (RuntimeException) ex;
                            } else {
                                throw new ThrowableHolderException(ex);
                            }
                        } else {
                            // A normal return value: will lead to a commit
                            return new ThrowableHolder(ex);
                        }
                    } finally {
                        cleanupTransactionInfo(txInfo);
                    }
                });

                // Check result: It might indicate a Throwable to rethrow
                if(result instanceof ThrowableHolder)
                {
                    throw ((ThrowableHolder) result).getThrowable();
                }
                else
                {
                    return result;
                }
            }
            catch (ThrowableHolderException ex)
            {
                throw ex.getCause();
            }
        }
    }

    protected void clearTransactionManagerCache()
    {

    }

    protected PlatformTransactionManager determineTransactionManager(@Nullable TransactionAttribute txAttr)
    {

        return null;
    }

    private String methodIdentification(Method method, @Nullable Class<?> targetClass,
                                        @Nullable TransactionAttribute txAttr)
    {
        return null;
    }


    protected TransactionInfo createTransactionIfNecessary(@Nullable PlatformTransactionManager tm,
                                                           @Nullable TransactionAttribute txAttr, final String joinpointIdentification)
    {
        return null;
    }

    protected TransactionInfo prepareTransactionInfo(@Nullable PlatformTransactionManager tm,
                                                     @Nullable TransactionAttribute txAttr, String joinpointIdentification,
                                                     @Nullable TransactionStatus status)
    {
        return null;
    }

    protected void commitTransactionAfterReturning(@Nullable TransactionInfo txInfo)
    {

    }

    protected void completeTransactionAfterThrowing(@Nullable TransactionInfo txInfo, Throwable ex)
    {

    }

    protected void cleanupTransactionInfo(@Nullable TransactionInfo txInfo)
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

    /**
     * Internal holder class for a Throwable, used as a return value
     * from a TransactionCallback (to be subsequently unwrapped again)
     */
    private static class ThrowableHolder
    {
        private final Throwable throwable;

        public ThrowableHolder(Throwable throwable) {
            this.throwable = throwable;
        }

        public final Throwable getThrowable() {
           return this.throwable;
        }
    }

    /**
     * Internal holder class for a Throwable, used as a RuntimeException to be
     * thrown from a TransactionCallback (and subsequently unwrapped again)
     */
    private static class ThrowableHolderException extends RuntimeException
    {
        public ThrowableHolderException(Throwable throwable)
        {
            super(throwable);
        }

        @Override
        public String toString() {
           return getCause().toString();
        }
    }
}
