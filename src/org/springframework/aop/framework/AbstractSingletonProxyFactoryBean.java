package org.springframework.aop.framework;

import org.springframework.beans.factory.FactoryBean;

/**
 * Convenient proxy factory bean superclass for proxy factory
 * beans that create only singletons. Manages pre and post
 * interceptors--references, rather than interceptorNames,
 * as in ProxyFactoryBean--and provides consistent interface management.
 * TODO make this the parent of TransactionProxyFactoryBean
 * @since 1.3
 * @author Rod Johnson
 */
public abstract class AbstractSingletonProxyFactoryBean extends ProxyConfig implements FactoryBean {

	public final boolean isSingleton() {
		return true;
	}

}
