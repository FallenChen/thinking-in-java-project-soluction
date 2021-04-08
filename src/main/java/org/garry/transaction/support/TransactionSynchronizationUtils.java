package org.garry.transaction.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.core.InfrastructureProxy;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.List;

/**
 * Utility methods for triggering specific {@link TransactionSynchronization}
 * callback methods on all currently registered synchronizations
 */
public abstract class TransactionSynchronizationUtils {

    private static final Log logger = LogFactory.getLog(TransactionSynchronizationUtils.class);

    private static final boolean aopAvailable = ClassUtils.isPresent(
            "org.springframework.aop.scope.ScopedObject",TransactionSynchronizationUtils.class.getClassLoader()
    );

    /**
     * Check whether the given resource transaction managers refers to the given (underlying) resource factory
     * @param tm
     * @param resourceFactory
     * @return
     */
    public static boolean sameResourceFactory(ResourceTransactionManager tm, Object resourceFactory)
    {
        return unwrapResourceIfNecessary(tm.getResourceFactory()).equals(unwrapResourceIfNecessary(resourceFactory));
    }

    /**
     * todo why???
     * Unwrap the given resource handle if necessary; otherwise return
     * @param resource
     * @return
     */
    static Object unwrapResourceIfNecessary(Object resource)
    {
        Assert.notNull(resource,"Resource must not be null");
        Object resourceRef = resource;
        // unwrap infrastructure proxy
        if(resourceRef instanceof InfrastructureProxy)
        {
            resourceRef = ((InfrastructureProxy)resourceRef).getWrappedObject();
        }
        if(aopAvailable)
        {
            // now unwrap second proxy
            resourceRef = ScopedProxyUnwrapper.unwrapIfNecessary(resourceRef);
        }
        return resourceRef;
    }

    public static void triggerFlush()
    {
    }

    /**
     * Trigger {@code beforeCommit} callbacks on all currently registered synchronizations.
     *
     * @param readOnly
     */
    public static void triggerBeforeCommit(boolean readOnly)
    {

    }

    public static void triggerBeforeCompletion()
    {

    }

    public static void triggerAfterCommit()
    {

    }

    /**
     * Actually invoke the {@code afterCommit} methods of the
     * given Spring TransactionSynchronization objects
     * @param synchronizations
     */
    public static void invokeAfterCommit(@Nullable List<TransactionSynchronization> synchronizations)
    {

    }

    /**
     * Trigger {@code afterCompletion} callbacks on all currently registered synchronizations
     * @param completionStatus
     */
    public static void triggerAfterCompletion(int completionStatus)
    {

    }

    /**
     * Actually invoke the {@code afterCompletion} methods of the
     * given Spring TransactionSynchronization objects
     * @param synchronizations
     * @param completionStatus
     */
    public static void invokeAfterCompletion(@Nullable List<TransactionSynchronization> synchronizations,
                                             int completionStatus)
    {
       if(synchronizations != null)
       {

       }
    }

    /**
     * todo ???
     * Inner class to avoid hard-code dependency on AOP module
     */
    private static class ScopedProxyUnwrapper
    {
        public static Object unwrapIfNecessary(Object resource)
        {
            if(resource instanceof ScopedObject)
            {
                return ((ScopedObject)resource).getTargetObject();
            }
            else
            {
                return resource;
            }
        }
    }
}
