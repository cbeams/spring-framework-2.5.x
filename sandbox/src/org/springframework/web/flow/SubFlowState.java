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

import org.springframework.web.flow.config.NoSuchFlowAttributesMapperException;
import org.springframework.web.flow.config.NoSuchFlowDefinitionException;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Keith Donald
 */
public class SubFlowState extends TransitionableState {

	private Flow subFlow;

	private FlowAttributesMapper attributesMapper;

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
		super(flow, id);
		setSubFlow(subFlow);
		setAttributesMapper(attributesMapper);
		addAll(transitions);
	}

	protected void setSubFlow(Flow subFlow) {
		this.subFlow = subFlow;
	}

	protected Flow getSubFlow() throws NoSuchFlowDefinitionException {
		return this.subFlow;
	}

	protected void setAttributesMapper(FlowAttributesMapper attributesMapper) {
		this.attributesMapper = attributesMapper;
	}

	protected FlowAttributesMapper getAttributesMapper() throws NoSuchFlowAttributesMapperException {
		return this.attributesMapper;
	}

	public boolean isSubFlowState() {
		return true;
	}

	protected ModelAndView doEnterState(FlowExecutionStack sessionExecution, HttpServletRequest request,
			HttpServletResponse response) {
		Flow subFlow = getSubFlow();
		if (logger.isDebugEnabled()) {
			logger.debug("Spawning child sub flow '" + subFlow.getId() + "' within this flow '"
					+ sessionExecution.getActiveFlowId() + "'");
		}
		Map subFlowAttributes;
		if (getAttributesMapper() != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Messaging the configured attributes mapper to map parent-flow attributes "
						+ "down to the spawned subflow for access within the subflow");
			}
			subFlowAttributes = getAttributesMapper().createSpawnedSubFlowAttributesMap(sessionExecution);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("No attributes mapper is configured for this subflow state '" + getId()
						+ "'; as a result, no attributes in the parent flow '" + sessionExecution.getActiveFlowId()
						+ "' scope will be passed to the spawned subflow '" + subFlow.getId() + "'");
			}
			subFlowAttributes = new HashMap(1);
		}
		return subFlow.spawnIn(sessionExecution, request, response, subFlowAttributes);
	}
}