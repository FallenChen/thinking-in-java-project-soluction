package org.garry.transaction.support;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.lang.Nullable;

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
           TransactionSynchronizationManager.registerSynchronization(new CleanupSynchronization(scopedObjects));
           TransactionSynchronizationManager.bindResource(this,scopedObjects);
        }
        Object scopedObject = scopedObjects.scopedInstances.get(name);
        if(scopedObject == null)
        {
            scopedObject = objectFactory.getObject();
            scopedObjects.scopedInstances.put(name,scopedObject);
        }
        return scopedObject;
    }

    @Override
    @Nullable
    public Object remove(String name) {
        ScopedObjectsHolder scopedObjects= (ScopedObjectsHolder)TransactionSynchronizationManager.getResource(this);
        if(scopedObjects != null)
        {
            scopedObjects.destructionCallbacks.remove(name);
            return scopedObjects.scopedInstances.remove(name);
        }
        else
        {
            return null;
        }
    }

    @Override
    @Nullable
    public void registerDestructionCallback(String name, Runnable callback) {
        ScopedObjectsHolder scopedObjects = (ScopedObjectsHolder) TransactionSynchronizationManager.getResource(this);
        if(scopedObjects != null)
        {
            scopedObjects.destructionCallbacks.put(name,callback);
        }
    }

    @Override
    @Nullable
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    @Nullable
    public String getConversationId() {
        return TransactionSynchronizationManager.getCurrentTransactionName();
    }

    static class ScopedObjectsHolder
    {
        final Map<String,Object> scopedInstances = new HashMap<>();

        final Map<String,Runnable> destructionCallbacks = new LinkedHashMap<>();
    }

    private class CleanupSynchronization extends TransactionSynchronizationAdapter
    {
        private final ScopedObjectsHolder scopedObjects;

        public CleanupSynchronization(ScopedObjectsHolder scopedObjects) {
            this.scopedObjects = scopedObjects;
        }

        @Override
        public void suspend() {
           TransactionSynchronizationManager.unbindResource(SimpleTransactionScope.this);
        }

        @Override
        public void resume() {
           TransactionSynchronizationManager.bindResource(SimpleTransactionScope.this,this.scopedObjects);
        }

        @Override
        public void afterCompletion(int status) {
           TransactionSynchronizationManager.unbindResourceIfPossible(SimpleTransactionScope.this);
           for(Runnable callback: this.scopedObjects.destructionCallbacks.values())
           {
               callback.run();
           }
           this.scopedObjects.destructionCallbacks.clear();
           this.scopedObjects.scopedInstances.clear();
        }
    }
}
