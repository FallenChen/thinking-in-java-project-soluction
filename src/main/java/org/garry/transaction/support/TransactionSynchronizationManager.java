package org.garry.transaction.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;

/**
 * Central delegate that manages resources and transaction synchronizations per thread.
 * To be used by resource management code but not by typical application code.
 *
 * Supports one resource per key without overwriting, that is , a resource needs
 * to be removed before a new one can be set for the same key.
 * Supports a list of transaction synchronizations if synchronization is active.
 *
 * Resource management code should check for thread-bound resources,e.g. JDBC
 * Connections or Hibernate Sessions, via {@code getResource}.Such code is
 * normally not supposed to bind resources to threads, as this is the responsibility
 * of transaction managers.A further option is to lazily bind on first use if
 * transaction synchronization is active, for performing transactions that span
 * an arbitrary number of resources.
 *
 * Transaction synchronization must be activated and deactivated by a transaction
 * manager via {@link #initSynchronization()} and {@link #clearSynchronization()}.
 * This is automatically supported by {@link AbstractTransactionManager},
 * and thus by all standard Spring transaction managers, such as
 * {@link org.springframework.transaction.jta.JtaTransactionManager} and
 * {@link org.springframework.jdbc.datasource.DataSourceTransactionManager}.
 *
 * Resource management code should only register synchronizations when this
 * manager is active, which can be checked via {@link #isSynchronizationActice};
 * it should perform immediate resource cleanup else.If transaction synchronization
 * isn't active, there is either  no current transaction, or the transaction manager
 * doesn't support transaction synchronization
 *
 * Synchronization is for example used to always return the same resources
 * within a JTA transaction, e.g. a JDBC Connection or a Hibernate Session for
 * any given DataSource or SessionFactory, respectively
 */
public abstract class TransactionSynchronizationManager {

    private static final Log logger = LogFactory.getLog(TransactionSynchronizationManager.class);

    private static final ThreadLocal<Map<Object, Object>> resources =
            new NamedThreadLocal<>("Transactional resources");

    private static final ThreadLocal<Set<TransactionSynchronization>> synchronizations =
            new NamedThreadLocal<>("Transaction synchronizations");

    private static final ThreadLocal<String> currentTransactionName =
            new NamedThreadLocal<>("Current transaction name");

    private static final ThreadLocal<Boolean> currentTransactionReadOnly =
            new NamedThreadLocal<>("Current transaction read-only status");

    private static final ThreadLocal<Integer> currentTransactionIsolationLevel =
            new NamedThreadLocal<>("Current transaction isolation level");

    private static final ThreadLocal<Boolean> actualTransactionActive =
            new NamedThreadLocal<>("Actual transaction active");

    // Management of transaction-associated resource handles


    /**
     * Return all resources that are bound to the current thread.
     *
     * @return a Map with resource keys(usually the resource factory) and resource
     * values (usually the active resource object), or an empty Map if there are
     * currently no resource bound
     */
    public static Map<Object, Object> getResourceMap() {
        Map<Object, Object> map = resources.get();
        return (map != null ? Collections.unmodifiableMap(map) : Collections.emptyMap());
    }

    /**
     * Check if there is a resource for the given key bound to the current thread
     *
     * @param key
     * @return
     */
    public static boolean hasResource(Object key) {
        Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
        Object value = doGetResource(actualKey);
        return (value != null);
    }

    /**
     * Retrieve a resource for the given key that is bound to the current thread
     *
     * @param key
     * @return
     */
    @Nullable
    public static Object getResource(Object key) {
        Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
        Object value = doGetResource(actualKey);
        if(value !=null && logger.isTraceEnabled())
        {
            logger.trace("Retrieved vale [" + value + "] for key [" + actualKey + "] bound to thread ["+
                    Thread.currentThread().getName() + "]");
        }
        return value;
    }

    /**
     * Actual check the value of the resource that is bound for the given key
     *
     * @param actualKey
     * @return
     */
    @Nullable
    private static Object doGetResource(Object actualKey) {
        Map<Object, Object> map = resources.get();
        if(map == null)
        {
            return null;
        }
        Object value = map.get(actualKey);
        // Transparently remove ResourceHolder that was marked as void...
        if(value instanceof ResourceHolder && ((ResourceHolder) value).isVoid())
        {
            map.remove(actualKey);
            // Remove entire ThreadLocal if empty...
            if(map.isEmpty())
            {
                resources.remove();
            }
            value = null;
        }
        return value;
    }

    /**
     * Bind the given resource for the given key to the current thread
     *
     * @param key
     * @param value
     * @throws IllegalStateException if there is already a value bound to the thread
     */
    public static void bindResource(Object key, Object value) throws IllegalStateException {
        Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
        Assert.notNull(value, "Value must not be null");
        Map<Object, Object> map = resources.get();
        // set ThreadLocal map if none found
        if(map == null)
        {
            map = new HashMap<>();
            resources.set(map);
        }
        Object oldValue = map.put(actualKey, value);
        // Transparently suppress a ResourceHolder that was marked as void...
        if(oldValue instanceof ResourceHolder && ((ResourceHolder)oldValue).isVoid())
        {
            oldValue = null;
        }
        if(oldValue != null)
        {
            throw new IllegalStateException("Already value [" + oldValue + "] for key ["+
                    actualKey + " ] bound to thread [" + Thread.currentThread().getName() + "]");
        }
        if(logger.isTraceEnabled())
        {
            logger.trace("Bound value [" + value + "] for key [" + actualKey + "] to thread [" +
                    Thread.currentThread().getName() + "]");
        }
    }

    /**
     * Unbind a resource for the given key from the current thread
     *
     * @param key
     * @return
     * @throws IllegalStateException if there is no value bound to the thread
     */
    public static Object unbindResource(Object key) throws IllegalStateException {
        Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
        Object value = doUnbindResource(actualKey);
        if(value == null)
        {
            throw new IllegalStateException(
                    "No value for key [" + actualKey + "] bound to thread [" + Thread.currentThread().getName() + "]"
            );
        }
        return value;
    }

    /**
     * Unbind a resource for the given key from the current thread
     *
     * @param key
     * @return
     */
    public static Object unbindResourceIfPossible(Object key) {
        Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
        return doUnbindResource(actualKey);
    }

    /**
     * Actually remove the value of the resource that is bound for the given key
     *
     * @param actualKey
     * @return
     */
    private static Object doUnbindResource(Object actualKey) {
        Map<Object, Object> map = resources.get();
        if(map == null)
        {
            return null;
        }
        Object value = map.remove(actualKey);
        // Remove entire ThreadLocal if empty...
        if(map.isEmpty())
        {
            resources.remove();
        }
        // Transparently suppress a ResourceHolder that was marked as void...
        if(value instanceof ResourceHolder && ((ResourceHolder)value).isVoid())
        {
            value = null;
        }
        if(value != null && logger.isTraceEnabled())
        {
            logger.trace("Removed value [" + value + "] for key [" + actualKey + "] from thread ["
            + Thread.currentThread().getName() + "]");
        }
        return value;
    }

    // Management of transaction synchronizations

    /**
     * Return if transaction synchronization is active for the current thread.
     * Can be called before register to avoid unnecessary instance creation
     *
     * @return
     */
    public static boolean isSynchronizationActive() {
        return (synchronizations.get() != null);
    }

    /**
     * Activate transaction synchronization for the current thread.
     * Called by a transaction manager on transaction begin
     */
    public static void initSynchronization() {
        if(isSynchronizationActive())
        {
            throw new IllegalStateException("Cannot active transaction synchronization - already active");
        }
        logger.trace("Initializing transaction synchronization");
        synchronizations.set(new LinkedHashSet<>());
    }

    /**
     * Register a new transaction synchronization for the current thread.
     * Typically called by resource management code.
     *
     * @param synchronization
     */
    public static void registerSynchronization(TransactionSynchronization synchronization) {
        Assert.notNull(synchronization, "TransactionSynchronization must not be null");
        if(!isSynchronizationActive())
        {
            throw new IllegalStateException("Transaction synchronization is not active");
        }
        synchronizations.get().add(synchronization);
    }

    /**
     * Return an unmodifiable snapshot list of all registered synchronizations
     * for the current thread
     *
     * @return
     */
    public static List<TransactionSynchronization> getSynchronizations() {
        Set<TransactionSynchronization> synchs = synchronizations.get();
        if(synchs == null)
        {
            throw new IllegalStateException("Transaction synchronization is not active");
        }
        // Return unmodifiable snapshot, to avoid ConcurrentModificationExceptions
        // while iterating and invoking synchronization callbacks that in turn
        // might register further synchronizations
        if(synchs.isEmpty())
        {
            return Collections.emptyList();
        }
        else
        {
            // Sort lazily here, not in registerSynchronization
            List<TransactionSynchronization> sortedSynchs = new ArrayList<>(synchs);
            AnnotationAwareOrderComparator.sort(sortedSynchs);
            return Collections.unmodifiableList(sortedSynchs);
        }
    }

    /**
     * Deactivate transaction synchronization for the current thread.
     * Called by the transaction manager on transaction cleanup.
     */
    public static void clearSynchronization() {
        if(!isSynchronizationActive())
        {
            throw new IllegalStateException("Cannot deactivate transaction synchronization - not active");
        }
        logger.trace("Clearing transaction synchronization");
        synchronizations.remove();
    }

    // Exposure of transaction characteristics

    /**
     * Expose the name of the current transaction, if any.
     * Called by thr transaction manager on transaction begin and on cleanup
     *
     * @param name
     */
    public static void setCurrentTransactionName(@Nullable String name) {
        currentTransactionName.set(name);
    }

    /**
     * Return the name of the current transaction, or {@code null} if none set.
     * To be called by resource management code for optimizations per use case,
     * for example to optimize fetch strategies for specific named transactions.
     *
     * @return
     */
    public static String getCurrentTransactionName() {
        return currentTransactionName.get();
    }

    /**
     * Expose a read-only flag for the current transaction.
     * Called by the transaction manager on transaction begin and on cleanup.
     *
     * @param readOnly
     */
    public static void setCurrentTransactionReadOnly(boolean readOnly) {
        currentTransactionReadOnly.set(readOnly ? Boolean.TRUE : null);
    }

    /**
     * Return whether the current transaction is marked as read-only
     * To be called by resource management code when preparing a newly
     * created resource (for example, a Hibernate Session).
     *
     * @return
     */
    public static boolean isCurrentTransactionReadOnly() {
        return (currentTransactionReadOnly.get() != null);
    }

    /**
     * Expose an isolation level for the current transaction.
     * Called by the transaction manager on transaction begin and on cleanup.
     *
     * @param isolationLevel the isolation level to expose, according to the
     *                       JDBC Connection constants
     */
    public static void setCurrentTransactionIsolationLevel(@Nullable Integer isolationLevel) {
        currentTransactionIsolationLevel.set(isolationLevel);
    }

    /**
     * Return the isolation level for the current transaction, if any.
     * To be called by resource management code when preparing a newly
     * created resource (for example, a JDBC Connection).
     * @return
     */
    @Nullable
    public static Integer getCurrentTransactionIsolationLevel() {
        return currentTransactionIsolationLevel.get();
    }

    /**
     * Expose whether there currently is an actual transaction active.
     * Called by the transaction manager on transaction begin and on cleanup.
     * @param active
     */
    public static void setActualTransactionActive(boolean active)
    {
        actualTransactionActive.set(active ? Boolean.TRUE : null);
    }

    /**
     * Return whether there currently is an actual transaction active.
     * This indicates whether the current thread is associated with an actual
     * transaction rather than just with active transaction synchronization.
     * @return
     */
    public static boolean isActualTransactionActive()
    {
        return (actualTransactionActive.get() != null);
    }

    /**
     * Clear the entire transaction synchronization state for the current thread:
     * registered synchronizations as well as the various transaction characteristics
     */
    public static void clear()
    {
       synchronizations.remove();
       currentTransactionName.remove();
       currentTransactionReadOnly.remove();
       currentTransactionIsolationLevel.remove();
       actualTransactionActive.remove();
    }
}
