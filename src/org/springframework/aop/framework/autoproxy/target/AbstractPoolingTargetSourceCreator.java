/*
 * The Spring Framework is published under the terms of the Apache Software License.
 */

package org.springframework.aop.framework.autoproxy.target;

import org.springframework.aop.target.AbstractPoolingTargetSource;
import org.springframework.aop.target.AbstractPrototypeTargetSource;
import org.springframework.aop.target.CommonsPoolTargetSource;
import org.springframework.beans.factory.BeanFactory;

/**
 * Convenient superclass for TargetSource creators that create pooling TargetSources.
 * @author Rod Johnson
 * @version $Id: AbstractPoolingTargetSourceCreator.java,v 1.3 2003-12-30 01:07:11 jhoeller Exp $
 */
public abstract class AbstractPoolingTargetSourceCreator extends AbstractPrototypeTargetSourceCreator {

	protected final AbstractPrototypeTargetSource createPrototypeTargetSource(Object bean, String beanName, BeanFactory factory) {
		PoolingAttribute poolingAttribute = getPoolingAttribute(bean, beanName, factory);
		if (poolingAttribute == null) {
			// No pooling attribute
			return null;
		}
		else {
			AbstractPoolingTargetSource poolingTargetSource = newPoolingTargetSource(poolingAttribute);
			return poolingTargetSource;
		}
	}
	
	/**
	 * Create a new AbstractPoolingTargetSource. This implementation creates
	 * a CommonsPoolTargetSource, but subclasses may wish to override that
	 * behaviour. Don't need to set bean name or call setBeanFactory.
	 */
	protected AbstractPoolingTargetSource newPoolingTargetSource(PoolingAttribute poolingAttribute) {
		AbstractPoolingTargetSource poolingTargetSource = new CommonsPoolTargetSource();
		poolingTargetSource.setMaxSize(poolingAttribute.getSize());
		return poolingTargetSource;
	}

	/**
	 * Create a PoolingAttribute for the given bean, if any.
	 * @param bean the bean to create a PoolingAttribute for
	 * @param beanName the name of the bean
	 * @param beanFactory the current bean factory
	 * @return the PoolingAttribute, or null for no pooling
	 */
	protected abstract PoolingAttribute getPoolingAttribute(Object bean, String beanName, BeanFactory beanFactory);

}