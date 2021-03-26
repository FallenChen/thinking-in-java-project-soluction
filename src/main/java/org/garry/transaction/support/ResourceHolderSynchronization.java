package org.garry.transaction.support;

public abstract class ResourceHolderSynchronization<H extends ResourceHolder, K> implements TransactionSynchronization{

    private final H resourceHolder;

    private final K resourceKey;

    private volatile boolean holderActive = true;

    /**
     * Create a new ResourceHolderSynchronization for the given holder
     * @param resourceHolder the ResourceHolder to manager
     * @param resourceKey the key to bind the ResourceHolder for
     */
    public ResourceHolderSynchronization(H resourceHolder, K resourceKey) {
        this.resourceHolder = resourceHolder;
        this.resourceKey = resourceKey;
    }

    @Override
    public void suspend() {
        if (this.holderActive)
        {

        }
    }
}
