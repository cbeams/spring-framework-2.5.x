/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */
package org.springframework.web.flow;

/**
 * Thrown when no flow session exists by the specified
 * <code>flowSessionId</code>.
 * @author Keith Donald
 */
public class NoSuchFlowSessionException extends FlowException {
    private String flowSessionId;

    public NoSuchFlowSessionException(String flowSessionId, Throwable cause) {
        super(cause);
        this.flowSessionId = flowSessionId;
    }

    public String getMessage() {
        return "No flow session could be found with id '" + flowSessionId + "' - perhaps the flow has ended?";
    }
}