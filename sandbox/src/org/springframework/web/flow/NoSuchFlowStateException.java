/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */
package org.springframework.web.flow;

import java.util.Iterator;

/**
 * Thrown when a state could not be found in a flow, on lookup by
 * <code>stateId</code>.
 * @author Keith Donald
 */
public class NoSuchFlowStateException extends FlowNavigationException {

    private String stateId;

    /**
     * @param flow
     * @param message
     */
    public NoSuchFlowStateException(Flow flow, String stateId) {
        super(flow);
        this.stateId = stateId;
    }

    /**
     * @param flow
     * @param message
     * @param cause
     */
    public NoSuchFlowStateException(Flow flow, String stateId, Throwable cause) {
        super(flow, cause);
        this.stateId = stateId;
    }

    public String getMessage() {
        Iterator it = getFlow().statesIterator();
        StringBuffer statesStrBuffer = new StringBuffer(512);
        while (it.hasNext()) {
            statesStrBuffer.append(((AbstractState)it.next()).getId());
            if (it.hasNext()) {
                statesStrBuffer.append(", ");
            }
        }
        return "No state with state id '" + stateId + "' exists for flow '" + getFlow().getId()
                + "' -- valid states are '[" + statesStrBuffer.toString() + "]' -- programmer error?";
    }
}