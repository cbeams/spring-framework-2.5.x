/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

/**
 * @author Keith Donald
 */
public class NoSuchFlowDefinitionException extends FlowServiceLookupException {

	/**
	 * @param serviceImplementationClass
	 */
	public NoSuchFlowDefinitionException(Class serviceImplementationClass) {
		super(serviceImplementationClass);
	}

	/**
	 * @param serviceImplementationClass
	 * @param cause
	 */
	public NoSuchFlowDefinitionException(Class serviceImplementationClass, Throwable cause) {
		super(serviceImplementationClass, cause);
	}

	/**
	 * @param serviceId
	 * @param cause
	 */
	public NoSuchFlowDefinitionException(String serviceId, Throwable cause) {
		super(serviceId, cause);
	}

	public String getMessage() {
		if (isServiceIdLookupFailure()) {
			return "No flow definition was found with id '" + getServiceId()
					+ "' -- make sure there is a single Flow implementation exported in the context with this id";
		}
		else {
			return "No flow definition was found of implementation '" + getServiceImplementationClass()
					+ "'; make sure there is a single Flow implementation of this type exported in the context";
		}
	}

}