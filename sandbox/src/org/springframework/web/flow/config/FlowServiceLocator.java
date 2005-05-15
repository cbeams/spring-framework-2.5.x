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

import org.springframework.web.flow.Action;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowAttributeMapper;
import org.springframework.web.flow.State;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.TransitionCriteria;
import org.springframework.web.flow.execution.FlowLocator;
import org.springframework.web.flow.execution.ServiceLookupException;


/**
 * Service locator interface used by flow builders at configuration time to
 * retrieve needed artifacts.
 * <p>
 * Note that this service locator is a configuration time object. It is not used
 * during flow execution!
 * 
 * @author Keith Donald
 * @author Colin Sampaleanu
 * @author Erwin Vervaet
 */
public interface FlowServiceLocator extends FlowLocator {
	
	// dealing with flows
	
	/**
	 * Request that the registry backed by this locator instantiate the flow
	 * of the specified implementation class, using the given autowire policy.
	 * Note: not all registries may support this advanced feature (Spring does
	 * though ;-)).
	 * @param implementationClass the flow implementation class
	 * @param autowireMode the autowire policy
	 * @return the instantiated (and possibly autowired) flow
	 * @throws ServiceLookupException when the flow cannot be created
	 */
	public Flow createFlow(Class implementationClass, AutowireMode autowireMode) throws ServiceLookupException;
	
	/**
	 * Lookup a flow of specified implementation class; there must exactly one
	 * flow implementation of the specified implementation in the registry this
	 * locator queries.
	 * @param implementationClass the required implementation class
	 * @return the flow
	 * @throws ServiceLookupException when the flow cannot be found, or more
	 *         than one flow of the specified type exists
	 */
	public Flow getFlow(Class implementationClass) throws ServiceLookupException;

	/**
	 * Lookup a flow build by specified type of flow builder.
	 * @param id the flow id
	 * @param requiredFlowBuilderImplementationClass the required builder type
	 * @return the flow
	 * @throws ServiceLookupException when the flow cannot be found
	 */
	public Flow getFlow(String id, Class requiredFlowBuilderImplementationClass) throws ServiceLookupException;
	
	// dealing with states

	/**
	 * Request that the registry backed by this locator instantiate the state
	 * of the specified implementation class, using the given autowire policy.
	 * Note: not all registries may support this advanced feature (Spring does
	 * though ;-)).
	 * @param implementationClass the state implementation class
	 * @param autowireMode the autowire policy
	 * @return the instantiated (and possibly autowired) state
	 * @throws ServiceLookupException when the state cannot be created
	 */
	public State createState(Class implementationClass, AutowireMode autowireMode) throws ServiceLookupException;

	/**
	 * Lookup a state with specified id.
	 * @param id the state id
	 * @return the state
	 * @throws ServiceLookupException when the state cannot be found
	 */
	public State getState(String id) throws ServiceLookupException;

	/**
	 * Lookup a state of specified implementation class.
	 * @param implementationClass the required implementation class
	 * @return the state
	 * @throws ServiceLookupException when the state cannot be found
	 */
	public State getState(Class implementationClass) throws ServiceLookupException;

	// dealing with transitions

	/**
	 * Request that the registry backed by this locator instantiate the transition
	 * of the specified implementation class, using the given autowire policy.
	 * Note: not all registries may support this advanced feature (Spring does
	 * though ;-)).
	 * @param implementationClass the transition implementation class
	 * @param autowireMode the autowire policy
	 * @return the instantiated (and possibly autowired) transition
	 * @throws ServiceLookupException when the transition object
	 *         cannot be created
	 */
	public Transition createTransition(Class implementationClass, AutowireMode autowireMode) throws ServiceLookupException;

	/**
	 * Lookup a transition with specified id.
	 * @param id the transition id
	 * @return the transition
	 * @throws ServiceLookupException when the transition cannot be found
	 */
	public Transition getTransition(String id) throws ServiceLookupException;

	/**
	 * Lookup a transition of specified implementation class.
	 * @param implementationClass the required implementation class
	 * @return the transition
	 * @throws ServiceLookupException when the transition cannot be found
	 */
	public Transition getTransition(Class implementationClass) throws ServiceLookupException;

	// dealing with transition criteria

	/**
	 * Request that the registry backed by this locator instantiate the transition
	 * criteria specified in encoded form, using the given autowire policy.
	 * Note: not all registries may support this advanced feature (Spring does
	 * though ;-)).
	 * @param encodedCriteria the encoded representation of the criteria
	 * @param autowireMode the autowire policy
	 * @return the instantiated (and possibly autowired) transition criteria
	 * @throws ServiceLookupException when the transition criteria object
	 *         cannot be created
	 */
	public TransitionCriteria createTransitionCriteria(String encodedCriteria, AutowireMode autowireMode)
			throws ServiceLookupException;

	// dealing with actions

	/**
	 * Request that the registry backed by this locator instantiate the action
	 * of the specified implementation class, using the given autowire policy.
	 * Note: not all registries may support this advanced feature (Spring does
	 * though ;-)).
	 * @param implementationClass the action implementation class
	 * @param autowireMode the autowire policy
	 * @return the instantiated (and possibly autowired) action
	 * @throws ServiceLookupException when the action cannot be created
	 */
	public Action createAction(Class implementationClass, AutowireMode autowireMode) throws ServiceLookupException;

	/**
	 * Lookup an action with specified id.
	 * @param id the action id
	 * @return the action
	 * @throws ServiceLookupException when the action cannot be found
	 */
	public Action getAction(String id) throws ServiceLookupException;

	/**
	 * Lookup an action of specified implementation class.
	 * @param implementationClass the required implementation class
	 * @return the action
	 * @throws ServiceLookupException when the action cannot be found
	 */
	public Action getAction(Class implementationClass) throws ServiceLookupException;

	// dealing with attribute mappers
	
	/**
	 * Request that the registry backed by this locator instantiate the flow
	 * attribute mapper of the specified implementation class, using the given
	 * autowire policy. Note: not all registries may support this advanced
	 * feature (Spring does though ;-)).
	 * @param attributeMapperImplementationClass the implementation class
	 * @param autowireMode the autowire policy
	 * @return the instantiated (and possibly autowired) attribute mapper
	 */
	public FlowAttributeMapper createFlowAttributeMapper(Class attributeMapperImplementationClass,
			AutowireMode autowireMode) throws ServiceLookupException;

	/**
	 * Lookup a flow model mapper with specified id.
	 * @param id the flow model mapper id
	 * @return the flow model mapper
	 * @throws ServiceLookupException when the flow model mapper cannot be found
	 */
	public FlowAttributeMapper getFlowAttributeMapper(String id) throws ServiceLookupException;

	/**
	 * Lookup a flow model mapper of specified implementation class.
	 * @param implementationClass the required implementation class
	 * @return the flow model mapper
	 * @throws ServiceLookupException when the flow model mapper cannot be found
	 */
	public FlowAttributeMapper getFlowAttributeMapper(Class implementationClass)
			throws ServiceLookupException;
	
}