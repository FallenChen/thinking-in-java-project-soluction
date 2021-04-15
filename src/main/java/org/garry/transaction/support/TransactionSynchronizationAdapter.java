package org.garry.transaction.support;

import org.springframework.core.Ordered;

/**
 * Simple {@link TransactionSynchronization} adapter containing empty
 * method implementations, for easier overriding of single methods.
 *
 * Also implements the {@link Ordered} interface to enable the execution
 * order of synchronizations to be controlled declaratively. The default
 * {@link #getOrder() order} is {@link Ordered#LOWEST_PRECEDENCE}, indicating
 * late execution; return a lower value for earlier execution.
 */
public abstract class TransactionSynchronizationAdapter implements TransactionSynchronization, Ordered {

    @Override
    public void suspend() {
        TransactionSynchronization.super.suspend();
    }

    @Override
    public void resume() {
        TransactionSynchronization.super.resume();
    }

    @Override
    public void flush() {
        TransactionSynchronization.super.flush();
    }

    @Override
    public void beforeCommit(boolean readOnly) {
        TransactionSynchronization.super.beforeCommit(readOnly);
    }

    @Override
    public void beforeCompletion() {
        TransactionSynchronization.super.beforeCompletion();
    }

    @Override
    public void afterCommit() {
        TransactionSynchronization.super.afterCommit();
    }

    @Override
    public void afterCompletion(int status) {
        TransactionSynchronization.super.afterCompletion(status);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
