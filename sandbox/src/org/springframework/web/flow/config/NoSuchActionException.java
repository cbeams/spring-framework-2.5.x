/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow.config;

/**
 * @author Keith Donald
 */
public class NoSuchActionException extends FlowServiceLookupException {
	
	public NoSuchActionException(String actionId) {
		super(actionId);
	}

	public NoSuchActionException(String serviceId, Throwable cause) {
		super(serviceId, cause);
	}

	public NoSuchActionException(Class actionImplementationClass) {
		super(actionImplementationClass);
	}

	public NoSuchActionException(Class serviceImplementationClass, Throwable cause) {
		super(serviceImplementationClass, cause);
	}

	public String getMessage() {
		if (isServiceIdLookupFailure()) {
			return "No action was found with id '" + getServiceId()
					+ "' -- make sure there is a single Action implementation exported in the context with this id";
		}
		else {
			return "No action was found of implementation '" + getServiceImplementationClass()
					+ "'; make sure there is a single Action implementation of this type exported in the context";
		}
	}

}