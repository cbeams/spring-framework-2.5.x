/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */
package org.springframework.web.flow;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.ToStringCreator;

/**
 * A state that has one or more transitions. State transitions are triggered by
 * events, specifically, when execution of an event in this state is requested.
 * 
 * @author Keith Donald
 */
public abstract class TransitionableState extends AbstractState {
    private Set transitions = new HashSet();

    public TransitionableState(String id) {
        super(id);
    }

    public TransitionableState(String id, Transition transition) {
        super(id);
        add(transition);
    }

    public TransitionableState(String id, Transition[] transitions) {
        super(id);
        addAll(transitions);
    }

    public boolean isTransitionable() {
        return true;
    }

    public void add(Transition transition) {
        transitions.add(transition);
    }

    public void addAll(Transition[] transitions) {
        this.transitions.addAll(Arrays.asList(transitions));
    }

    public Collection getTransitions() {
        return Collections.unmodifiableSet(transitions);
    }

    protected Transition getTransition(String eventId, Flow flow) throws NoSuchEventInStateException {
        Iterator it = transitions.iterator();
        while (it.hasNext()) {
            Transition transition = (Transition)it.next();
            if (transition.getId().equals(eventId)) {
                return transition;
            }
        }
        throw new NoSuchEventInStateException(flow, this, eventId);
    }

    /**
     * Execute the event identified by <code>eventId</code> in this state
     * 
     * @param eventId The id of the event to execute (e.g 'submit', 'next',
     *        'back')
     * @param flow The flow definition
     * @param sessionExecutionStack A flow session execution stack, tracking any
     *        suspended parent flows that spawned this flow (as a subflow)
     * @param request the client http request
     * @param response the server http response
     * @return A view descriptor containing model and view information needed to
     *         render the results of the event execution.
     * @throws IllegalArgumentException if the <code>eventId</code> does not
     *         map to a valid transition for this state.
     */
    public ViewDescriptor execute(String eventId, Flow flow, FlowSessionExecutionStack sessionExecutionStack,
            HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException {
        String qualifiedActiveFlowId = null;
        if (logger.isDebugEnabled()) {
            qualifiedActiveFlowId = sessionExecutionStack.getQualifiedActiveFlowId();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Event '" + eventId + "' within this state '" + getId() + "' for flow '"
                    + qualifiedActiveFlowId + "' was signaled; processing...");
        }
        if (flow.isLifecycleListenerSet()) {
            flow.getFlowLifecycleListener().flowEventSignaled(flow, eventId, this, sessionExecutionStack, request);
        }

        ViewDescriptor descriptor = getTransition(eventId, flow)
                .execute(flow, sessionExecutionStack, request, response);

        if (logger.isDebugEnabled()) {
            if (sessionExecutionStack.isActive()) {
                logger.debug("Event '" + eventId + "' within this state '" + getId() + "' for flow '"
                        + sessionExecutionStack.getQualifiedActiveFlowId()
                        + "' was processed; as a result, the new flow state is '"
                        + sessionExecutionStack.getCurrentStateId() + "'");
            }
            else {
                logger.debug("Event '" + eventId + "' within this state '" + getId() + "' for flow '"
                        + qualifiedActiveFlowId + "' was processed; as a result, the entire flow has ended");
            }
        }
        if (flow.isLifecycleListenerSet()) {
            flow.getFlowLifecycleListener().flowEventProcessed(flow, eventId, this, sessionExecutionStack, request);
        }
        return descriptor;
    }

    public String toString() {
        return new ToStringCreator(this).append("id", getId()).append("transitions", transitions).toString();
    }

}