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

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.ToStringCreator;

/**
 * @author Keith Donald
 */
public class Transition implements Serializable {
    private static final Log logger = LogFactory.getLog(Transition.class);

    private String id;

    private String toState;

    public Transition(String id, String toState) {
        Assert.notNull(id, "The id is required");
        Assert.notNull(toState, "The state is required");
        this.id = id;
        this.toState = toState;
    }

    public String getId() {
        return id;
    }

    public String getToState() {
        return toState;
    }

    public ViewDescriptor execute(Flow flow, FlowSessionExecutionStack sessionExecutionStack,
            HttpServletRequest request, HttpServletResponse response) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing transition from state '" + sessionExecutionStack.getCurrentStateId()
                    + "' to state '" + getToState() + "' in flow '" + flow.getId() + "'");
        }
        ViewDescriptor descriptor = flow.getRequiredState(getToState()).enter(flow, sessionExecutionStack, request,
                response);
        return descriptor;

    }

    public String toString() {
        return new ToStringCreator(this).append("id", id).append("toState", toState).toString();
    }
}