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
public abstract class FlowException extends RuntimeException {

    protected FlowException() {
        super();
    }
    
    public FlowException(String message) {
        super(message);
    }
    
    public FlowException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public FlowException(Throwable cause) {
        super(cause);
    }
}
