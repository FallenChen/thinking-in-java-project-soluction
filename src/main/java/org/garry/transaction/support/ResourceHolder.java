package org.garry.transaction.support;

/**
 * Generic interface to be implemented by resource holders
 * Allows Spring's transaction infrastructure to introspect
 * and reset the holder when necessary
 */
public interface ResourceHolder {

    /**
     * Reset the transactional state of this holder
     */
    void reset();

    /**
     * Notify this holder that it has been unbound from transaction synchronization
     */
    void unbound();

    /**
     * Determine whether this holder is considered as 'void'
     * i.e as a leftover from a previous thread
     */
    boolean isVoid();
}
