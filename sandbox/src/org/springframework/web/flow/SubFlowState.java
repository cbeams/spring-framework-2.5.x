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

	public SubFlowState(String id, Transition transition) {
		this(id, id, null, new Transition[] { transition });
	}

	public SubFlowState(Flow subFlow, Transition transition) {
		this(subFlow.getId(), subFlow, new Transition[] { transition });
	}

	public SubFlowState(String id, Flow subFlow, Transition transition) {
		this(id, subFlow, new Transition[] { transition });
	}

	public SubFlowState(String id, Transition[] transitions) {
		this(id, id, null, transitions);
	}

	public SubFlowState(Flow subFlow, Transition[] transitions) {
		this(subFlow.getId(), subFlow, null, transitions);
	}

	public SubFlowState(String id, Flow subFlow, Transition[] transitions) {
		this(id, subFlow, null, transitions);
	}

	public SubFlowState(String id, String attributesMapperId, Transition transition) {
		this(id, id, attributesMapperId, new Transition[] { transition });
	}

	public SubFlowState(String id, String subFlowId, String attributesMapperId, Transition transition) {
		this(id, subFlowId, attributesMapperId, new Transition[] { transition });
	}

	public SubFlowState(Flow subFlow, FlowAttributesMapper attributesMapper, Transition transition) {
		this(subFlow.getId(), subFlow, attributesMapper, new Transition[] { transition });
	}

	public SubFlowState(String id, Flow subFlow, FlowAttributesMapper attributesMapper, Transition transition) {
		this(id, subFlow, attributesMapper, new Transition[] { transition });
	}

	public SubFlowState(String id, String attributesMapperId, Transition[] transitions) {
		this(id, id, attributesMapperId, transitions);
	}

	public SubFlowState(String id, String subFlowId, String attributesMapperId, Transition[] transitions) {
		super(id);
		Assert.hasText(subFlowId, "The id of thr subflow definition is required");
		this.subFlowId = subFlowId;
		this.attributesMapperId = attributesMapperId;
		addAll(transitions);
	}

	public SubFlowState(Flow subFlow, FlowAttributesMapper attributesMapper, Transition[] transitions) {
		this(subFlow.getId(), subFlow, attributesMapper, transitions);
	}

	public SubFlowState(String id, Flow subFlow, FlowAttributesMapper attributesMapper, Transition[] transitions) {
		super(id);
		Assert.notNull(subFlow, "The subflow definition instance is required");
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
				Assert.notNull(subFlow, "The subflow retrieved must be non-null");
				if (logger.isInfoEnabled()) {
					if (!subFlow.getId().equals(this.subFlowId)) {
						logger.info("The subflow definition exported in the registry under id '" + this.subFlowId + "' has an id of '"
								+ subFlow.getId() + "' -- these ids are NOT equal; is this OK?");
					}
				}
				return subFlow;
			}
			catch (NoSuchBeanDefinitionException e) {
				throw new NoSuchFlowDefinitionException(this.subFlowId, e);
			}
		}
	}

	protected FlowAttributesMapper getAttributesMapper(Flow flow) throws NoSuchFlowAttributesMapperException {
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
			throw new NoSuchFlowAttributesMapperException(flow, this, e);
		}
	}

	/**
	 * @return
	 */
	protected String getAttributesMapperId() {
		return attributesMapperId;
	}

	protected ViewDescriptor doEnterState(Flow flow, FlowSessionExecutionStack sessionExecution,
			HttpServletRequest request, HttpServletResponse response) {
		if (logger.isDebugEnabled()) {
			logger.debug("Retrieving sub flow definition with id '" + this.subFlowId + "'");
		}
		Flow subFlow = getSubFlow(flow);
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