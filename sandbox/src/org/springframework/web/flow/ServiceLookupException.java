/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.web.flow;

import org.springframework.util.StringUtils;

/**
 * Abstract superclass of all flow service lookup exceptions.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class ServiceLookupException extends RuntimeException {

	private String serviceId;

	private Class serviceImplementationClass;

	/**
	 * Create a new service lookup exception
	 * @param serviceId The id of the service that cannot be found
	 */
	public ServiceLookupException(String serviceId) {
		super();
		this.serviceId = serviceId;
	}

	/**
	 * Create a new service lookup exception
	 * @param serviceId The id of the service that cannot be found
	 * @param cause The underlying cause of this exception
	 */
	public ServiceLookupException(String serviceId, Throwable cause) {
		super(cause);
		this.serviceId = serviceId;
	}

	/**
	 * Create a new service lookup exception
	 * @param serviceId The id of the service that cannot be found
	 * @param message Descriptive message
	 * @param cause The underlying cause of this exception
	 */
	public ServiceLookupException(String serviceId, String message, Throwable cause) {
		super(message, cause);
		this.serviceId = serviceId;
	}

	/**
	 * Create a new service lookup exception
	 * @param serviceImplementationClass The required implementation class of
	 *        the service that cannot be found
	 */
	public ServiceLookupException(Class serviceImplementationClass) {
		super();
		this.serviceImplementationClass = serviceImplementationClass;
	}

	/**
	 * Create a new service lookup exception
	 * @param serviceImplementationClass The required implementation class of
	 *        the service that cannot be found
	 * @param cause The underlying cause of this exception
	 */
	public ServiceLookupException(Class serviceImplementationClass, Throwable cause) {
		super(cause);
		this.serviceImplementationClass = serviceImplementationClass;
	}

	/**
	 * Create a new service lookup exception
	 * @param serviceImplementationClass The required implementation class of
	 *        the service that cannot be found
	 * @param message Descriptive message
	 * @param cause The underlying cause of this exception
	 */
	public ServiceLookupException(Class serviceImplementationClass, String message, Throwable cause) {
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

	/**
	 * @return true if lookup by service id failed, false otherwise
	 */
	public boolean isServiceIdLookupFailure() {
		return StringUtils.hasText(serviceId);
	}

	/**
	 * @return true if lookup by service type failed, false otherwise
	 */
	public boolean isServiceTypeLookupFailure() {
		return serviceImplementationClass != null;
	}

	/**
	 * @return the id of the service that cannot be found
	 */
	public String getServiceId() {
		return serviceId;
	}

	/**
	 * @return the required implementation class of the service that cannot be found
	 */
	public Class getServiceImplementationClass() {
		return serviceImplementationClass;
	}

}