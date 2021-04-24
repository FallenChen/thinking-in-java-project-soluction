package org.garry.transaction.interceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodClassKey;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName AbstractFallbackTransactionAttributeSource
 * @Description Abstract implementation of {@link TransactionAttributeSource} that caches
 * attributes for methods and implements a fallback policy: 1. specific target
 * method.2.target class;3. declaring method;4. declaring class/interface
 *
 *
 * @Author cy
 * @Date 2021/4/23 20:10
 */
public abstract class AbstractFallbackTransactionAttributeSource implements TransactionAttributeSource {

    private final static TransactionAttribute NULL_TRANSACTION_ATTRIBUTE = new DefaultTransactionAttribute();

    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Cache of TransactionAttributes, keyed by method on a specific target class.
     *
     */
    private final Map<Object,TransactionAttribute> attributeCache = new ConcurrentHashMap<>(1024);

    /**
     * Determine the transaction attribute for this method invocation.
     * @param method
     * @param targetClass
     * @return
     */
    @Nullable
    @Override
    public TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass) {
        if(method.getDeclaringClass() == Object.class)
        {
            return null;
        }

        // First, see if we have a cached value
        Object cacheKey = getCacheKey(method,targetClass);
        Object cached = this.attributeCache.get(cacheKey);
        if(cached != null)
        {
            // Value will either ba canonical value indicating there is no transaction attribute,
            // or an actual transaction attribute
            if(cached == NULL_TRANSACTION_ATTRIBUTE)
            {
                return null;
            }
            else
            {
                return (TransactionAttribute) cached;
            }

        }
        else
        {
            // We need to work it out
            TransactionAttribute txAttr = computeTransactionAttribute(method, targetClass);
            // Put it in the cache
            if(txAttr == null)
            {
                this.attributeCache.put(cacheKey, NULL_TRANSACTION_ATTRIBUTE);
            }
            else
            {
                String methodIdentification = ClassUtils.getQualifiedMethodName(method, targetClass);
                if(txAttr instanceof DefaultTransactionAttribute)
                {
                    ((DefaultTransactionAttribute) txAttr).setDescriptor(methodIdentification);
                }
                if(logger.isDebugEnabled())
                {
                    logger.debug("Adding transactional method " + methodIdentification + " with attribute: " + txAttr);
                }
                this.attributeCache.put(cacheKey,txAttr);
            }
            return txAttr;
        }
    }

    /**
     * Determine a cache key for the given method and target class
     * @param method
     * @param targetClass
     * @return
     */
    protected Object getCacheKey(Method method, @Nullable Class<?> targetClass)
    {
       return new MethodClassKey(method,targetClass);
    }

    /**
     * Same signature as {@link #getTransactionAttribute}, but doesn't cache the result
     * @param method
     * @param targetClass
     * @return
     */
    protected TransactionAttribute computeTransactionAttribute(Method method, @Nullable Class<?> targetClass)
    {
        // Don't allow no-public methods as required
        if(allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers()))
        {
            return null;
        }

        // Ignore CGLIB subclasses - introspect the actual user class
        Class<?> userClass = targetClass != null ? ClassUtils.getUserClass(targetClass) : null;
        // The method may ba on an interface, but we need attributes from the target class.
        // If the target class is null, the method will be unchanged
        Method specificMethod = ClassUtils.getMostSpecificMethod(method, userClass);
        // If we are dealing with method with generic parameters, find the original method
        specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);

        // First try is the method is the target class
        TransactionAttribute txAttr = findTransactionAttribute(specificMethod);
        if(txAttr != null)
        {
            return txAttr;
        }

        // Second try is the transaction attribute on the target class
        txAttr = findTransactionAttribute(specificMethod.getDeclaringClass());
        if(txAttr != null && ClassUtils.isUserLevelMethod(method))
        {
            return txAttr;
        }

        if(specificMethod != method)
        {
            // Fallback is to look at the original method
            txAttr = findTransactionAttribute(method);
            if(txAttr != null)
            {
                return txAttr;
            }

            // Last fallback is the class of the original method
            txAttr = findTransactionAttribute(method.getDeclaringClass());
            if(txAttr != null && ClassUtils.isUserLevelMethod(method))
            {
                return txAttr;
            }
        }
        return null;
    }

    /**
     * Subclasses method to implement this to return the transaction attribute
     * for the given method, if any
     * @param method
     * @return
     */
    @Nullable
    protected abstract TransactionAttribute findTransactionAttribute(Method method);


    /**
     * Subclass method to implement this to return the transaction
     * @param clazz
     * @return
     */
    @Nullable
    protected abstract TransactionAttribute findTransactionAttribute(Class<?> clazz);

    /**
     * Should only public methods be allowed to have transactional semantics?
     * @return
     */
    protected boolean allowPublicMethodsOnly()
    {
        return true;
    }
}
