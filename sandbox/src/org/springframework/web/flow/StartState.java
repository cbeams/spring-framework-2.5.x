/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */
package org.springframework.web.flow;

import java.io.Serializable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.DefaultObjectStyler;

/**
 * @author Keith Donald
 */
public class StartState implements Serializable {
    private static final Log logger = LogFactory.getLog(StartState.class);

    private TransitionableState state;

    public StartState(TransitionableState state) {
        Assert.notNull(state, "The start state is required");
        this.state = state;
    }

    protected TransitionableState getState() {
        return state;
    }

    public ViewDescriptor enter(Flow flow, FlowSessionExecutionStack sessionExecutionStack, HttpServletRequest request,
            HttpServletResponse response, Map inputAttributes) {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting new session for flow '" + flow.getId() + "' in start state '" + getState()
                    + "' with input attributes '" + DefaultObjectStyler.call(inputAttributes) + "'");
        }
        sessionExecutionStack.push(flow.createSession(inputAttributes));
        if (flow.isLifecycleListenerSet()) {
            flow.getLifecycleListener().flowStarted(flow, sessionExecutionStack, request);
        }
        return getState().enter(flow, sessionExecutionStack, request, response);
    }

}