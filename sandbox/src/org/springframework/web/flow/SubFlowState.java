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
import org.springframework.util.StringUtils;

/**
 * @author Keith Donald
 */
public class SubFlowState extends TransitionableState {

	private String subFlowId;

	private Flow subFlow;

	private String attributesMapperId;

	private FlowAttributesMapper attributesMapper;

	public SubFlowState(Flow flow, String id, String subFlowId, Transition transition) {
		this(flow, id, subFlowId, null, new Transition[] { transition });
	}

	public SubFlowState(Flow flow, String id, Flow subFlow, Transition transition) {
		this(flow, id, subFlow, new Transition[] { transition });
	}

	public SubFlowState(Flow flow, String id, String subFlowId, Transition[] transitions) {
		this(flow, id, subFlowId, null, transitions);
	}

	public SubFlowState(Flow flow, String id, Flow subFlow, Transition[] transitions) {
		this(flow, id, subFlow, null, transitions);
	}

	public SubFlowState(Flow flow, String id, String subFlowId, String attributesMapperId, Transition transition) {
		this(flow, id, subFlowId, attributesMapperId, new Transition[] { transition });
	}

	public SubFlowState(Flow flow, String id, Flow subFlow, FlowAttributesMapper attributesMapper, Transition transition) {
		this(flow, id, subFlow, attributesMapper, new Transition[] { transition });
	}

	public SubFlowState(Flow flow, String id, String subFlowId, String attributesMapperId, Transition[] transitions) {
		super(flow, id);
		Assert.hasText(subFlowId, "The id of the subflow definition is required");
		setSubFlowId(subFlowId);
		setAttributesMapperId(attributesMapperId);
		addAll(transitions);
	}

	public SubFlowState(Flow flow, String id, Flow subFlow, FlowAttributesMapper attributesMapper,
			Transition[] transitions) {
		super(flow, id);
		Assert.notNull(subFlow, "The subflow definition instance is required");
		setSubFlow(subFlow);
		setAttributesMapper(attributesMapper);
		addAll(transitions);
	}

	public void setSubFlow(Flow subFlow) {
		this.subFlow = subFlow;
	}

	public void setAttributesMapper(FlowAttributesMapper attributesMapper) {
		this.attributesMapper = attributesMapper;
	}

	public void setSubFlowId(String subFlowId) {
		this.subFlowId = subFlowId;
	}

	public void setAttributesMapperId(String attributesMapperId) {
		this.attributesMapperId = attributesMapperId;
	}

	public boolean isSubFlowState() {
		return true;
	}

	protected String getAttributesMapperId() {
		return attributesMapperId;
	}

	protected ViewDescriptor doEnterState(FlowSessionExecutionStack sessionExecution, HttpServletRequest request,
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

	protected Flow getSubFlow() throws NoSuchFlowDefinitionException {
		if (this.subFlow != null) {
			return this.subFlow;
		}
		else {
			Assert.notNull(this.subFlowId, "The subflow id is required");
			if (logger.isDebugEnabled()) {
				logger.debug("Retrieving sub flow definition with id '" + this.subFlowId + "'");
			}
			Flow subFlow = getFlowServiceLocator().getFlow(this.subFlowId);
			Assert.notNull(subFlow, "The subflow retrieved must be non-null");
			if (logger.isInfoEnabled()) {
				if (!subFlow.getId().equals(this.subFlowId)) {
					logger.info("The subflow definition exported in the registry under id '" + this.subFlowId
							+ "' has an id of '" + subFlow.getId() + "' -- these ids are NOT equal; is this OK?");
				}
			}
			return subFlow;
		}
	}

	protected FlowAttributesMapper getAttributesMapper() throws NoSuchFlowAttributesMapperException {
		if (this.attributesMapper != null) {
			return this.attributesMapper;
		}
		if (!StringUtils.hasText(this.attributesMapperId)) {
			return null;
		}
		return getFlowServiceLocator().getFlowAttributesMapper(this.attributesMapperId);
	}
}