package org.garry.transaction.support;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple transaction-backed {@link org.springframework.beans.factory.config.Scope} implementation,delegating to
 * {@link TransactionSynchronizationManager}'s resource binding mechanism。
 *
 *
 */
public class SimpleTransactionScope implements Scope {


    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        ScopedObjectsHolder scopedObjects = (ScopedObjectsHolder) TransactionSynchronizationManager.getResource(this);
        if (scopedObjects == null)
        {
           scopedObjects = new ScopedObjectsHolder();
           TransactionSynchronizationManager.registerSynchronization();
        }
        return null;
    }

    @Override
    public Object remove(String name) {
        return null;
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {

    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return null;
    }

    static class ScopedObjectsHolder
    {
        final Map<String,Object> scopedInstances = new HashMap<>();

        final Map<String,Runnable> destructionCallbacks = new LinkedHashMap<>();
    }

//    private class CleanupSynchronization extends
}
