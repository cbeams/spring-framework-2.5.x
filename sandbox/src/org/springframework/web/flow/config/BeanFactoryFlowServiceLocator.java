/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowAttributesMapper;

/**
 * A flow service locator that uses a Spring bean factory to lookup services.
 * @author Keith Donald
 */
public class BeanFactoryFlowServiceLocator implements FlowServiceLocator, BeanFactoryAware {

	private BeanFactory beanFactory;

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	protected ListableBeanFactory getListableBeanFactory() {
		return (ListableBeanFactory)getBeanFactory();
	}

	public Action getActionBean(String actionBeanId) throws FlowServiceLookupException {
		try {
			return (Action)getBeanFactory().getBean(actionBeanId, Action.class);
		}
		catch (BeansException e) {
			throw new NoSuchActionException(actionBeanId, e);
		}
	}

	public Action getActionBean(Class actionBeanImplementationClass) throws FlowServiceLookupException {
		if (!Action.class.isAssignableFrom(actionBeanImplementationClass)) {
			throw new IllegalArgumentException("Your action bean implementation '" + actionBeanImplementationClass
					+ "' must implement the '" + Action.class.getName() + "' interface");

		}
		try {
			return (Action)BeanFactoryUtils.beanOfType(getListableBeanFactory(), actionBeanImplementationClass);
		}
		catch (BeansException e) {
			throw new NoSuchActionException(actionBeanImplementationClass, e);
		}
	}

	public Flow getFlow(String flowDefinitionId) throws FlowServiceLookupException {
		try {
			return (Flow)getBeanFactory().getBean(flowDefinitionId, Flow.class);
		}
		catch (BeansException e) {
			throw new NoSuchFlowDefinitionException(flowDefinitionId, e);
		}
	}

	public Flow getFlow(String flowDefinitionId, Class requiredBuilderImplementationClass)
			throws FlowServiceLookupException {
		try {
			String flowFactoryBeanId = "&" + flowDefinitionId;
			FlowFactoryBean factoryBean = (FlowFactoryBean)getBeanFactory().getBean(flowFactoryBeanId,
					FlowFactoryBean.class);
			if (factoryBean.buildsWith(requiredBuilderImplementationClass)) {
				return factoryBean.getFlow();
			}
			else {
				throw new NoSuchFlowDefinitionException(flowDefinitionId, new IllegalStateException(
						"The flow factory must produce flows using FlowBuilder of type '"
								+ requiredBuilderImplementationClass + "' but it doesn't"));
			}
		}
		catch (BeansException e) {
			throw new NoSuchFlowDefinitionException(flowDefinitionId, e);
		}
	}

	public Flow getFlow(Class flowDefinitionImplementationClass) throws FlowServiceLookupException {
		try {
			if (!Flow.class.isAssignableFrom(flowDefinitionImplementationClass)) {
				throw new IllegalArgumentException("The flow definition implementation  '"
						+ flowDefinitionImplementationClass + "' you wish to retrieve must be a subclass of '"
						+ Flow.class.getName() + "'");
			}
			return (Flow)BeanFactoryUtils.beanOfType(getListableBeanFactory(), flowDefinitionImplementationClass);
		}
		catch (BeansException e) {
			throw new NoSuchFlowDefinitionException(flowDefinitionImplementationClass, e);
		}
	}

	public FlowAttributesMapper getFlowAttributesMapper(String flowAttributesMapperId)
			throws FlowServiceLookupException {
		try {
			return (FlowAttributesMapper)getBeanFactory().getBean(flowAttributesMapperId, FlowAttributesMapper.class);
		}
		catch (BeansException e) {
			throw new NoSuchFlowAttributesMapperException(flowAttributesMapperId, e);
		}
	}

	public FlowAttributesMapper getFlowAttributesMapper(Class flowAttributesMapperImplementationClass)
			throws FlowServiceLookupException {
		if (!FlowAttributesMapper.class.isAssignableFrom(flowAttributesMapperImplementationClass)) {
			throw new IllegalArgumentException("Your attributes mapper implementation '"
					+ flowAttributesMapperImplementationClass + "' must implement the '"
					+ FlowAttributesMapper.class.getName() + "' interface");

		}
		try {
			return (FlowAttributesMapper)BeanFactoryUtils.beanOfType(getListableBeanFactory(),
					flowAttributesMapperImplementationClass);
		}
		catch (BeansException e) {
			throw new NoSuchFlowAttributesMapperException(flowAttributesMapperImplementationClass, e);
		}
	}
}