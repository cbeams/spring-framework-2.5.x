/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.target;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * Differs from the more commonly used InvokerInterceptor in that
 * a new instance of the target bean is created for each request.
 * This interceptor must run in a BeanFactory, as it needs
 * to do an explicit getBean() method to get the prototype
 * instance.
 * @author Rod Johnson
 * @version $Id: PrototypeTargetSource.java,v 1.1 2003-11-30 17:17:34 johnsonr Exp $
 */
public class PrototypeTargetSource implements TargetSource, BeanFactoryAware {
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	/**
	 * Name of the target bean we will create on each invocation
	 */
	private String targetBeanName;
	
	/**
	 * BeanFactory that owns this interceptor
	 */
	private BeanFactory owningBeanFactory;
	
	private Class targetClass;

	
	/**
	 * Set the name of the target bean in the factory.
	 * This bean should be a prototype, or the same instance
	 * will always be obtained from the factory,
	 * resulting in the same behaviour as the
	 * InvokerInterceptor 
	 * @param targetBeanName name of the target bean in the
	 * BeanFactory that owns this interceptor.
	 */
	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
	}
	
	public String getTargetBeanName() {
		return this.targetBeanName;
	}
	
	/**
	 * Set the owning BeanFactory. We need to save a reference
	 * so that we can use the getBean() method on every invocation.
	 */
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.owningBeanFactory = beanFactory;
		if (this.owningBeanFactory.isSingleton(this.targetBeanName)) {
			throw new BeanDefinitionStoreException("Cannot use PrototypeInvoker against a Singleton bean; instances would not be independent", null);
		}
		logger.info("Getting bean with name '" + targetBeanName + "' to find class");
		this.targetClass = owningBeanFactory.getBean(targetBeanName).getClass();
	}

	public Object getTarget() throws Exception {
		if (logger.isInfoEnabled()) {
			logger.info("Creating new target from bean '" + this.targetBeanName + "'");
		}
		return this.owningBeanFactory.getBean(this.targetBeanName);
	}
	
	/**
	 * @see org.springframework.aop.TargetSource#releaseTarget()
	 */
	public void releaseTarget(Object target) throws Exception {
	
	}
	
	public Class getTargetClass() {
		return this.targetClass;
	}

}
