/*
 * The Spring Framework is published under the terms of the Apache Software License.
 */

package org.springframework.aop.framework.autoproxy.target;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.TargetSourceCreator;
import org.springframework.aop.target.AbstractPrototypeTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Convenient superclass for TargetSourceCreators that require creating multiple
 * instances of a prototype bean.
 * @author Rod Johnson
 * @version $Id: AbstractPrototypeTargetSourceCreator.java,v 1.2 2003-12-30 01:07:11 jhoeller Exp $
 */
public abstract class AbstractPrototypeTargetSourceCreator implements TargetSourceCreator {

	protected final Log logger = LogFactory.getLog(getClass());

	public final TargetSource getTargetSource(Object bean, String beanName, BeanFactory factory) {
		
		AbstractPrototypeTargetSource prototypeTargetSource = createPrototypeTargetSource(bean, beanName, factory);
		
		if (prototypeTargetSource == null) {
			return null;
		}
		else {			
			if (!(factory instanceof BeanDefinitionRegistry)) {
				logger.warn("Cannot do autopooling with a BeanFactory that doesn't implement BeanDefinitionRegistry");
				return null;
			}
			BeanDefinitionRegistry definitionRegistry = (BeanDefinitionRegistry) factory;
			RootBeanDefinition definition = (RootBeanDefinition) definitionRegistry.getBeanDefinition(beanName);

			logger.info("Configuring AbstractPrototypeTargetSource...");
		
			// Infinite cycle will result if we don't use a different factory,
			// because a getBean() call with this beanName will go through the autoproxy
			// infrastructure again.
			// We to override just this bean definition, as it may reference other beans
			// and we're happy to take the parent's definition for those.
			DefaultListableBeanFactory beanFactory2 = new DefaultListableBeanFactory(factory);
			// Override the prototype bean
			beanFactory2.registerBeanDefinition(beanName, definition);
			
			// Complete configuring the PrototypeTargetSource
			prototypeTargetSource.setTargetBeanName(beanName);
			prototypeTargetSource.setBeanFactory(beanFactory2);

			return prototypeTargetSource;
		}
	}

	/**
	 * Subclasses must implement this method to return a new AbstractPrototypeTargetSource
	 * if they wish to create a custom TargetSource for this bean, or null if they are
	 * not interested it in, in which case no special target source will be created.
	 * Subclasses should not call setTargetBeanName() or setBeanFactory() on the
	 * AbstractPrototypeTargetSource: this class's implementation of
	 * getTargetSource() will do that.
	 * @return null if we don't match this
	 */
	protected abstract AbstractPrototypeTargetSource createPrototypeTargetSource(Object bean, String beanName, BeanFactory factory);

}
