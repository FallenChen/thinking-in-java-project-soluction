package org.garry.transaction.support;

import org.garry.transaction.TransactionStatus;
import org.springframework.lang.Nullable;

@FunctionalInterface
public interface TransactionCallback<T>{

    @Nullable
    T doInTransaction(TransactionStatus status);
}
