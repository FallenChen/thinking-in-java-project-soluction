package org.garry.transaction.interceptor;

import org.springframework.util.Assert;

/**
 * @ClassName RollbackRuleAttribute
 * @Description Rule determining whether or not a given exception (and any subclass)
 * should cause a rollback
 *
 * @Author cy
 * @Date 2021/4/26 22:50
 */
public class RollbackRuleAttribute {

    public static final RollbackRuleAttribute ROLLBACK_ON_RUNTIME_EXCEPTIONS =
            new RollbackRuleAttribute(RuntimeException.class);

    private final String exceptionName;

    public RollbackRuleAttribute(Class<?> clazz)
    {
        Assert.notNull(clazz,"'clazz' cannot be null");
        if(!Throwable.class.isAssignableFrom(clazz))
        {
            throw new IllegalArgumentException(
                    "Cannot construct rollback rule from [" + clazz.getName() + "]: it's not a Throwable"
            );
        }
        this.exceptionName = clazz.getName();
    }

    public RollbackRuleAttribute(String exceptionName)
    {
        Assert.hasText(exceptionName, "'exceptionName' cannot be null or empty");
        this.exceptionName = exceptionName;
    }

    public String getExceptionName() {
        return exceptionName;
    }

    public int getDepth(Throwable ex)
    {
        return 0;
    }

    private int getDepth(Class<?> exceptionClass, int depth)
    {
        return 0;
    }

    @Override
    public boolean equals(Object other)
    {
        if(this == other)
        {
            return true;
        }

        if(!(other instanceof RollbackRuleAttribute))
        {
            return false;
        }
        RollbackRuleAttribute rhs = (RollbackRuleAttribute) other;
        return this.exceptionName.equals(rhs.exceptionName);
    }

    @Override
    public int hashCode()
    {
        return this.exceptionName.hashCode();
    }

    @Override
    public String toString()
    {
        return "RollbackRuleAttribute with pattern [" + this.exceptionName + "]";
    }
}
