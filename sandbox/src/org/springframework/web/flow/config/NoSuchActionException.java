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

import org.springframework.web.flow.ServiceLookupException;

/**
 * Thrown when an action cannot be found.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class NoSuchActionException extends ServiceLookupException {

	/**
	 * Create a new action lookup exception.
	 * @param actionId the id of the service that cannot be found
	 * @param cause the underlying cause of this exception
	 */
	public NoSuchActionException(String actionId, Throwable cause) {
		super(actionId, "No action was found with id '" + actionId
				+ "' -- make sure there is a single Action instance exported in the registry with this id", cause);
	}

	/**
	 * Create a new action lookup exception.
	 * @param actionImplementationClass the required implementation class of
	 *        the service that cannot be found
	 * @param cause the underlying cause of this exception
	 */
	public NoSuchActionException(Class actionImplementationClass, Throwable cause) {
		super(actionImplementationClass, "No action was found of implementation '" + actionImplementationClass
				+ "' -- make sure there is exactly one Action instace of this type exported in the registry", cause);
	}
}