/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

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
		setFlowDao(new BeanFactoryFlowDao());
	}

	/**
	 * @param id
	 */
	public BeanFactoryAwareFlow(String id) {
		super(id, new BeanFactoryFlowDao());
	}

	/**
	 * @param id
	 * @param startStateId
	 * @param flowDao
	 * @param states
	 */
	public BeanFactoryAwareFlow(String id, String startStateId, AbstractState[] states) {
		super(id, startStateId, new BeanFactoryFlowDao(), states);
	}

	public void setFlowDao(FlowDao dao) {
		Assert.isInstanceOf(BeanFactoryFlowDao.class, dao,
				"The FlowDao must be a BeanFactoryFlowDao implementation for BeanFactoryAwareFlows: ");
		super.setFlowDao(dao);
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		((BeanFactoryFlowDao)getFlowDao()).setBeanFactory(beanFactory);
	}

	protected BeanFactory getBeanFactory() {
		return ((BeanFactoryFlowDao)getFlowDao()).getBeanFactory();
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