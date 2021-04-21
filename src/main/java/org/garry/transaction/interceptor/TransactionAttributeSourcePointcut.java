package org.garry.transaction.interceptor;

import org.springframework.aop.support.StaticMethodMatcherPointcut;

import java.io.Serializable;

/**
 * @ClassName TransactionAttributeSourcePointcut
 * @Description Inner class that implements a Pointcut that matches if the underlying
 * {@link TransactionAttribute} has an attribute for a given method
 * @Author cy
 * @Date 2021/4/21 19:59
 */
abstract class TransactionAttributeSourcePointcut extends StaticMethodMatcherPointcut implements Serializable {
}
