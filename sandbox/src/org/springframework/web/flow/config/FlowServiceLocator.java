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

import org.springframework.web.flow.Action;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowAttributesMapper;

/**
 * Service locator interface used by flows to retrieve needed artifacts.
 * 
 * @author Keith Donald
 * @author Colin Sampaleanu
 * @author Erwin Vervaet
 */
public interface FlowServiceLocator {

	/**
	 * Lookup an action with specified id.
	 * @param actionId The action id
	 * @return The action
	 * @throws FlowServiceLookupException When the action cannot be found
	 */
	public Action getAction(String actionId) throws FlowServiceLookupException;

	/**
	 * Lookup an action of specified implementation class.
	 * @param actionImplementationClass The required implementation class
	 * @return the action
	 * @throws FlowServiceLookupException When the action cannot be found
	 */
	public Action getAction(Class actionImplementationClass) throws FlowServiceLookupException;

	/**
	 * Lookup a flow with specified id.
	 * @param flowDefinitionId The flow id
	 * @return The flow
	 * @throws FlowServiceLookupException When the flow cannot be found
	 */
	public Flow getFlow(String flowDefinitionId) throws FlowServiceLookupException;

	/**
	 * Lookup a flow build by specified type of flow builder.
	 * @param flowDefinitionId the flow id
	 * @param requiredFlowBuilderImplementationClass The required builder type
	 * @return the flow
	 * @throws FlowServiceLookupException When the flow cannot be found
	 */
	public Flow getFlow(String flowDefinitionId, Class requiredFlowBuilderImplementationClass)
			throws FlowServiceLookupException;

	/**
	 * Lookup a flow of specified implementation class.
	 * @param flowDefinitionImplementationClass The required implementation class.
	 * @return The flow
	 * @throws FlowServiceLookupException When the flow cannot be found
	 */
	public Flow getFlow(Class flowDefinitionImplementationClass) throws FlowServiceLookupException;

	/**
	 * Lookup a flow attributes mapper with specified id.
	 * @param flowAttributesMapperId The flow attributes mapper id
	 * @return The flow attributes mapper
	 * @throws FlowServiceLookupException When the flow attributes mapper cannot
	 *         be found
	 */
	public FlowAttributesMapper getFlowAttributesMapper(String flowAttributesMapperId)
			throws FlowServiceLookupException;

	/**
	 * Lookup a flow attributes mapper of specified implementation class.
	 * @param flowAttributesMapperId The required implementation class
	 * @return The flow attributes mapper
	 * @throws FlowServiceLookupException When the flow attributes mapper cannot
	 *         be found
	 */
	public FlowAttributesMapper getFlowAttributesMapper(Class flowAttributesMapperImplementationClass)
			throws FlowServiceLookupException;

}