/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.flow.AbstractState;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowServiceLocator;

/**
 * Specialization of Flow that is aware of the Spring <code>BeanFactory</code>
 * lightweigt container. Automatically receives an initialization callback from
 * the container.
 * @author Keith Donald
 */
public class BeanFactoryAwareFlow extends Flow implements BeanFactoryAware, BeanNameAware, InitializingBean {

	private BeanFactory beanFactory;

	public BeanFactoryAwareFlow() {
		super();
		setServiceLocator(new BeanFactoryFlowServiceLocator());
	}

	/**
	 * @param id
	 */
	public BeanFactoryAwareFlow(String id) {
		super(id, new BeanFactoryFlowServiceLocator());
	}

	/**
	 * @param id
	 * @param startStateId
	 * @param flowDao
	 * @param states
	 */
	public BeanFactoryAwareFlow(String id, String startStateId, AbstractState[] states) {
		super(id, startStateId, new BeanFactoryFlowServiceLocator(), states);
	}

	public void setServiceLocator(FlowServiceLocator dao) {
		Assert.isInstanceOf(BeanFactoryFlowServiceLocator.class, dao,
				"The FlowServiceLocator must be a BeanFactoryFlowServiceLocator implementation "
						+ "for BeanFactoryAwareFlows: ");
		super.setServiceLocator(dao);
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		((BeanFactoryFlowServiceLocator)getServiceLocator()).setBeanFactory(beanFactory);
	}

	protected BeanFactory getBeanFactory() {
		return ((BeanFactoryFlowServiceLocator)getServiceLocator()).getBeanFactory();
	}

	public void setBeanName(String name) {
		if (getId() == null) {
			setId(name);
		}
	}

	public void afterPropertiesSet() {
		initFlow();
	}

}