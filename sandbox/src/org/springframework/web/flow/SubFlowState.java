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

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;
import org.springframework.web.flow.config.NoSuchFlowDefinitionException;
import org.springframework.web.servlet.ModelAndView;

/**
 * State that executes a sub flow.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class SubFlowState extends TransitionableState implements FlowAttributesMapper {

	private Flow subFlow;

	private FlowAttributesMapper flowAttributesMapper;

	public SubFlowState(Flow flow, String id, Flow subFlow, Transition transition) {
		this(flow, id, subFlow, new Transition[] { transition });
	}

	public SubFlowState(Flow flow, String id, Flow subFlow, Transition[] transitions) {
		this(flow, id, subFlow, null, transitions);
	}

	public SubFlowState(Flow flow, String id, Flow subFlow, FlowAttributesMapper attributesMapper, Transition transition) {
		this(flow, id, subFlow, attributesMapper, new Transition[] { transition });
	}

	public SubFlowState(Flow flow, String id, Flow subFlow, FlowAttributesMapper attributesMapper,
			Transition[] transitions) {
		super(flow, id, transitions);
		setSubFlow(subFlow);
		setFlowAttributesMapper(attributesMapper);
	}

	protected void setSubFlow(Flow subFlow) {
		Assert.notNull(subFlow, "A sub flow state must have a sub flow");
		this.subFlow = subFlow;
	}

	protected Flow getSubFlow() throws NoSuchFlowDefinitionException {
		return this.subFlow;
	}

	protected void setFlowAttributesMapper(FlowAttributesMapper attributesMapper) {
		this.flowAttributesMapper = attributesMapper;
	}

	protected FlowAttributesMapper getFlowAttributesMapper() {
		return this.flowAttributesMapper;
	}

	public boolean isSubFlowState() {
		return true;
	}

	protected ModelAndView doEnterState(FlowExecutionStack flowExecution, HttpServletRequest request,
			HttpServletResponse response) {
		Flow subFlow = getSubFlow();
		if (logger.isDebugEnabled()) {
			logger.debug("Spawning child sub flow '" + subFlow.getId() + "' within this flow '"
					+ flowExecution.getActiveFlowId() + "'");
		}
		Map subFlowInput = createSubFlowInputAttributes(flowExecution);
		return flowExecution.spawn(getSubFlow(), subFlowInput, request, response);
	}

	public Map createSubFlowInputAttributes(AttributesAccessor parentFlowModel) {
		if (getFlowAttributesMapper() != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Messaging the configured attributes mapper to map parent-flow attributes "
						+ "down to the spawned subflow for access within the subflow");
			}
			return this.flowAttributesMapper.createSubFlowInputAttributes(parentFlowModel);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger
						.debug("No attributes mapper configured for this subflow state '"
								+ getId()
								+ "'; as a result, no attributes in the parent flow scope will be passed to the spawned subflow '"
								+ subFlow.getId() + "'");
			}
			return Collections.EMPTY_MAP;
		}
	}

	public void mapSubFlowOutputAttributes(AttributesAccessor subFlowModel, MutableAttributesAccessor parentFlowModel) {
		if (getFlowAttributesMapper() != null) {
			if (logger.isDebugEnabled()) {
				logger
						.debug("Messaging the configured attributes mapper to map subflow attributes back up to the resuming parent flow - "
								+ "the resuming parent flow will now have access to attributes passed up by the completed subflow");
			}
			this.flowAttributesMapper.mapSubFlowOutputAttributes(subFlowModel, parentFlowModel);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger
						.debug("No attributes mapper is configured for the resuming state '"
								+ getId()
								+ "' - note: as a result, no attributes in the ending subflow scope will be passed to the resuming parent flow");
			}
		}
	}
}