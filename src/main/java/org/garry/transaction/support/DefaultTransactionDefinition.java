package org.garry.transaction.support;

import org.garry.transaction.TransactionDefinition;
import org.springframework.core.Constants;
import org.springframework.lang.Nullable;

import java.io.Serializable;

/**
 * Default implementation of the {@link TransactionDefinition} interface,
 * offering bean-style configuration and sensible default values
 * (PROPAGATION_REQUIRED, ISOLATION_DEFAULT, TIMEOUT_DEFAULT, readOnly = false)
 */
public class DefaultTransactionDefinition implements TransactionDefinition, Serializable {

    // prefix for the propagation constants
    public static final String PREFIX_PROPAGATION = "PROPAGATION_";
    // prefix for the isolation constants
    public static final String PREFIX_ISOLATION = "ISOLATION_";
    // prefix for transaction timeout values
    public static final String PREFIX_TIMEOUT = "timeout_";
    // marker for read-only transactions
    public static final String READ_ONLY_MARKER = "readOnly";

    // Constants instance for TransactionDefinition
    static final Constants constants = new Constants(TransactionDefinition.class);

    private int propagationBehavior = PROPAGATION_REQUIRED;

    private int isolationLevel = ISOLATION_DEFAULT;

    private int timeout = TIMEOUT_DEFAULT;

    private boolean readOnly = true;


    @Nullable
    private String name;

    public DefaultTransactionDefinition() {
    }

    public DefaultTransactionDefinition(TransactionDefinition other)
    {
        this.propagationBehavior = other.getPropagationBehavior();
        this.isolationLevel = other.getIsolationLevel();
        this.timeout = other.getTimeout();
        this.readOnly = other.isReadOnly();
        this.name = other.getName();
    }

    public DefaultTransactionDefinition(int propagationBehavior)
    {
        this.propagationBehavior = propagationBehavior;
    }

    /**
     * Set the propagation behavior by the name of the corresponding constant in
     * TransactionDefinition
     * @param constantName
     */
    public final void setPropagationBehaviorName(String constantName)
    {
        if(!constantName.startsWith(PREFIX_PROPAGATION))
        {
            throw new IllegalArgumentException("Only propagation constants allowed");
        }
        setPropagationBehavior(constants.asNumber(constantName).intValue());
    }

    /**
     * Set the propagation behavior. Must be one of the propagation constants
     * in the TransactionDefinition interface.
     * @param propagationBehavior
     */
    public final void setPropagationBehavior(int propagationBehavior)
    {
        if(!constants.getValues(PREFIX_PROPAGATION).contains(propagationBehavior))
        {
            throw new IllegalArgumentException("Only values of propagation constants allowed");
        }
        this.propagationBehavior = propagationBehavior;
    }

    @Override
    public int getIsolationLevel() {
        return this.isolationLevel;
    }

    /**
     * Set the isolation level by the name of the corresponding constant in
     * TransactionDefinition
     * @param constantName
     */
    public final void setIsolationLevelName(String constantName)
    {
        if(!constantName.startsWith(PREFIX_ISOLATION))
        {
            throw new IllegalArgumentException("Only isolation constants allowed");
        }
        setIsolationLevel(constants.asNumber(constantName).intValue());
    }

    /**
     * Set the isolation level. Must be one of the isolation constants
     * in the TransactionDefinition interface.
     * @param isolationLevel
     */
    public final void setIsolationLevel(int isolationLevel)
    {
        if(!constants.getValues(PREFIX_ISOLATION).contains(isolationLevel))
        {
            throw new IllegalArgumentException("Only values of isolation constants allowed");
        }
        this.isolationLevel = isolationLevel;
    }

    @Override
    public int getTimeout() {
        return 0;
    }

    /**
     * Set the timeout to apply, as number of seconds
     * @param timeout
     */
    public final void setTimeout(int timeout)
    {
        if(timeout < TIMEOUT_DEFAULT)
        {
            throw new IllegalArgumentException("Timeout must be a positive integer or TIMEOUT_DEFAULT");
        }
        this.timeout = timeout;
    }

    /**
     * Set whether to optimize as read-only transaction
     * @param readOnly
     */
    public final void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    @Nullable
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getPropagationBehavior() {
        return this.propagationBehavior;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof TransactionDefinition && toString().equals(other.toString()));
    }


    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return getDefinitionDescription().toString();
    }

    protected final StringBuilder getDefinitionDescription()
    {
       StringBuilder result = new StringBuilder();
       result.append(constants.toCode(this.propagationBehavior, PREFIX_PROPAGATION));
       result.append(',');
       result.append(constants.toCode(this.isolationLevel, PREFIX_ISOLATION));
       if (this.timeout != TIMEOUT_DEFAULT) {
           result.append(',');
           result.append(PREFIX_TIMEOUT).append(this.timeout);
       }
       if (this.readOnly) {
           result.append(',');
           result.append(READ_ONLY_MARKER);
       }
       return result;
    }
}
