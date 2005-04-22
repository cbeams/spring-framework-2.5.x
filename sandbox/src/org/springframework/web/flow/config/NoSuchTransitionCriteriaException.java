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
 * Thrown when a transition criteria cannot be found.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class NoSuchTransitionCriteriaException extends ServiceLookupException {

	/**
	 * Create a new action lookup exception.
	 * 
	 * @param serviceId
	 *            the id of the service that cannot be found
	 * @param cause
	 *            the underlying cause of this exception
	 */
	public NoSuchTransitionCriteriaException(String serviceId, Throwable cause) {
		super(serviceId, "No transition criteria was found with id '" + serviceId
				+ "' -- make sure there is a single TransitionCriteria or "
				+ "TransitionCriteriaFactoryBean instance exported in the registry with this id", cause);
	}
}