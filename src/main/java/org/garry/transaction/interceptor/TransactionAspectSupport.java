package org.garry.transaction.interceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.garry.transaction.PlatformTransactionManager;
import org.garry.transaction.TransactionStatus;
import org.garry.transaction.TransactionSystemException;
import org.garry.transaction.support.CallbackPreferringPlatformTransactionManager;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.NamedThreadLocal;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

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

    protected final Log logger = LogFactory.getLog(getClass());

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
    public void afterPropertiesSet(){

    }

    /**
     * General delegate for around-advice-based subclasses, delegating to several other template
     * methods on this class. Able to handle {@link CallbackPreferringPlatformTransactionManager}
     * as well as regular {@link PlatformTransactionManager} implementations
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

    /**
     * Create a transaction if necessary based on the given TransactionAttribute
     * Allows callers to perform custom TransactionAttribute lookups through
     * thr TransactionAttributeSource
     * @param tm
     * @param txAttr
     * @param joinpointIdentification the fully qualified method name(used for monitoring and logging purposes)
     * @return
     */
    protected TransactionInfo createTransactionIfNecessary(@Nullable PlatformTransactionManager tm,
                                                           @Nullable TransactionAttribute txAttr, final String joinpointIdentification)
    {
        // If no name specified, apply method identification as transaction name
        if(txAttr != null && txAttr.getName() == null)
        {
            txAttr = new DelegatingTransactionAttribute(txAttr)
            {
                @Override
                public String getName() {
                   return joinpointIdentification;
                }
            };
        }

        TransactionStatus status = null;
        if(txAttr != null)
        {
            if(tm != null)
            {
                status = tm.getTransaction(txAttr);
            }
            else
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("Skipping transactional joinpoint [" + joinpointIdentification +
                            "] because to transaction manager has been configured");
                }
            }
        }
        return prepareTransactionInfo(tm,txAttr,joinpointIdentification,status);
    }

    /**
     * Prepare a TransactionInfo for the given attribute and status object.
     * @param tm
     * @param txAttr
     * @param joinpointIdentification
     * @param status the TransactionStatus for the current transaction
     * @return the prepared TransactionInfo object
     */
    protected TransactionInfo prepareTransactionInfo(@Nullable PlatformTransactionManager tm,
                                                     @Nullable TransactionAttribute txAttr, String joinpointIdentification,
                                                     @Nullable TransactionStatus status)
    {
        TransactionInfo txInfo = new TransactionInfo(tm, txAttr, joinpointIdentification);
        if(txAttr != null)
        {
            // We need a transaction for this method
            if(logger.isTraceEnabled())
            {
                logger.trace("Getting transaction for [" + txInfo.getJoinpointIdentification() + "]");
            }
            // The transaction manager will flag an error if an incompatible tx already exists
            txInfo.newTransactionStatus(status);
        }
        else
        {
            // The TransactionInfo.hasTransaction() method will return false. We created it only
            // to preserve the integrity of the ThreadLocal stack maintained in this class
            if(logger.isTraceEnabled())
            {
                logger.trace("Don't need to create transaction for [ " + joinpointIdentification +
                        "]: This method isn't transactional.");
            }
        }
        // We always bind the TransactionInfo to the thread, even if we didn't create
        // a new transaction here. This guarantees that the TransactionInfo stack
        // will be managed correctly even if no transaction was created by this aspect
        txInfo.bindToThread();
        return txInfo;
    }

    /**
     * Execute after successful completion of call, but not after an exception was handled
     * Do nothing if we didn't create a transaction
     * @param txInfo
     */
    protected void commitTransactionAfterReturning(@Nullable TransactionInfo txInfo)
    {
        if(txInfo !=null && txInfo.getTransactionStatus() != null)
        {
            if(logger.isTraceEnabled())
            {
                logger.trace("Completing transaction for ["+txInfo.getJoinpointIdentification() + "]");
            }
            txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());
        }
    }

    /**
     * Handle a throwable, completing the transaction
     * We may commit or roll back, depending on the configuration
     * @param txInfo
     * @param ex
     */
    protected void completeTransactionAfterThrowing(@Nullable TransactionInfo txInfo, Throwable ex)
    {
        if(txInfo != null && txInfo.getTransactionStatus() !=null)
        {
            if(logger.isTraceEnabled())
            {
                logger.trace("Completing transaction for [" + txInfo.getJoinpointIdentification() +
                        "] after exception: " + ex);
            }
            if(txInfo.transactionAttribute != null && txInfo.transactionAttribute.rollbackOn(ex))
            {
                try {
                    txInfo.getTransactionManager().rollback(txInfo.getTransactionStatus());
                }
                catch (TransactionSystemException ex2) {
                    logger.error("Application exception overridden by rollback exception", ex);
                    ex2.initApplicationException(ex);
                    throw ex2;
                }
                catch (RuntimeException ex2) {
                    logger.error("Application exception overridden by rollback exception", ex);
                    throw ex2;
                }
                catch (Error err) {
                    logger.error("Application exception overridden by rollback error", ex);
                    throw err;
                }
            }
            else
            {
                // We don't roll back on this exception.
                // Will still roll back if TransactionStatus.isRollbackOnly() is true.
                try {
                    txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());
                }
                catch (TransactionSystemException ex2) {
                    logger.error("Application exception overridden by commit exception", ex);
                    ex2.initApplicationException(ex);
                    throw ex2;
                }
                catch (RuntimeException ex2) {
                    logger.error("Application exception overridden by commit exception", ex);
                    throw ex2;
                }
                catch (Error err) {
                    logger.error("Application exception overridden by commit error", ex);
                    throw err;
                }
            }
        }
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
    protected final class TransactionInfo {

        @Nullable
        private final PlatformTransactionManager transactionManager;

        @Nullable
        private final TransactionAttribute transactionAttribute;

        private final String joinpointIdentification;

        @Nullable
        private TransactionStatus transactionStatus;

        @Nullable
        private TransactionInfo oldTransactionInfo;

        public TransactionInfo(@Nullable PlatformTransactionManager transactionManager,
                               @Nullable TransactionAttribute transactionAttribute, String joinpointIdentification) {
            this.transactionManager = transactionManager;
            this.transactionAttribute = transactionAttribute;
            this.joinpointIdentification = joinpointIdentification;
        }

        @Nullable
        public PlatformTransactionManager getTransactionManager() {
            Assert.state(this.transactionManager != null, "No PlatformTransactionManager set");
            return this.transactionManager;
        }

        @Nullable
        public TransactionAttribute getTransactionAttribute() {
            return this.transactionAttribute;
        }

        /**
         * Return a String representation of this joipoint (usually a Method call)
         * for use in logging
         *
         * @return
         */
        public String getJoinpointIdentification() {
            return this.joinpointIdentification;
        }

        public void newTransactionStatus(@Nullable TransactionStatus status) {
            this.transactionStatus = status;
        }

        @Nullable
        public TransactionStatus getTransactionStatus() {
            return this.transactionStatus;
        }

        /**
         * Return whether a transaction was created by the aspect,
         * or whether we just have a placeholder to keep ThreadLocal stack integrity
         *
         * @return
         */
        public boolean hasTransaction() {
            return (this.transactionStatus != null);
        }

        private void bindToThread() {
            // Expose current TransactionStatus, preserving any existing TransactionStatus
            // for restoration after this transaction is complete
            this.oldTransactionInfo = transactionInfoHolder.get();
            transactionInfoHolder.set(this);
        }

        private void restoreThreadLocalStatus() {
            // Use stack to restore old transaction TransactionInfo
            // Will be null if none was set
            transactionInfoHolder.set(this.oldTransactionInfo);
        }

        @Override
        public String toString() {
            return (this.transactionAttribute != null ? this.transactionAttribute.toString() : "No transaction");
        }
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
