package org.garry.transaction.interceptor;

import org.garry.transaction.support.DefaultTransactionDefinition;
import org.springframework.lang.Nullable;

/**
 * @ClassName DefaultTransactionAttribute
 * @Description
 * @Author cy
 * @Date 2021/4/23 20:13
 */
public class DefaultTransactionAttribute extends DefaultTransactionDefinition implements TransactionAttribute {

    @Nullable
    private String qualifier;

    @Nullable
    private String descriptor;

    public DefaultTransactionAttribute()
    {
        super();
    }

    public DefaultTransactionAttribute(TransactionAttribute other)
    {
        super(other);
    }

    public DefaultTransactionAttribute(int propagationBehavior)
    {
        super(propagationBehavior);
    }

    @Override
    @Nullable
    public String getQualifier() {
        return this.qualifier;
    }

    public void setDescriptor(@Nullable String descriptor) {
        this.descriptor = descriptor;
    }

    @Nullable
    public String getDescriptor() {
        return this.descriptor;
    }

    @Override
    public boolean rollbackOn(Throwable ex) {
       return (ex instanceof RuntimeException || ex instanceof Error);
    }

    protected final StringBuilder getAttributeDescription()
    {
        StringBuilder result = getDefinitionDescription();
        if(this.qualifier != null)
        {
            result.append("; '").append(this.qualifier).append("'");
        }
        return result;
    }
}
