/*
 * The Spring Framework is published under the terms of the Apache Software License.
 */

package org.springframework.aop.framework.autoproxy.target;

import org.springframework.aop.target.AbstractPoolingTargetSource;
import org.springframework.aop.target.AbstractPrototypeTargetSource;
import org.springframework.aop.target.CommonsPoolTargetSource;
import org.springframework.beans.factory.BeanFactory;

/**
 * Convenient superclass for TargetSource creators that creating
 * pooling TargetSources.
 * @author Rod Johnson
 * @version $Id: AbstractPoolingTargetSourceCreator.java,v 1.2 2003-12-14 16:10:51 johnsonr Exp $
 */
public abstract class AbstractPoolingTargetSourceCreator extends AbstractPrototypeTargetSourceCreator {

	/**
	 * @see org.springframework.aop.framework.support.TargetSourceCreator#getTargetSource(java.lang.Object,
	 *      java.lang.String, org.springframework.beans.factory.ListableBeanFactory)
	 */
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
	 * behaviour.
	 * Don't need to set bean name or call setBeanFactory
	 * @param poolingAttribute
	 * @return
	 */
	protected AbstractPoolingTargetSource newPoolingTargetSource(PoolingAttribute poolingAttribute) {
		AbstractPoolingTargetSource poolingTargetSource = new CommonsPoolTargetSource();
		poolingTargetSource.setMaxSize(poolingAttribute.getSize());
		return poolingTargetSource;
	}

	/**
	 * Return null for no pooling
	 * @param bean
	 * @param beanName
	 * @param bf
	 * @return
	 */
	protected abstract PoolingAttribute getPoolingAttribute(Object bean, String beanName, BeanFactory bf);

}