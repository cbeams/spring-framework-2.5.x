/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
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