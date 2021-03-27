package org.garry.transaction.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.core.InfrastructureProxy;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

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
//        for(TransactionSynchronization synchronization: TransactionSynchronizationManager.)
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
