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
import org.springframework.util.StringUtils;

/**
 * @author Keith Donald
 */
public class SubFlowState extends TransitionableState {

    private String subFlowId;

    private String attributesMapperId;

    public SubFlowState(String subFlowId, Transition transition) {
        this(subFlowId, subFlowId, null, new Transition[] { transition });
    }

    public SubFlowState(String subFlowId, Transition[] transitions) {
        this(subFlowId, subFlowId, null, transitions);
    }

    public SubFlowState(String subFlowId, String attributesMapperId, Transition transition) {
        this(subFlowId, subFlowId, attributesMapperId, new Transition[] { transition });
    }

    public SubFlowState(String subFlowId, String attributesMapperId, Transition[] transitions) {
        this(subFlowId, subFlowId, attributesMapperId, transitions);
    }

    public SubFlowState(String id, String subFlowId, String attributesMapperId, Transition transition) {
        this(id, subFlowId, attributesMapperId, new Transition[] { transition });
    }

    public SubFlowState(String id, String subFlowId, String attributesMapperId, Transition[] transitions) {
        super(id);
        Assert.hasText(subFlowId, "The id of the subflow is required");
        this.subFlowId = subFlowId;
        this.attributesMapperId = attributesMapperId;
        addAll(transitions);
    }

    public boolean isSubFlowState() {
        return false;
    }

    protected SubFlowAttributesMapper getAttributesMapper(FlowDao flowDao) {
        if (!StringUtils.hasText(attributesMapperId)) {
            return null;
        }
        return flowDao.getSubFlowAttributesMapper(attributesMapperId);
    }

    protected ViewDescriptor doEnterState(Flow flow, FlowSessionExecutionStack sessionExecutionStack,
            HttpServletRequest request, HttpServletResponse response) {
        Flow subFlow = flow.getFlowDao().getFlow(subFlowId);
        if (logger.isDebugEnabled()) {
            logger.debug("Spawning child sub flow '" + subFlow.getId() + "' within this flow '" + flow.getId() + "'");
        }
        Map subFlowAttributes;
        if (getAttributesMapper(flow.getFlowDao()) != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Messaging the configured attributes mapper to map parent-flow attributes "
                        + "down to the spawned subflow for access within the subflow");
            }
            subFlowAttributes = getAttributesMapper(flow.getFlowDao()).createSpawnedSubFlowAttributesMap(
                    sessionExecutionStack);
        }
        else {
            if (logger.isInfoEnabled()) {
                logger.info("No attributes mapper is configured for this subflow state '" + getId()
                        + "' - note: as a result, no attributes in the parent flow '" + flow.getId()
                        + "' scope will be passed to the spawned subflow '" + subFlow.getId() + "'");
            }
            subFlowAttributes = Collections.EMPTY_MAP;
        }
        return subFlow.start(sessionExecutionStack, request, response, subFlowAttributes);
    }

}