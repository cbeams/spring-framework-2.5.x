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
package org.springframework.web.flow.config;

import org.springframework.web.flow.ServiceLookupException;

/**
 * Thrown when an action cannot be found.
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class NoSuchActionException extends ServiceLookupException {

	/**
	 * Create a new action lookup exception.
	 * @param serviceId The id of the service that cannot be found
	 */
	public NoSuchActionException(String serviceId) {
		super(serviceId);
	}

	/**
	 * Create a new action lookup exception.
	 * @param serviceId The id of the service that cannot be found
	 * @param cause The underlying cause of this exception
	 */
	public NoSuchActionException(String serviceId, Throwable cause) {
		super(serviceId, cause);
	}

	/**
	 * Create a new action lookup exception.
	 * @param serviceImplementationClass The required implementation class of
	 *        the service that cannot be found
	 */
	public NoSuchActionException(Class serviceImplementationClass) {
		super(serviceImplementationClass);
	}

	/**
	 * Create a new action lookup exception.
	 * @param serviceImplementationClass The required implementation class of
	 *        the service that cannot be found
	 * @param cause The underlying cause of this exception
	 */
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