/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

/**
 * @author Keith Donald
 */
public class NoSuchActionBeanException extends FlowServiceLookupException {
	private NoSuchActionBeanException(String actionBeanId) {
		super(actionBeanId);
	}

	private NoSuchActionBeanException(Class actionBeanImplementationClass) {
		super(actionBeanImplementationClass);
	}

	/**
	 * @param serviceId
	 * @param cause
	 */
	public NoSuchActionBeanException(String serviceId, Throwable cause) {
		super(serviceId, cause);
	}

	/**
	 * @param serviceImplementationClass
	 * @param cause
	 */
	public NoSuchActionBeanException(Class serviceImplementationClass, Throwable cause) {
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