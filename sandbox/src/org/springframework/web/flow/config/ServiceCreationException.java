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
package org.springframework.web.flow.config;

import org.springframework.web.flow.execution.ServiceLookupException;

/**
 * Exception thrown when a flow artifact cannot be created.
 *  
 * @author Erwin Vervaet
 */
public class ServiceCreationException extends ServiceLookupException {

	/**
	 * Create a new service creation exception.
	 * @param expectedClass the expected service type
	 * @param serviceImplementationClass the required implementation class of
	 *        the service that cannot be created
	 * @param message descriptive message
	 * @param cause the underlying cause of this exception
	 */
	public ServiceCreationException(Class expectedClass,
			Class serviceImplementationClass, String message, Throwable cause) {
		super(expectedClass, serviceImplementationClass, message, cause);
	}

}
