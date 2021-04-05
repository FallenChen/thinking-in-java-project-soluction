package org.garry.transaction.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.garry.transaction.*;
import org.springframework.core.Constants;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.List;

/**
 * Abstract base class that implements Spring's standard transaction workflow,
 * serving as basis for concrete platform transaction managers
 *
 * This base class provides the flowing workflow handling:
 *
 * determines if there is an existing transaction;
 * applies the appropriate propagation behavior;
 * suspends and resumes transactions if necessary;
 * checks the rollback-only flag on commit;
 * applies the appropriate modification on rollback
 * (actual rollback or setting rollback-only);
 * triggers registered synchronization callbacks
 * (if transaction synchronization is active)
 *
 * Subclasses have to implement specific template methods for specific
 * states of a transaction, e.g.: begin, suspend, resume, commit, rollback.
 * The most important of them are abstract and must be provided by a concrete
 * implementation; for the rest, defaults are provided,so overriding is optional.
 *
 * Transaction synchronization is a generic mechanism for registering callbacks
 * that get invoked at transaction completion time.This is mainly used internally
 * by the data access support classes for JDBC, Hibernate, JPA, etc when running
 * within a JTA transaction: They register resources that are opened within the
 * transaction for closing at transaction completion time, allowing e.g. for reuse
 * of the same Hibernate Session within the transaction. The same mechanism can
 * also be leveraged for custom synchronization needs in an application.
 *
 * The state od this class is serializable, to allow for serializing the
 * transaction strategy along with proxies that carry a transaction interceptor.
 * It is up to subclasses if they wish to make their state to be serializable too.
 * They should implement the {@code java.io.Serializable}marker interface in
 * that case, and potentially a private {@code readObject()} method (according
 * to Java serialization rules) if they need to restore any transient state.
 */
public abstract class AbstractPlatformTransactionManager implements PlatformTransactionManager, Serializable {

    public static final int SYNCHRONIZATION_ALWAYS = 0;

    public static final int SYNCHRONIZATION_ON_ACTUAL_TRANSACTION = 1;

    public static final int SYNCHRONIZATION_NEVER = 2;

    protected transient Log logger = LogFactory.getLog(getClass());

    private int transactionSynchronization = SYNCHRONIZATION_ALWAYS;

    private int defaultTimeout = TransactionDefinition.TIMEOUT_DEFAULT;

    /**
     * Return if this transaction manager should active the thread-bound
     * transaction synchronization support
     * @return
     */
    public int getTransactionSynchronization() {
        return transactionSynchronization;
    }

    /**
     * Set when this transaction manager should active the thread-bound
     * transaction synchronization support.Default is "always".
     * Note that transaction synchronization isn't supported for
     * multiple concurrent transactions by different transaction managers.
     * Only one transaction manager is allowed to active it at any time.
     * @param transactionSynchronization
     */
    public void setTransactionSynchronization(int transactionSynchronization) {
        this.transactionSynchronization = transactionSynchronization;
    }

    // Implementation of PlatformTransactionManager

    /**
     * This implementation handles propagation behavior. Delegates to
     * {@code doGetTransaction}, {@code isExistingTransaction}
     * @param definition
     * @return
     * @throws TransactionException
     */
    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        Object transaction = doGetTransaction();

        // Cache debug flag to avoid repeated checks
        boolean debugEnabled = logger.isDebugEnabled();

        if(definition == null)
        {
            // Use defaults if no transaction definition given
            definition = new DefaultTransactionDefinition();
        }

        if(isExistingTransaction(transaction))
        {
            // Existing transaction found -> check propagation behavior to find out how to behave
            return handleExistingTransaction(definition,transaction,debugEnabled);
        }

        // Check definition settings for new transaction
        if(definition.getTimeout() < TransactionDefinition.TIMEOUT_DEFAULT)
        {
            throw new InvalidTimeoutException("Invalid transaction timeout",definition.getTimeout());
        }

        // No existing transaction found -> check propagation behavior to find out how to proceed
        if(definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_MANDATORY)
        {
            throw new IllegalTransactionStateException(
                    "No existing transaction found for transaction marked with propagation 'mandatory'");
        }
        else if(definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED ||
                definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW ||
                definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED)
        {
            SuspendedResourcesHolder suspnededResources = suspend(null);
            if(debugEnabled)
            {
                logger.debug("Creating new transaction with name [" + definition.getName() + "]: " + definition);
            }
            try
            {
                boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
                DefaultTransactionStatus status = newTransactionStatus(
                        definition,transaction,true,newSynchronization,debugEnabled,suspnededResources);
                doBegin(transaction,definition);
                prepareSynchronization(status,definition);
                return status;
            }
            catch (RuntimeException | Error ex)
            {
                resume(null,suspnededResources);
                throw ex;
            }
        }
        else
        {
            // Create "empty" transaction: no actual transaction, but potentially synchronization
            if(definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT && logger.isWarnEnabled())
            {
                logger.warn("Custom isolation level specified but no actual transaction initiated: " +
                        "isolation level will effectively be ignored: " + definition);
            }
            boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);
            return prepareTransactionStatus(definition,null,true,newSynchronization,debugEnabled,null);
        }

    }

    /**
     * Create a TransactionStatus for an existing transaction
     * @param definition
     * @param transaction
     * @param debugEnabled
     * @return
     * @throws TransactionException
     */
    private TransactionStatus handleExistingTransaction(
            TransactionDefinition definition, Object transaction, boolean debugEnabled) throws TransactionException
    {
        if(definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NEVER)
        {
            throw new IllegalTransactionStateException(
                    "Existing transaction found for transaction marked with propagation 'never'");
        }

        if(definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NOT_SUPPORTED)
        {
            if(debugEnabled)
            {
                logger.debug("Suspending current transaction");
            }
            Object suspendedResources = suspend(transaction);
            boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);
            return prepareTransactionStatus(
                    definition,null,false,newSynchronization,debugEnabled,suspendedResources);
        }

        if(definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW)
        {
            if(debugEnabled)
            {
                logger.debug("Suspending current transaction, creating new transaction with name [" +
                        definition.getName() + "]");
            }
            SuspendedResourcesHolder suspendedResources = suspend(transaction);
            try
            {
                boolean newSynchronization = (getTransactionSynchronization() !=SYNCHRONIZATION_NEVER);
                DefaultTransactionStatus status = newTransactionStatus(
                        definition, transaction, true, newSynchronization, debugEnabled, suspendedResources
                );
                doBegin(transaction,definition);
                prepareSynchronization(status,definition);
                return status;
            }
            catch (RuntimeException | Error beginEx)
            {
                resumeAfterBeginException(transaction,suspendedResources,beginEx);
                throw beginEx;
            }
        }

        if(definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED)
        {
            if(!isNestedTransactionAllowed())
            {
                throw new NestedTransactionNotSupportedException(
                        "Transaction manager does not allow nested transactions by default - " +
                                "specify 'nestedTransactionAllowed' property with value 'true'"
                );
                if(debugEnabled)
                {
                    logger.debug("Creating nested transaction with name [" + definition.getName() + "]");
                }
                if(useSavepointForNestedTransaction())
                {
                    // Create savepoint within existing Spring-managed transaction,
                    // through the SavepointManager API implemented by TransactionStatus.
                    // Usually uses JDBC 3.0 savepoints. Never activates Spring synchronization
                    DefaultTransactionStatus status =
                            prepareTransactionStatus(definition, transaction, false, false, debugEnabled, null);
                    status.createAndHoldSavepoint();
                    return status;
                }
                else
                {
                    // Nested transaction through nested begin and commit/rollback calls.
                    // Usually only for JTA
                   boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
                   DefaultTransactionStatus status =
                           newTransactionStatus(definition,transaction,true,newSynchronization,debugEnabled,null);
                   doBegin(transaction,definition);
                   prepareSynchronization(status,definition);
                   return status;
                }
            }

            // Assumably PROPAGATION_SUPPORTS or PROPAGATION_REQUIRED
            if(debugEnabled)
            {
                logger.debug("Participating in existing transaction");
            }
            if(isValidateExistingTransaction())
            {
                if(definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT)
                {
                    Integer currentIsolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
                    if(currentIsolationLevel == null || currentIsolationLevel != definition.getIsolationLevel())
                    {
                        Constants isoConstants = DefaultTransactionDefinition.constants;
                        throw new IllegalTransactionStateException("Participating transaction with definition [" +
                                definition + "] specifies isolation level which is incompatible with existing transaction: " +
                                (currentIsolationLevel != null ?
                                        isoConstants.toCode(currentIsolationLevel,DefaultTransactionDefinition.PREFIX_PROPAGATION):
                                        "(unknown)"));
                    }
                }
                if(!definition.isReadOnly())
                {
                    if(TransactionSynchronizationManager.isCurrentTransactionReadOnly())
                    {
                        throw new IllegalTransactionStateException("Participating transaction with definition [" +
                                definition + "] is not marked as read-only but existing transaction is");

                    }
                }
            }
            boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
            return prepareTransactionStatus(definition,transaction,false,newSynchronization,debugEnabled,null);
        }

       return null;
    }

    /**
     * Create a new TransactionStatus for the given arguments,
     * also initializing transaction synchronization as appropriate
     * @param definition
     * @param transaction
     * @param newTransaction
     * @param newSynchronization
     * @param debug
     * @param suspendedResources
     * @return
     */
    protected final DefaultTransactionStatus prepareTransactionStatus(
            TransactionDefinition definition, @Nullable Object transaction, boolean newTransaction,
            boolean newSynchronization, boolean debug, @Nullable Object suspendedResources)
    {
        return null;
    }

    /**
     * Create a TransactionStatus instance for the given arguments
     * @param definition
     * @param transaction
     * @param newTransaction
     * @param newSynchronization
     * @param debug
     * @param suspendedResources
     * @return
     */
    protected DefaultTransactionStatus newTransactionStatus(
            TransactionDefinition definition, @Nullable Object transaction, boolean newTransaction,
            boolean newSynchronization, boolean debug, @Nullable Object suspendedResources)
    {
       return null;
    }

    /**
     * Initialize transaction synchronization as appropriate
     */
    protected void prepareSynchronization(DefaultTransactionStatus status, TransactionDefinition definition)
    {

    }

    /**
     * Determine the actual timeout to use for the given definition.
     * Will fall back to this manager's default timeout if the
     * transaction definition doesn't specify a non-default value.
     * @param definition
     * @return
     */
    protected int determineTimeout(TransactionDefinition definition)
    {
        return 0;
    }

    /**
     * Suspend the given transaction.Suspends transaction synchronization first,
     * then delegates to the {@code doSuspend} template method.
     * @param transaction
     * @return
     */
    protected final SuspendedResourcesHolder suspend(@Nullable Object transaction)
    {
        return null;
    }

    /**
     * Resume the given transaction.Delegates to the {@code doResume}
     * template method first, then resuming transaction synchronization.
     * @param transaction
     * @param resourcesHolder
     */
    protected final void resume(@Nullable Object transaction, @Nullable SuspendedResourcesHolder resourcesHolder)
    {

    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {

    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {

    }

    protected final void triggerBeforeCommit(DefaultTransactionStatus status)
    {

    }

    protected final void triggerBeforeCompletion(DefaultTransactionStatus status)
    {

    }

    /**
     * Actually invoke the {@code afterCompletion} methods of the
     * given Spring TransactionSynchronization objects.
     * @param synchronizations
     * @param completionStatus
     */
    protected final void invokeAfterCompletion(List<TransactionSynchronization> synchronizations, int completionStatus)
    {

    }

    // Template methods to be implemented in subclass

    /**
     * Return a transaction object for the current transaction state.
     * The returned object will usually be specific to the concrete transaction
     * manager implementation, carrying corresponding transaction state in a
     * modifiable fashion.This object will be passed into the other template
     * methods,either directly or as part of a
     * DefaultTransactionStatus instance.
     * The returned object should contain information about any existing
     * transaction, that is, a transaction that has already started before the
     * current {@code getTransaction} call on the transaction manager.
     * Consequently, a {@code doGetTransaction} implementation will usually
     * look for an existing transaction and store corresponding state in the
     * returned transaction object.
     * @return
     * @throws TransactionException
     */
    protected abstract Object doGetTransaction() throws TransactionException;

    /**
     * Check if the given transaction object indicates an existing transaction
     * (that is, a transaction which has already started)
     * The result will evaluated according to the specified propagation
     * behavior for the new transaction. An existing transaction might get
     * suspended (in case of PROPAGATION_REQUIRES_NEW),or the new transaction
     * might participate in the existing one (in case of PROPAGATION_REQUIRED).
     * The default implementation returns {@code false},assuming that
     * participating in existing transactions is generally not support.
     * @param transaction
     * @return
     * @throws TransactionException
     */
    protected boolean isExistingTransaction(Object transaction) throws TransactionException
    {
        return false;
    }

    /**
     * Return whether to use a savepoint for a nested transaction
     */
    protected boolean useSavepointForNestedTransaction()
    {
        return true;
    }

    /**
     * Suspend the resource of the current transaction.
     * Transaction synchronization will already haven been suspended.
     * @param transaction
     * @return
     * @throws TransactionException
     */
    protected Object doSuspend(Object transaction)throws TransactionException
    {
        return null;
    }

    /**
     * Resume the resources of the current transaction.
     * Transaction synchronization will be resumed afterwards.
     * @param transaction
     * @param suspendedResources
     * @throws TransactionException
     */
    protected void doResume(@Nullable Object transaction, Object suspendedResources) throws TransactionException
    {

    }

    /**
     * Return whether to call {@code doCommit} on a transaction that has been
     * marked as rollback-only in a global fashion.
     * @return
     */
    protected boolean shouldCommitOnGlobalRollbackOnly()
    {
        return false;
    }

    /**
     * Make preparations for commit, to be performed before the
     * {@code beforeCommit} synchronization callbacks occur.
     * @param status
     */
    protected void prepareForCommit(DefaultTransactionStatus status)
    {

    }

    /**
     * Perform an actual commit of the given transaction
     * @param status
     * @throws TransactionException
     */
    protected abstract void doCommit(DefaultTransactionStatus status) throws TransactionException;

    /**
     * Perform an actual rollback of the given transaction
     * @param status
     * @throws TransactionException
     */
    protected abstract void doRollback(DefaultTransactionStatus status) throws TransactionException;

    /**
     * Set the given transaction rollback-only. Only called on rollback
     * if the current transaction participated in an existing one.
     * @param status
     * @throws TransactionException
     */
    protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException
    {

    }

    /**
     * Register the given list of transaction synchronizations with the existing transaction.
     * @param transaction
     * @param synchronizations
     * @throws TransactionException
     */
    protected void registerAfterCompletionWithExistingTransaction(Object transaction, List<TransactionSynchronization> synchronizations)
        throws TransactionException
    {

    }

    /**
     * Cleanup resources after transaction completion.
     * @param transaction
     */
    protected void doCleanupAfterCompletion(Object transaction)
    {

    }

    /**
     * Begin a new transaction with semantics according to the given transaction
     * definition. Does not have to care about applying the propagation behavior,
     * as this has already been handled by this abstract manager.
     * @param transaction
     * @param definition
     * @throws TransactionException
     */
    protected abstract void doBegin(Object transaction, TransactionDefinition definition)throws TransactionException;

    protected static class SuspendedResourcesHolder
    {
        @Nullable
        private final Object suspendedResources;

        @Nullable
        private List<TransactionSynchronization> suspendedSynchronizations;

        @Nullable
        private String name;

        private boolean readOnly;

        @Nullable
        private Integer isolationLevel;

        private boolean wasActive;

        private SuspendedResourcesHolder(Object suspendedResources)
        {
            this.suspendedResources = suspendedResources;
        }

        private SuspendedResourcesHolder(
                @Nullable Object suspendedResources, List<TransactionSynchronization> suspendedSynchronizations,
                @Nullable String name, boolean readOnly, @Nullable Integer isolationLevel, boolean wasActive)
        {
            this.suspendedResources = suspendedResources;
            this.suspendedSynchronizations = suspendedSynchronizations;
            this.name = name;
            this.readOnly = readOnly;
            this.isolationLevel = isolationLevel;
            this.wasActive = wasActive;
        }
    }
}
