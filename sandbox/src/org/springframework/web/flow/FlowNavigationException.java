/*
 * Created on 12-Dec-2004 by Interface21 on behalf of Voca.
 *
 * This file is part of the NewBACS programme.
 * (c) Voca 2004-5. All rights reserved.
 */
package org.springframework.web.flow;

/**
 * @author Keith Donald
 */
public abstract class FlowNavigationException extends FlowException {

    private Flow flow;

    public FlowNavigationException(Flow flow) {
        super();
        setFlow(flow);
    }

    public FlowNavigationException(Flow flow, String message) {
        super(message);
        setFlow(flow);
    }
    
    public FlowNavigationException(Flow flow, String message, Throwable cause) {
        super(message, cause);
        setFlow(flow);
    }
    
    public FlowNavigationException(Flow flow, Throwable cause) {
        super(cause);
        setFlow(flow);
    }
    
    private void setFlow(Flow flow) {
        this.flow = flow;
    }
    
    protected Flow getFlow() {
        return flow;
    }
}
