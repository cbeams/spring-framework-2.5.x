/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.autoproxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.target.AbstractPoolingTargetSource;
import org.springframework.aop.target.CommonsPoolTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * 
 * @author Rod Johnson
 * @version $Id: PoolingTargetSourceCreator.java,v 1.1 2003-12-12 16:50:43 johnsonr Exp $
 */
public class PoolingTargetSourceCreator implements TargetSourceCreator {
	
	private Log logger = LogFactory.getLog(getClass());

	/**
	 * @see org.springframework.aop.framework.support.TargetSourceCreator#getTargetSource(java.lang.Object, java.lang.String, org.springframework.beans.factory.ListableBeanFactory)
	 */
	public TargetSource getTargetSource(Object bean, String beanName, BeanFactory factory) {
		if (!(factory instanceof BeanDefinitionRegistry)) {
			logger.warn("Cannot do autopooling with a BeanFactory that doesn't implement BeanDefinitionRegistry");
			return null;
		}
			
		AbstractBeanFactory bf = (AbstractBeanFactory) factory;
		RootBeanDefinition definition = (RootBeanDefinition) bf.getBeanDefinition(beanName);
		
		
		logger.info("Configuring pooling...");
			
		AbstractPoolingTargetSource cpii = new CommonsPoolTargetSource();
		cpii.setMaxSize(25);
		cpii.setTargetBeanName(beanName);
		try {
			// Infinite cycle: tries to create the bean if we don't use a different factory
			DefaultListableBeanFactory bf2 = new DefaultListableBeanFactory(factory);
			// Override this bean
			bf2.registerBeanDefinition(beanName, definition);
			cpii.setBeanFactory(bf2);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}
		System.err.println("GOTCHA!");
		return cpii;
	}

}
