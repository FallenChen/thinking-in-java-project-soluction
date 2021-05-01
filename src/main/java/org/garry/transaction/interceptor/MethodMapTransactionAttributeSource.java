package org.garry.transaction.interceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PatternMatchUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MethodMapTransactionAttributeSource
 * @Description Simple {@link TransactionAttributeSource} implementation that
 * allows attributes to be stored per method in a {@link Map}
 * @Author cy
 * @Date 2021/5/1 19:41
 */
public class MethodMapTransactionAttributeSource
        implements TransactionAttributeSource, BeanClassLoaderAware, InitializingBean {

    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Map from method name to attribute value
     */
    @Nullable
    private Map<String,TransactionAttribute> methodMap;

    @Nullable
    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    private boolean eagerlyInitialized = false;

    private boolean initialized = false;

    /**
     * Map from method to TransactionAttribute
     */
    private final Map<Method, TransactionAttribute> transactionAttributeMap = new HashMap<>();

    /**
     * Map from Method to name pattern used for registration
     */
    private final Map<Method,String> methodNameMap = new HashMap<>();

    /**
     * Intended for configuration via setter injection, typically within
     * a Spring bean factory. Relies on {@link #afterPropertiesSet()}
     * @param methodMap
     */
    public void setMethodMap(@Nullable Map<String, TransactionAttribute> methodMap) {
        this.methodMap = methodMap;
    }

    @Override
    public void setBeanClassLoader(@Nullable ClassLoader beanClassLoader) {
        this.beanClassLoader = beanClassLoader;
    }

    /**
     * Eagerly initializes the specified
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        initMethodMap(this.methodMap);
        this.eagerlyInitialized = true;
        this.initialized = true;
    }

    protected void initMethodMap(@Nullable Map<String,TransactionAttribute> methodMap)
    {
        if(methodMap != null)
        {
            methodMap.forEach(this::addTransactionalMethod);
        }
    }

    /**
     * Add an attribute for a transactional method
     * @param name
     * @param attr
     */
    public void addTransactionalMethod(String name, TransactionAttribute attr)
    {
        Assert.notNull(name, "Name must not be null");
        int lastDotIndex = name.lastIndexOf('.');
        if(lastDotIndex == -1)
        {
            throw new IllegalArgumentException("'" + name + "' is not a valid method name: format is FQN.methodName");
        }
        String className = name.substring(0, lastDotIndex);
        String methodName = name.substring(lastDotIndex + 1);
        Class<?> clazz = ClassUtils.resolveClassName(className, this.beanClassLoader);
        addTransactionalMethod(clazz, methodName, attr);
    }

    public void addTransactionalMethod(Class<?> clazz, String mappedName, TransactionAttribute attr)
    {
       Assert.notNull(clazz,"Class must not be null");
       Assert.notNull(mappedName, "Mapped name must not be null");
       String name = clazz.getName() + '.' + mappedName;

        Method[] methods = clazz.getDeclaredMethods();
        List<Method> matchingMethods = new ArrayList<>();
        for(Method method: methods)
        {
            if(isMatch(method.getName(), mappedName))
            {
                matchingMethods.add(method);
            }
        }
        if(matchingMethods.isEmpty())
        {
            throw new IllegalArgumentException(
                    "Could not find method '" + mappedName + "' on class [" + clazz.getName() + "]"
            );
        }
        // Register all matching methods
        for(Method method : matchingMethods)
        {
            String regMethodName = this.methodNameMap.get(method);
            if(regMethodName == null || (!regMethodName.equals(name) && regMethodName.length() <= name.length()))
            {
                // No already registered method name, or more specific
                // method name specification now -> (re-)register method
                if(logger.isDebugEnabled() && regMethodName != null)
                {
                    logger.debug("Replacing attribute for transactional method [" + method + "]: current name '"
                    + name + "' is more specific than '" + regMethodName + "'");
                }
                this.methodNameMap.put(method,name);
                addTransactionalMethod(method,attr);
            }
            else
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("Keeping attribute for transactional method [" + method + "]: current name '"+
                            name + "' is not more specific than '" + regMethodName + "'");
                }
            }
        }
    }

    public void addTransactionalMethod(Method method, TransactionAttribute attr) {
        Assert.notNull(method, "Method must not be null");
        Assert.notNull(attr, "TransactionAttribute must not be null");
        if (logger.isDebugEnabled()) {
            logger.debug("Adding transactional method [" + method + "] with attribute [" + attr + "]");
        }
        this.transactionAttributeMap.put(method, attr);
    }

    /**
     * Return is the given method name matches the mapped name
     * @param methodName
     * @param mappedName
     * @return
     */
    protected boolean isMatch(String methodName, String mappedName)
    {
        return PatternMatchUtils.simpleMatch(mappedName, methodName);
    }

    @Override
    @Nullable
    public TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass) {
        if(this.eagerlyInitialized)
        {
            return this.transactionAttributeMap.get(method);
        }
        else
        {
            synchronized (this.transactionAttributeMap)
            {
                if(!this.initialized)
                {
                    initMethodMap(this.methodMap);
                    this.initialized = true;
                }
                return this.transactionAttributeMap.get(method);
            }
        }
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MethodMapTransactionAttributeSource)) {
            return false;
        }
        MethodMapTransactionAttributeSource otherTas = (MethodMapTransactionAttributeSource) other;
        return ObjectUtils.nullSafeEquals(this.methodMap, otherTas.methodMap);
    }

    @Override
    public int hashCode() {
        return MethodMapTransactionAttributeSource.class.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + this.methodMap;
    }
}
