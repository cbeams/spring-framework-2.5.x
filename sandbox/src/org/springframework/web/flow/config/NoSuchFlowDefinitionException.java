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

/**
 * Thrown when a flow definition cannot be found.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class NoSuchFlowDefinitionException extends FlowServiceLookupException {

	/**
	 * Create a new flow definition lookup exception.
	 * @param serviceImplementationClass The required implementation class of
	 *        the service that cannot be found
	 */
	public NoSuchFlowDefinitionException(Class serviceImplementationClass) {
		super(serviceImplementationClass);
	}

	/**
	 * Create a new flow definition lookup exception.
	 * @param serviceImplementationClass The required implementation class of
	 *        the service that cannot be found
	 * @param cause The underlying cause of this exception
	 */
	public NoSuchFlowDefinitionException(Class serviceImplementationClass, Throwable cause) {
		super(serviceImplementationClass, cause);
	}
	
	/**
	 * Create a new flow definition lookup exception.
	 * @param serviceId The id of the service that cannot be found
	 */
	public NoSuchFlowDefinitionException(String serviceId) {
		super(serviceId);
	}

	/**
	 * Create a new flow definition lookup exception.
	 * @param serviceId The id of the service that cannot be found
	 * @param cause The underlying cause of this exception
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