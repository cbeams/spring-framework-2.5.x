/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public class BeanFactoryAwareFlow extends Flow implements BeanFactoryAware, BeanNameAware, InitializingBean {

	private BeanFactory beanFactory;

	public BeanFactoryAwareFlow() {
		super();
	}

	/**
	 * @param id
	 */
	public BeanFactoryAwareFlow(String id) {
		super(id);
	}

	/**
	 * @param id
	 * @param flowDao
	 */
	public BeanFactoryAwareFlow(String id, FlowDao flowDao) {
		super(id, flowDao);
	}

	/**
	 * @param id
	 * @param startStateId
	 * @param flowDao
	 * @param states
	 */
	public BeanFactoryAwareFlow(String id, String startStateId, FlowDao flowDao, AbstractState[] states) {
		super(id, startStateId, flowDao, states);
	}

	/**
	 *  
	 */
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 *  
	 */
	public void setBeanName(String name) {
		if (getId() == null) {
			setId(name);
		}
	}

	public void afterPropertiesSet() {
		initFlow();
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	protected AutowireCapableBeanFactory getAutowireCapableBeanFactory() {
		Assert.isInstanceOf(AutowireCapableBeanFactory.class, getBeanFactory());
		return (AutowireCapableBeanFactory)beanFactory;
	}

	protected ActionBean getActionBean(Class actionBeanClassName) {
		return (ActionBean)getAutowireCapableBeanFactory().autowire(actionBeanClassName,
				AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
	}

}