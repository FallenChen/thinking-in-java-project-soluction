package org.garry.transaction.interceptor;

import org.garry.transaction.TransactionDefinition;
import org.springframework.lang.Nullable;

/**
 *
 */
public interface TransactionAttribute extends TransactionDefinition {

    @Nullable
    String getQualifier();

    boolean rollbackOn(Throwable ex);
}
