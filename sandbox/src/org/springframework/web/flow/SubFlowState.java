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

import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.AttributeSource;
import org.springframework.binding.MutableAttributeSource;
import org.springframework.util.Assert;

/**
 * State that spawns a subflow when executed.
 * <p>
 * A sub flow state has the ability to map data between the parent and sub flow.
 * See the {@link FlowAttributeMapper} interface definition for info on how to
 * do this.
 * 
 * @see org.springframework.web.flow.FlowAttributeMapper
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class SubFlowState extends TransitionableState implements FlowAttributeMapper {

	/**
	 * The subflow that should be spawned when this subflow state is entered.
	 */
	private Flow subFlow;

	/**
	 * The attribute mapper that should map attributes from the parent flow down
	 * to the spawned subflow and visa versa.
	 */
	private FlowAttributeMapper flowAttributeMapper;

	/**
	 * Create a new sub flow state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param subFlow the sub flow to spawn
	 * @param transition the sole transition of this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public SubFlowState(Flow flow, String id, Flow subFlow, Transition transition) throws IllegalArgumentException {
		this(flow, id, subFlow, new Transition[] { transition });
	}

	/**
	 * Create a new sub flow state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param subFlow the sub flow to spawn
	 * @param transitions the transitions of this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public SubFlowState(Flow flow, String id, Flow subFlow, Transition[] transitions) throws IllegalArgumentException {
		this(flow, id, subFlow, null, transitions);
	}

	/**
	 * Create a new sub flow state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param subFlow the sub flow to spawn
	 * @param attributeMapper the attribute mapper to use
	 * @param transition the sole transition of this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public SubFlowState(Flow flow, String id, Flow subFlow, FlowAttributeMapper attributeMapper, Transition transition)
			throws IllegalArgumentException {
		this(flow, id, subFlow, attributeMapper, new Transition[] { transition });
	}

	/**
	 * Create a new sub flow state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param subFlow the sub flow to spawn
	 * @param attributeMapper the attribute mapper to use
	 * @param transitions the transitions of this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public SubFlowState(Flow flow, String id, Flow subFlow, FlowAttributeMapper attributeMapper,
			Transition[] transitions) throws IllegalArgumentException {
		super(flow, id, transitions);
		setSubFlow(subFlow);
		setFlowAttributeMapper(attributeMapper);
	}

	/**
	 * Set the sub flow that will be spawned by this state.
	 * @param subFlow the sub flow to spawn
	 */
	protected void setSubFlow(Flow subFlow) {
		Assert.notNull(subFlow, "A sub flow state must have a sub flow");
		this.subFlow = subFlow;
	}

	/**
	 * Returns the sub flow spawned by this state.
	 */
	public Flow getSubFlow() {
		return this.subFlow;
	}

	/**
	 * Set the attribute mapper to use to map model data between parent and sub
	 * flow model. Can be null if no mapping is needed.
	 */
	protected void setFlowAttributeMapper(FlowAttributeMapper attributeMapper) {
		this.flowAttributeMapper = attributeMapper;
	}

	/**
	 * Returns the attribute mapper used to map data between parent and sub flow
	 * model, or null if no mapping is needed.
	 */
	public FlowAttributeMapper getFlowAttributeMapper() {
		return this.flowAttributeMapper;
	}

	/**
	 * Specialization of State's <code>doEnterState</code> template
	 * method that executes behaivior specific to this state type in polymorphic
	 * fashion.
	 * <p>
	 * Enter this state, creating the sub flow input map and spawning the sub
	 * flow in the current flow execution.
	 */
	protected ViewDescriptor doEnterState(StateContext context) {
		Flow subFlow = getSubFlow();
		if (logger.isDebugEnabled()) {
			logger.debug("Spawning child sub flow '" + subFlow.getId() + "' within this flow '" + getFlow() + "'");
		}
		return context.spawn(getSubFlow(), createSubFlowInputAttributes(context.getFlowScope()));
	}

	public Map createSubFlowInputAttributes(AttributeSource parentFlowAttributes) {
		if (getFlowAttributeMapper() != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Messaging the configured attribute mapper to map parent-flow attributes "
						+ "down to the spawned sub flow for access within the sub flow");
			}
			return this.flowAttributeMapper.createSubFlowInputAttributes(parentFlowAttributes);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("No attribute mapper configured for this sub flow state '"
						+ getId()
						+ "' -- as a result, no attributes in the parent flow scope will be passed to the spawned sub flow '"
						+ subFlow.getId() + "'");
			}
			return new HashMap();
		}
	}

	public void mapSubFlowOutputAttributes(AttributeSource subFlowAttributes, MutableAttributeSource parentFlowAttributes) {
		if (getFlowAttributeMapper() != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Messaging the configured attribute mapper to map sub flow attributes back up to the resuming parent flow -- "
						+ "the resuming parent flow will now have access to attributes passed up by the completed sub flow");
			}
			this.flowAttributeMapper.mapSubFlowOutputAttributes(subFlowAttributes, parentFlowAttributes);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("No attribute mapper is configured for the resuming state '"
						+ getId()
						+ "' -- note: as a result, no attributes in the ending sub flow scope will be passed to the resuming parent flow");
			}
		}
	}
}