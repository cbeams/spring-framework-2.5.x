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

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
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

	public SubFlowState(String subFlowId, Transition transition) {
		this(subFlowId, subFlowId, null, new Transition[] { transition });
	}

	public SubFlowState(String subFlowId, Flow subFlow, Transition transition) {
		this(subFlowId, subFlow, new Transition[] { transition });
	}

	public SubFlowState(String subFlowId, Transition[] transitions) {
		this(subFlowId, subFlowId, null, transitions);
	}

	public SubFlowState(String subFlowId, Flow subFlow, Transition[] transitions) {
		this(subFlowId, subFlow, null, transitions);
	}

	public SubFlowState(String subFlowId, String attributesMapperId, Transition transition) {
		this(subFlowId, subFlowId, attributesMapperId, new Transition[] { transition });
	}

	public SubFlowState(String id, String subFlowId, String attributesMapperId, Transition transition) {
		this(id, subFlowId, attributesMapperId, new Transition[] { transition });
	}

	public SubFlowState(String subFlowId, Flow subFlow, FlowAttributesMapper attributesMapper, Transition transition) {
		this(subFlowId, subFlow, attributesMapper, new Transition[] { transition });
	}

	public SubFlowState(String subFlowId, String attributesMapperId, Transition[] transitions) {
		this(subFlowId, subFlowId, attributesMapperId, transitions);
	}

	public SubFlowState(String id, String subFlowId, String attributesMapperId, Transition[] transitions) {
		super(id);
		Assert.hasText(subFlowId, "The id of this subflow state is required");
		this.subFlowId = subFlowId;
		this.attributesMapperId = attributesMapperId;
		addAll(transitions);
	}

	public SubFlowState(String id, Flow subFlow, FlowAttributesMapper attributesMapper, Transition[] transitions) {
		super(id);
		Assert.hasText(subFlowId, "The id of this subflow state is required");
		this.subFlow = subFlow;
		this.attributesMapper = attributesMapper;
		addAll(transitions);
	}

	public boolean isSubFlowState() {
		return true;
	}

	protected Flow getSubFlow(Flow flow) throws NoSuchFlowDefinitionException {
		if (this.subFlow != null) {
			return this.subFlow;
		}
		else {
			try {
				Flow subFlow = flow.getFlowDao().getFlow(this.subFlowId);
				Assert.notNull(subFlow, "The subflow is required");
				return subFlow;
			}
			catch (NoSuchBeanDefinitionException e) {
				throw new NoSuchFlowDefinitionException(this.subFlowId, e);
			}
		}
	}

	protected FlowAttributesMapper getAttributesMapper(Flow flow) throws NoSuchFlowAttributeMapperException {
		if (this.attributesMapper != null) {
			return this.attributesMapper;
		}
		if (!StringUtils.hasText(this.attributesMapperId)) {
			return null;
		}
		try {
			return flow.getFlowDao().getFlowAttributesMapper(this.attributesMapperId);
		}
		catch (NoSuchBeanDefinitionException e) {
			throw new NoSuchFlowAttributeMapperException(flow, this, e);
		}
	}

	protected ViewDescriptor doEnterState(Flow flow, FlowSessionExecutionStack sessionExecution,
			HttpServletRequest request, HttpServletResponse response) {
		if (logger.isDebugEnabled()) {
			logger.debug("Retrieving sub flow definition with id '" + this.subFlowId + "'");
		}
		Flow subFlow = getSubFlow(flow);
		if (logger.isInfoEnabled()) {
			if (!subFlow.getId().equals(this.subFlowId)) {
				logger.info("The subflow definition exported in the context under ID '" + this.subFlowId + "' has ID '"
						+ subFlow.getId() + "' -- these ID values are NOT equal; is this OK?");
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Spawning child sub flow '" + subFlow.getId() + "' within this flow '" + flow.getId() + "'");
		}
		Map subFlowAttributes;
		if (getAttributesMapper(flow) != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Messaging the configured attributes mapper to map parent-flow attributes "
						+ "down to the spawned subflow for access within the subflow");
			}
			subFlowAttributes = getAttributesMapper(flow).createSpawnedSubFlowAttributesMap(sessionExecution);
		}
		else {
			if (logger.isInfoEnabled()) {
				logger.info("No attributes mapper is configured for this subflow state '" + getId()
						+ "' - note: as a result, no attributes in the parent flow '" + flow.getId()
						+ "' scope will be passed to the spawned subflow '" + subFlow.getId() + "'");
			}
			subFlowAttributes = new HashMap(1);
		}
		return subFlow.spawnIn(sessionExecution, request, response, subFlowAttributes);
	}

}