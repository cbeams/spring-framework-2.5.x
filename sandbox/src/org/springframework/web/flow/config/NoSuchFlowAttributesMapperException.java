/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow.config;

/**
 * @author Keith Donald
 */
public class NoSuchFlowAttributesMapperException extends FlowServiceLookupException {

	/**
	 * @param serviceImplementationClass
	 */
	public NoSuchFlowAttributesMapperException(Class serviceImplementationClass) {
		super(serviceImplementationClass);
	}

	/**
	 * @param serviceImplementationClass
	 * @param cause
	 */
	public NoSuchFlowAttributesMapperException(Class serviceImplementationClass, Throwable cause) {
		super(serviceImplementationClass, cause);
	}

	/**
	 * @param serviceId
	 */
	public NoSuchFlowAttributesMapperException(String serviceId) {
		super(serviceId);
	}

	/**
	 * @param serviceId
	 * @param cause
	 */
	public NoSuchFlowAttributesMapperException(String serviceId, Throwable cause) {
		super(serviceId, cause);
	}

	public String getMessage() {
		if (isServiceIdLookupFailure()) {
			return "No attributes mapper was found with id '"
					+ getServiceId()
					+ "' -- make sure there is a single FlowAttributesMapper implementation exported in the context with this id";
		}
		else {
			return "No attributes mapper was found of implementation '"
					+ getServiceImplementationClass()
					+ "'; make sure there is a single FlowAttributesMapper implementation of this type exported in the context";
		}
	}

}