package org.garry.transaction.interceptor;

/**
 * @ClassName NoRollbackRuleAttribute
 * @Description Tag subclass {@link RollbackRuleAttribute} that has the opposite behavior
 * to the {@code RollbackRuleAttribute} superclass
 * @Author cy
 * @Date 2021/4/26 22:49
 */
public class NoRollbackRuleAttribute extends RollbackRuleAttribute{

    public NoRollbackRuleAttribute(Class<?> clazz)
    {
        super(clazz);
    }

    public NoRollbackRuleAttribute(String exceptionName)
    {
        super(exceptionName);
    }

    @Override
    public String toString() {

        return "No" + super.toString();
    }
}
