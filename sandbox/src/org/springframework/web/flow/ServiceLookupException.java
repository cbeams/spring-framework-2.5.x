/*
 * Copyright 2002-2005 the original author or authors.
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

import org.springframework.core.NestedRuntimeException;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Abstract superclass of all flow service lookup exceptions. A service lookup
 * exception is thrown when a service artifact required by a flow cannot be
 * obtained, either at flow configuration time or at runtime.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ServiceLookupException extends NestedRuntimeException {
	
	private Class expectedClass;

	private String serviceId;

	private Class serviceImplementationClass;

	/**
	 * Create a new service lookup exception.
	 * @param expectedClass the expected service type
	 * @param serviceId the id of the service that cannot be found
	 * @param cause the underlying cause of this exception
	 */
	public ServiceLookupException(Class expectedClass, String serviceId, Throwable cause) {
		this(expectedClass, serviceId, null, cause);
	}
	
	/**
	 * Create a new service lookup exception.
	 * @param expectedClass the expected service type
	 * @param serviceId the id of the service that cannot be found
	 * @param message descriptive message
	 * @param cause the underlying cause of this exception
	 */
	public ServiceLookupException(Class expectedClass, String serviceId, String message, Throwable cause) {
		super((StringUtils.hasText(message) ? message :
				"Unable to look up '" + ClassUtils.getShortName(expectedClass) + "' with id '" + serviceId
				+ "'; make sure there is at least one '" + ClassUtils.getShortName(expectedClass)
				+ "' exported in the registry with this id"), cause);
		this.expectedClass = expectedClass;
		this.serviceId = serviceId;
	}

	/**
	 * Create a new service lookup exception.
	 * @param expectedClass the expected service type
	 * @param serviceImplementationClass the required implementation class of
	 *        the service that cannot be found
	 * @param cause the underlying cause of this exception
	 */
	public ServiceLookupException(Class expectedClass, Class serviceImplementationClass, Throwable cause) {
		this(expectedClass, serviceImplementationClass, null, cause);
	}

	/**
	 * Create a new service lookup exception.
	 * @param expectedClass the expected service type
	 * @param serviceImplementationClass the required implementation class of
	 *        the service that cannot be found
	 * @param message descriptive message
	 * @param cause the underlying cause of this exception
	 */
	public ServiceLookupException(Class expectedClass, Class serviceImplementationClass, String message, Throwable cause) {
		super((StringUtils.hasText(message) ? message :
				"Unable to look up '" + ClassUtils.getShortName(expectedClass) + "' implementation '" + serviceImplementationClass
				+ "'; make sure there is a single '" + ClassUtils.getShortName(expectedClass)
				+ "' exported in the registry of this type"), cause);
		this.expectedClass = expectedClass;
		this.serviceImplementationClass = serviceImplementationClass;
	}

	/**
	 * Returns true if lookup by service id failed, false otherwise.
	 */
	public boolean isServiceIdLookupFailure() {
		return StringUtils.hasText(serviceId);
	}

	/**
	 * Returns true if lookup by service type failed, false otherwise.
	 */
	public boolean isServiceTypeLookupFailure() {
		return serviceImplementationClass != null;
	}
	
	/**
	 * Returns the expected service type.
	 */
	public Class getExpectedClass() {
		return expectedClass;
	}

	/**
	 * Returns the id of the service that cannot be found.
	 */
	public String getServiceId() {
		return serviceId;
	}

	/**
	 * Returns the required implementation class of the service that cannot be
	 * found.
	 */
	public Class getServiceImplementationClass() {
		return serviceImplementationClass;
	}

}