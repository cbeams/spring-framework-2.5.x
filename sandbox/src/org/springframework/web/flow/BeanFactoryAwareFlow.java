/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
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

	protected ListableBeanFactory getListableBeanFactory() {
		Assert.isInstanceOf(ListableBeanFactory.class, getBeanFactory());
		return (ListableBeanFactory)beanFactory;
	}

	protected AutowireCapableBeanFactory getAutowireCapableBeanFactory() {
		Assert.isInstanceOf(AutowireCapableBeanFactory.class, getBeanFactory());
		return (AutowireCapableBeanFactory)beanFactory;
	}

	protected Flow getFlow(Class flowClass) {
		Assert.isTrue(Flow.class.isAssignableFrom(flowClass), "Your flow class '" + flowClass
				+ "' must be a subclass of '" + Flow.class.getName() + "'");
		return (Flow)BeanFactoryUtils.beanOfTypeIncludingAncestors(getListableBeanFactory(), flowClass);
	}

	protected ActionBean getActionBean(Class actionBeanImplementationClass) {
		Assert.isTrue(ActionBean.class.isAssignableFrom(actionBeanImplementationClass), "Your action class '" + actionBeanImplementationClass
				+ "' must implement the '" + ActionBean.class.getName() + "' interface");
		return (ActionBean)BeanFactoryUtils.beanOfType(getListableBeanFactory(), actionBeanImplementationClass);
	}

	protected FlowAttributesMapper getAttributesMapper(String attributesMapperBeanNamePrefix) {
		return (FlowAttributesMapper)getBeanFactory().getBean(attributesMapper(attributesMapperBeanNamePrefix),
				FlowAttributesMapper.class);
	}

}