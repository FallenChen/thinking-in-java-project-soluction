package org.garry.transaction.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.garry.transaction.PlatformTransactionManager;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Properties;

public class TransactionInterceptor extends TransactionAspectSupport implements MethodInterceptor, Serializable {

    public TransactionInterceptor() {
    }

    public TransactionInterceptor(PlatformTransactionManager ptm, Properties attributes)
    {
        setTransactionManager(ptm);
        setTransactionAttributes(attributes);
    }

    public TransactionInterceptor(PlatformTransactionManager ptm, TransactionAttributeSource tas) {
        setTransactionManager(ptm);
        setTransactionAttributeSource(tas);
    }

    @Nullable
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        // Work out the target class: may be {@code null}
        // The TransactionAttributeSource should be passed the target class
        // as well as the method, which may be from an interface
        Class<?> targetClass = invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null;

        // Adapt to TransactionAspectSupport's invokeWithinTransaction...
        return invokeWithinTransaction(invocation.getMethod(), targetClass, invocation::proceed);
    }

    //-----------------------------------------
    // Serialization support
    //-----------------------------------------

    private void writeObject(ObjectOutputStream oos) throws IOException {
        // Rely on default serialization, although this class itself doesn't carry state anyway...
        oos.defaultWriteObject();

        // Deserialize superclass fields
        oos.writeObject(getTransactionManagerBeanName());
        oos.writeObject(getTransactionManager());
        oos.writeObject(getTransactionAttributeSource());
        oos.writeObject(getBeanFactory());
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // Rely on default serialization, although this class itself doesn't carry state anyway
        ois.defaultReadObject();

        // Serialize all relevant superclass fields
        // Superclass can't implement Serializable because it also serves as base class
        // for AspectJ aspects (which are not allowed to implement Serializable)
        setTransactionManagerBeanName((String) ois.readObject());
        setTransactionManager((PlatformTransactionManager) ois.readObject());
        setTransactionAttributeSource((TransactionAttributeSource) ois.readObject());
        setBeanFactory((BeanFactory) ois.readObject());
    }
}
