/*
 * Created on Jul 22, 2004
 */
package org.springframework.jmx;

/**
 * @author robh
 */
public class InvalidInvocationException extends RuntimeException {

    /**
     * 
     */
    public InvalidInvocationException() {
        super();
    }

    /**
     * @param arg0
     */
    public InvalidInvocationException(String msg) {
        super(msg);
    }

    /**
     * @param arg0
     */
    public InvalidInvocationException(Throwable rootCause) {
        super(rootCause);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public InvalidInvocationException(String msg, Throwable rootCause) {
        super(msg, rootCause);
    }

}
