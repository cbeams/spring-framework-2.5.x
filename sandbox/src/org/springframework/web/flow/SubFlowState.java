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
package org.springframework.web.flow;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;

/**
 * State that spawns a subflow when executed.
 * <p>
 * A sub flow state has the ability to map data between the parent and sub flow
 * models. See the {@link FlowModelMapper} interface definition for info on
 * how to do this.
 * 
 * @see org.springframework.web.flow.FlowModelMapper
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class SubFlowState extends TransitionableState implements FlowModelMapper {

	private Flow subFlow;

	private FlowModelMapper flowModelMapper;

	/**
	 * Create a new sub flow state.
	 * @param flow The owning flow
	 * @param id The state identifier (must be unique to the flow)
	 * @param subFlow The sub flow to spawn
	 * @param transition The sole transition of this state
	 * @throws IllegalArgumentException When this state cannot be added to given
	 *         flow
	 */
	public SubFlowState(Flow flow, String id, Flow subFlow, Transition transition) throws IllegalArgumentException {
		this(flow, id, subFlow, new Transition[] { transition });
	}

	/**
	 * Create a new sub flow state.
	 * @param flow The owning flow
	 * @param id The state identifier (must be unique to the flow)
	 * @param subFlow The sub flow to spawn
	 * @param transitions The transitions of this state
	 * @throws IllegalArgumentException When this state cannot be added to given
	 *         flow
	 */
	public SubFlowState(Flow flow, String id, Flow subFlow, Transition[] transitions) throws IllegalArgumentException {
		this(flow, id, subFlow, null, transitions);
	}

	/**
	 * Create a new sub flow state.
	 * @param flow The owning flow
	 * @param id The state identifier (must be unique to the flow)
	 * @param subFlow The sub flow to spawn
	 * @param modelMapper The model mapper to use
	 * @param transition The sole transition of this state
	 * @throws IllegalArgumentException When this state cannot be added to given
	 *         flow
	 */
	public SubFlowState(Flow flow, String id, Flow subFlow, FlowModelMapper modelMapper, Transition transition)
			throws IllegalArgumentException {
		this(flow, id, subFlow, modelMapper, new Transition[] { transition });
	}

	/**
	 * Create a new sub flow state.
	 * @param flow The owning flow
	 * @param id The state identifier (must be unique to the flow)
	 * @param subFlow The sub flow to spawn
	 * @param modelMapper The model mapper to use
	 * @param transitions The transitions of this state
	 * @throws IllegalArgumentException When this state cannot be added to given
	 *         flow
	 */
	public SubFlowState(Flow flow, String id, Flow subFlow, FlowModelMapper modelMapper,
			Transition[] transitions) throws IllegalArgumentException {
		super(flow, id, transitions);
		setSubFlow(subFlow);
		setFlowModelMapper(modelMapper);
	}

	/**
	 * Set the sub flow that will be spawned by this state.
	 * @param subFlow The sub flow to spawn
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
	 * Set the model mapper to use to map model data between parent and
	 * sub flow model. Can be null if no mapping is needed.
	 */
	protected void setFlowModelMapper(FlowModelMapper modelMapper) {
		this.flowModelMapper = modelMapper;
	}

	/**
	 * Returns the model mapper used to map data between parent and sub
	 * flow model, or null if no mapping is needed.
	 */
	public FlowModelMapper getFlowModelMapper() {
		return this.flowModelMapper;
	}

	/**
	 * Enter this state, creating the sub flow input map and spawning the sub
	 * flow in the current flow execution.
	 */
	protected ModelAndView doEnterState(FlowExecutionStack flowExecution, HttpServletRequest request,
			HttpServletResponse response) {
		Flow subFlow = getSubFlow();
		if (logger.isDebugEnabled()) {
			logger.debug("Spawning child sub flow '" + subFlow.getId() + "' within this flow '" + getFlow() + "'");
		}
		Map subFlowInput = createSubFlowInputAttributes(flowExecution);
		return flowExecution.spawn(getSubFlow(), subFlowInput, request, response);
	}

	public Map createSubFlowInputAttributes(FlowModel parentFlowModel) {
		if (getFlowModelMapper() != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Messaging the configured model mapper to map parent-flow attributes "
						+ "down to the spawned sub flow for access within the sub flow");
			}
			return this.flowModelMapper.createSubFlowInputAttributes(parentFlowModel);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger
						.debug("No model mapper configured for this sub flow state '"
								+ getId()
								+ "'; as a result, no attributes in the parent flow scope will be passed to the spawned sub flow '"
								+ subFlow.getId() + "'");
			}
			return new HashMap(1);
		}
	}

	public void mapSubFlowOutputAttributes(FlowModel subFlowModel, MutableFlowModel parentFlowModel) {
		if (getFlowModelMapper() != null) {
			if (logger.isDebugEnabled()) {
				logger
						.debug("Messaging the configured model mapper to map sub flow attributes back up to the resuming parent flow - "
								+ "the resuming parent flow will now have access to attributes passed up by the completed sub flow");
			}
			this.flowModelMapper.mapSubFlowOutputAttributes(subFlowModel, parentFlowModel);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger
						.debug("No model mapper is configured for the resuming state '"
								+ getId()
								+ "' - note: as a result, no attributes in the ending sub flow scope will be passed to the resuming parent flow");
			}
		}
	}
}