/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.web.flow.ActionBean;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowAttributesMapper;
import org.springframework.web.flow.FlowServiceLocator;
import org.springframework.web.flow.FlowServiceLookupException;
import org.springframework.web.flow.NoSuchActionBeanException;
import org.springframework.web.flow.NoSuchFlowDefinitionException;

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

	public ActionBean getActionBean(String actionBeanId) throws FlowServiceLookupException {
		try {
			return (ActionBean)getBeanFactory().getBean(actionBeanId, ActionBean.class);
		}
		catch (BeansException e) {
			throw new NoSuchActionBeanException(actionBeanId, e);
		}
	}

	public ActionBean getActionBean(Class actionBeanImplementationClass) throws FlowServiceLookupException {
		if (!ActionBean.class.isAssignableFrom(actionBeanImplementationClass)) {
			throw new IllegalArgumentException("Your action bean implementation '" + actionBeanImplementationClass
					+ "' must implement the '" + ActionBean.class.getName() + "' interface");

		}
		try {
			return (ActionBean)BeanFactoryUtils.beanOfType(getListableBeanFactory(), actionBeanImplementationClass);
		}
		catch (BeansException e) {
			throw new NoSuchActionBeanException(actionBeanImplementationClass, e);
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

	public Flow getFlow(Class flowDefinitionImplementationClass) throws FlowServiceLookupException {
		try {
			if (!Flow.class.isAssignableFrom(flowDefinitionImplementationClass)) {
				throw new IllegalArgumentException("Your flow definition implementation '"
						+ flowDefinitionImplementationClass + "' must be a subclass of '" + Flow.class.getName() + "'");
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
			throw new FlowServiceLookupException(flowAttributesMapperId, e);
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
			throw new FlowServiceLookupException(flowAttributesMapperImplementationClass, e);
		}
	}
}