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

/**
 * Thrown when a flow definition cannot be found in a registry.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class NoSuchFlowDefinitionException extends ServiceLookupException {

	/**
	 * Create a new flow definition lookup exception.
	 * @param flowBuilderClass the required implementation class of the service
	 *        that cannot be found
	 * @param cause the underlying cause of this exception
	 */
	public NoSuchFlowDefinitionException(Class flowBuilderClass, Throwable cause) {
		super(flowBuilderClass, "No flow definition was found produced by this FlowBuilder implementation '"
				+ flowBuilderClass
				+ "'; make sure there is exactly one FlowFactoryBean using this FlowBuilder exported in the registry",
				cause);
	}

	/**
	 * Create a new flow definition lookup exception.
	 * @param flowId the id of the service that cannot be found
	 * @param cause the underlying cause of this exception
	 */
	public NoSuchFlowDefinitionException(String flowId, Throwable cause) {
		super(flowId, "No flow definition was found with id '" + flowId
				+ "' -- make sure there is a Flow or FlowFactoryBean instance exported in the registry with this id",
				cause);
	}
}