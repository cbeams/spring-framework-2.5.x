/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow.config;

import org.springframework.web.flow.Action;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowAttributesMapper;
import org.springframework.web.flow.ServiceLookupException;

/**
 * @author Keith Donald
 */
public class FlowServiceLocatorAdapter implements FlowServiceLocator {

	public Action getAction(String actionId) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}

	public Action getAction(Class actionImplementationClass) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}

	public Flow getFlow(String flowDefinitionId) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}

	public Flow getFlow(String flowDefinitionId, Class requiredFlowBuilderImplementationClass)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}

	public Flow getFlow(Class flowDefinitionImplementationClass) throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}

	public FlowAttributesMapper getFlowAttributesMapper(String flowAttributesMapperId)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}

	public FlowAttributesMapper getFlowAttributesMapper(Class flowAttributesMapperImplementationClass)
			throws ServiceLookupException {
		throw new UnsupportedOperationException();
	}
}