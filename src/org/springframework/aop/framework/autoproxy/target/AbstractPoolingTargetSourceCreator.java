/*
 * The Spring Framework is published under the terms of the Apache Software License.
 */

package org.springframework.aop.framework.autoproxy.target;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.TargetSourceCreator;
import org.springframework.aop.target.AbstractPoolingTargetSource;
import org.springframework.aop.target.CommonsPoolTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * @author Rod Johnson
 * @version $Id: AbstractPoolingTargetSourceCreator.java,v 1.1 2003-12-12 18:43:45 johnsonr Exp $
 */
public abstract class AbstractPoolingTargetSourceCreator implements TargetSourceCreator {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * @see org.springframework.aop.framework.support.TargetSourceCreator#getTargetSource(java.lang.Object,
	 *      java.lang.String, org.springframework.beans.factory.ListableBeanFactory)
	 */
	public final TargetSource getTargetSource(Object bean, String beanName, BeanFactory factory) {
		PoolingAttribute poolingAttribute = getPoolingAttribute(bean, beanName, factory);
		if (poolingAttribute == null) {
			// No pooling attribute
			return null;
		}
		else {
			if (!(factory instanceof BeanDefinitionRegistry)) {
				logger.warn("Cannot do autopooling with a BeanFactory that doesn't implement BeanDefinitionRegistry");
				return null;
			}
			BeanDefinitionRegistry definitionRegistry = (BeanDefinitionRegistry) factory;
			RootBeanDefinition definition = (RootBeanDefinition) definitionRegistry.getBeanDefinition(beanName);

			logger.info("Configuring pooling...");

			AbstractPoolingTargetSource poolingTargetSource = newPoolingTargetSource(poolingAttribute);
			
			poolingTargetSource.setTargetBeanName(beanName);
		
			// Infinite cycle will result if we don't use a different factory,
			// because a getBean() call with this beanName will go through the autoproxy
			// infrastructure again.
			// We to override just this bean definition, as it may reference other beans
			// and we're happy to take the parent's definition for those.
			DefaultListableBeanFactory bf2 = new DefaultListableBeanFactory(factory);
			// Override this bean
			bf2.registerBeanDefinition(beanName, definition);
			poolingTargetSource.setBeanFactory(bf2);

			return poolingTargetSource;
		}
	}
	
	/**
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