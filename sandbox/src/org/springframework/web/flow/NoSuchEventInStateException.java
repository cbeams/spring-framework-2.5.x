/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */
package org.springframework.web.flow;

import org.springframework.util.DefaultObjectStyler;

/**
 * Thrown when no event with id <code>eventId</code> exists in the specified
 * state for the specified flow.
 * @author Keith Donald
 */
public class NoSuchEventInStateException extends FlowNavigationException {

    private TransitionableState state;

    private String eventId;

    public NoSuchEventInStateException(Flow flow, TransitionableState state, String eventId) {
        super(flow);
        this.state = state;
        this.eventId = eventId;
    }

    public NoSuchEventInStateException(Flow flow, TransitionableState state, String eventId, Throwable cause) {
        super(flow, cause);
        this.state = state;
        this.eventId = eventId;
    }

    public String getMessage() {
        return "No such transition for event '" + eventId + "' in state '" + state.getId() + "' in flow '"
                + getFlow().getId() + "' -- valid transitions are " + DefaultObjectStyler.call(state.getTransitions())
                + " -- programmer error?";
    }
}