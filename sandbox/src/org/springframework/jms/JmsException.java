package org.springframework.jms;

/**
 * Exception thrown by the framework whenever it encounters a problem related to JMS.
 *
 * @version 0.1
 * @author Andre Biryukov
 */
public final class JmsException extends org.springframework.core.NestedRuntimeException {


    /**
     * Constructor for JmsException.
     *
     * @param s Custom message string
     * @param ex Original exception
     */
    public JmsException(final String s, final Throwable ex) {
        super(s, ex);
    }

    /**
     * Constructor for JmsException
     *
     * @param s Custom message string.
     */
    public JmsException(final String s) {
        super(s);
    }


}
