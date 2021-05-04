package org.garry.transaction.interceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @ClassName NameMatchTransactionAttributeSource
 * @Description TODO
 * @Author cy
 * @Date 2021/5/4 21:17
 */
public class NameMatchTransactionAttributeSource implements TransactionAttributeSource, Serializable {

    protected static final Log logger = LogFactory.getLog(NameMatchTransactionAttributeSource.class);

    /**
     * Keys are method names; values are TransactionAttributes
     */
    private Map<String,TransactionAttribute> nameMap = new HashMap<>();

    public void setNameMap(Map<String, TransactionAttribute> nameMap) {
       nameMap.forEach(this::addTransactionalMethod);
    }

    public void setProperties(Properties transactionAttributes)
    {

    }

    public void addTransactionalMethod(String methodName, TransactionAttribute attr)
    {

    }

    @Override
    public TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass) {
        return null;
    }
}
