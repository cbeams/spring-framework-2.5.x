/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */

package org.springframework.aop.target;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * Base class for dynamic TargetSources that can create new prototype bean
 * instances to support a pooling or new-instance-per-invocation strategy. Such
 * TargetSources must run in a BeanFactory, as it needs to call the getBean()
 * method to create a new prototype instance.
 * 
 * @author Rod Johnson
 * @version $Id: AbstractPrototypeTargetSource.java,v 1.1 2003/12/11 10:58:12
 *               johnsonr Exp $
 */
public abstract class AbstractPrototypeTargetSource implements TargetSource, BeanFactoryAware, InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Name of the target bean we will create on each invocation
	 */
	private String targetBeanName;

	/**
	 * BeanFactory that owns this TargetSource. We need to hold onto this
	 * reference so that we can create new prototype instances as necessary.
	 */
	private BeanFactory owningBeanFactory;

	/** Class of the target */
	private Class targetClass;

	/**
	 * Set the name of the target bean in the factory. This bean should be a
	 * prototype, or the same instance will always be obtained from the
	 * factory, resulting in the same behaviour as the InvokerInterceptor
	 * 
	 * @param targetBeanName
	 *                  name of the target bean in the BeanFactory that owns this
	 *                  interceptor.
	 */
	public final void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
	}

	public final String getTargetBeanName() {
		return this.targetBeanName;
	}

	/**
	 * Set the owning BeanFactory. We need to save a reference so that we can
	 * use the getBean() method on every invocation.
	 */
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.owningBeanFactory = beanFactory;
		if (this.owningBeanFactory.isSingleton(this.targetBeanName)) {
			throw new BeanDefinitionStoreException(
				"Cannot use PrototypeTargetSource against a Singleton bean; instances would not be independent",
				null);
		}
		logger.info("Getting bean with name '" + targetBeanName + "' to find class");
		this.targetClass = owningBeanFactory.getBean(targetBeanName).getClass();
	}

	/**
	 * Subclasses should use this method to create a new prototype instance.
	 */
	protected Object newPrototypeInstance() {
		if (logger.isInfoEnabled()) {
			logger.info("Creating new target from bean '" + this.targetBeanName + "'");
		}
		return this.owningBeanFactory.getBean(this.targetBeanName);
	}

	/**
	 * @see org.springframework.aop.TargetSource#getTargetClass()
	 */
	public final Class getTargetClass() {
		return this.targetClass;
	}

	/**
	 * @see org.springframework.aop.TargetSource#isStatic()
	 */
	public final boolean isStatic() {
		return false;
	}

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		if (this.targetBeanName == null) {
			throw new IllegalStateException("targetBeanName property must be set.");
		}
	}
}
