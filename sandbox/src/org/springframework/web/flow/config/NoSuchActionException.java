/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow.config;

/**
 * @author Keith Donald
 */
public class NoSuchActionException extends FlowServiceLookupException {
	private NoSuchActionException(String actionBeanId) {
		super(actionBeanId);
	}

	private NoSuchActionException(Class actionBeanImplementationClass) {
		super(actionBeanImplementationClass);
	}

	/**
	 * @param serviceId
	 * @param cause
	 */
	public NoSuchActionException(String serviceId, Throwable cause) {
		super(serviceId, cause);
	}

	/**
	 * @param serviceImplementationClass
	 * @param cause
	 */
	public NoSuchActionException(Class serviceImplementationClass, Throwable cause) {
		super(serviceImplementationClass, cause);
	}

	public String getMessage() {
		if (isServiceIdLookupFailure()) {
			return "No action bean was found with id '" + getServiceId()
					+ "' -- make sure there is a single ActionBean implementation exported in the context with this id";
		}
		else {
			return "No action bean was found of implementation '" + getServiceImplementationClass()
					+ "'; make sure there is a single ActionBrean implementation of this type exported in the context";
		}
	}

}