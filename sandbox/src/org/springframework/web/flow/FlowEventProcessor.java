/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */
package org.springframework.web.flow;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles flow events signaled by the web-tier in response to web-controller
 * requests. There are two major different types of event processing operations:
 * <p>
 * <ol>
 * <li>1. A start operation, requesting the creation of a new session for the
 * Flow definition associated with this processor. A start operation transitions
 * the new flow session to its starting state.
 * <li>2. An event execution operation, signaling the occurence of a event in
 * the current state of an existing flow, submitting it for processing.
 * Processing the event occurence causes the current state of the flow to
 * transition to a new state.
 * 
 * This interface is everything web-tier controllers need to inteface with the
 * web flow system. This is their entry point.
 * 
 * @author Keith Donald
 */
public interface FlowEventProcessor {

    /**
     * Start a new session for this flow. This will cause the flow session to
     * enter its start state.
     * 
     * @param sessionExecutionStack The session execution stack, tracking any
     *        suspended parent flows that spawned this flow (as a subflow)
     * @param request the client http request
     * @param response the server http response
     * @param input optional input attributes to be passed to the new flow session,
     *        placed in 'flow scope'
     * @return A view descriptor containing model and view information needed to
     *         render the results of the start event execution.
     * @throws IllegalStateException if the event processor has not been
     *         configured with a valid start state.
     */
    public ViewDescriptor start(FlowSessionExecutionStack sessionExecutionStack, HttpServletRequest request,
            HttpServletResponse response, Map input) throws IllegalStateException;

    /**
     * Execute the event identified by <code>eventId</code> in the state
     * identified by <code>stateId</code>
     * 
     * @param eventId The id of the event to execute (e.g 'submit', 'next', 'back')
     * @param stateId The id of the state to execute this event in (e.g 'viewDetails')
     * @param sessionExecutionStack The session execution stack, tracking any
     *        suspended parent flows that spawned this flow (as a subflow)
     * @param request the client http request
     * @param response the server http response
     * @return A view descriptor containing model and view information needed to
     *         render the results of the event execution.
     * @throws FlowNavigationException if the <code>eventId</code> is not a valid event for
     *         the state identified by <code>stateId</code>, or if the <code>stateId</code>
     *         does not map to a valid flow state.
     */
    public ViewDescriptor execute(String eventId, String stateId, FlowSessionExecutionStack sessionExecutionStack,
            HttpServletRequest request, HttpServletResponse response) throws FlowNavigationException;
}