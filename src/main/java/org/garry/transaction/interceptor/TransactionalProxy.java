package org.garry.transaction.interceptor;

import org.springframework.aop.SpringProxy;

/**
 * @ClassName TransactionalProxy
 * @Description A marker interface for manually created transactional proxies
 * @Author cy
 * @Date 2021/4/21 19:55
 */
public interface TransactionalProxy extends SpringProxy {
}
