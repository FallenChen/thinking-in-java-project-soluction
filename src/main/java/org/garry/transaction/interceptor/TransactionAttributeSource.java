package org.garry.transaction.interceptor;

import org.springframework.lang.Nullable;

import java.lang.reflect.Method;

public interface TransactionAttributeSource {

    @Nullable
    TransactionAttribute getTransactionAttribute(Method method, @Nullable Class<?> targetClass);
}
