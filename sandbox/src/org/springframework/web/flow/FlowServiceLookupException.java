/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

import org.springframework.util.StringUtils;

/**
 * @author Keith Donald
 */
public class FlowServiceLookupException extends RuntimeException {

	private String serviceId;

	private Class serviceImplementationClass;

	/**
	 *  
	 */
	public FlowServiceLookupException(String serviceId) {
		super();
		this.serviceId = serviceId;
	}

	/**
	 *  
	 */
	public FlowServiceLookupException(String serviceId, Throwable cause) {
		super(cause);
		this.serviceId = serviceId;
	}

	/**
	 *  
	 */
	public FlowServiceLookupException(String serviceId, String message, Throwable cause) {
		super(message, cause);
		this.serviceId = serviceId;
	}

	/**
	 *  
	 */
	public FlowServiceLookupException(Class serviceImplementationClass) {
		super();
		this.serviceImplementationClass = serviceImplementationClass;
	}

	/**
	 *  
	 */
	public FlowServiceLookupException(Class serviceImplementationClass, Throwable cause) {
		super(cause);
		this.serviceImplementationClass = serviceImplementationClass;
	}

	/**
	 *  
	 */
	public FlowServiceLookupException(Class serviceImplementationClass, String message, Throwable cause) {
		super(message, cause);
		this.serviceImplementationClass = serviceImplementationClass;
	}

	public String getMessage() {
		if (StringUtils.hasText(super.getMessage())) {
			return super.getMessage();
		}
		else {
			if (StringUtils.hasText(serviceId)) {
				return "Unable to look up service with id '" + serviceId
						+ "'; make sure there is at least one service exported in the context with this id";
			}
			else {
				return "Unable to look up service implementation '" + serviceImplementationClass
						+ "'; make sure there is a single service exported in the context of this type";
			}
		}
	}

	public boolean isServiceIdLookupFailure() {
		return StringUtils.hasText(serviceId);
	}

	public boolean isServiceTypeLookupFailure() {
		return serviceImplementationClass != null;
	}

	public String getServiceId() {
		return serviceId;
	}

	public Class getServiceImplementationClass() {
		return serviceImplementationClass;
	}

}