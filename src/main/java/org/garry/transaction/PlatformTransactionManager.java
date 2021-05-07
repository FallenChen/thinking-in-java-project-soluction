package org.garry.transaction;

import org.springframework.lang.Nullable;

/**
 * This is the central interface in Spring's imperative transaction infrastructure.
 * Applications can use this directly, but it is not primarily meant as an API:
 * Typically, applications will work with either TransactionTemplate or
 * declarative transaction demarcation through AOP
 */
public interface PlatformTransactionManager {

    TransactionStatus getTransaction(@Nullable TransactionDefinition definition) throws TransactionException;

    void commit(TransactionStatus status) throws TransactionException;

    void rollback(TransactionStatus status) throws TransactionException;
}
