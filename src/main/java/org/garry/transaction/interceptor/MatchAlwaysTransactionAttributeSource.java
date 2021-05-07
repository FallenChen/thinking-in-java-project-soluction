package org.garry.transaction.interceptor;

import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @ClassName MatchAlwaysTransactionAttributeSource
 * @Description Very simple implementation of TransactionAttributeSource which will always
 * return the same TransactionAttribute for all methods fed to it.The TransactionAttribute
 * may be specified, but will otherwise default to PROPAGATION_REQUIRED.This may be
 * used in the cases where you want to use the same transaction attribute with all
 * methods being handled by a transaction interceptor
 * @Author cy
 * @Date 2021/5/7 15:39
 */
public class MatchAlwaysTransactionAttributeSource implements TransactionAttributeSource, Serializable {

    private TransactionAttribute transactionAttribute = new DefaultTransactionAttribute();

    /**
     * Allows a transaction attribute to be specified, using the String form,
     * for example, "PROPAGATION_REQUIRED"
     * @param transactionAttribute
     */
    public void setTransactionAttribute(TransactionAttribute transactionAttribute) {
        this.transactionAttribute = transactionAttribute;
    }

    @Override
    @Nullable
    public TransactionAttribute getTransactionAttribute(Method method, @Nullable Class<?> targetClass) {
        return (ClassUtils.isUserLevelMethod(method) ? this.transactionAttribute : null);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MatchAlwaysTransactionAttributeSource)) {
            return false;
        }
        MatchAlwaysTransactionAttributeSource otherTas = (MatchAlwaysTransactionAttributeSource) other;
        return ObjectUtils.nullSafeEquals(this.transactionAttribute, otherTas.transactionAttribute);
    }

    @Override
    public int hashCode() {
        return MatchAlwaysTransactionAttributeSource.class.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + this.transactionAttribute;
    }
}
