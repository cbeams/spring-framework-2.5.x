/*
 * Created on Jul 22, 2004
 */
package org.springframework.jmx.proxy;

import org.springframework.core.NestedRuntimeException;

/**
 * @author robh
 */
public class InvalidInvocationException extends NestedRuntimeException {

   
    /**
     * @param arg0
     */
    public InvalidInvocationException(String msg) {
        super(msg);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public InvalidInvocationException(String msg, Throwable rootCause) {
        super(msg, rootCause);
    }

}
