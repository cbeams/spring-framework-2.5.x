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
 * A base super class for a state definition, associtable with any number of
 * Flow definitions. Types of states include action states, view states, subflow
 * states, and end states.
 * 
 * @author Keith Donald
 */
public abstract class AbstractState implements Serializable {

    protected final Log logger = LogFactory.getLog(getClass());

    private String id;

    public AbstractState(String id) {
        Assert.hasText(id, "The state must have a valid identifier");
        this.id = id;
    }

    public boolean isTransitionable() {
        return false;
    }

    public boolean isViewState() {
        return false;
    }

    public boolean isActionState() {
        return false;
    }

    public boolean isSubFlowState() {
        return false;
    }

    public boolean isEndState() {
        return false;
    }

    public String getId() {
        return id;
    }

    public boolean equals(Object o) {
        if (!(o instanceof AbstractState)) {
            return false;
        }
        AbstractState s = (AbstractState)o;
        return id.equals(s.id);
    }

    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Requesting entering of this state for the active (currently executing)
     * flow session.
     * @param flow The flow definition associated with the executing flow
     *        session
     * @param sessionExecutionStack The session execution stack, tracking the
     *        current active flow session
     * @param request The client http request
     * @param response The server http response
     * @return A view descriptor containing model and view information needed to
     *         render the results of the event execution.
     */
    public final ViewDescriptor enter(Flow flow, FlowSessionExecutionStack sessionExecutionStack,
            HttpServletRequest request, HttpServletResponse response) {
        AbstractState oldState = null;
        if (sessionExecutionStack.getCurrentStateId() != null) {
            oldState = flow.getRequiredState(sessionExecutionStack.getCurrentStateId());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Entering state '" + this + "' in flow '" + flow.getId() + "'");
        }
        sessionExecutionStack.setCurrentState(getId());
        // Publish state transition event if necessary
        if (flow.getFlowLifecycleListener() != null) {
            flow.getFlowLifecycleListener().flowStateTransitioned(flow, oldState, this, sessionExecutionStack, request);
        }
        return doEnterState(flow, sessionExecutionStack, request, response);
    }

    /**
     * Hook method to do any processing as a result of entering this state.
     * @param flow The flow definition associated with the executing flow
     *        session
     * @param sessionExecutionStack The session execution stack, tracking the
     *        current active flow session
     * @param request The client http request
     * @param response The server http response
     * @return A view descriptor containing model and view information needed to
     *         render the results of the event execution.
     */
    protected abstract ViewDescriptor doEnterState(Flow flow, FlowSessionExecutionStack sessionExecutionStack,
            HttpServletRequest request, HttpServletResponse response);

    public String toString() {
        return new ToStringCreator(this).append("id", id).toString();
    }

}