/*
 * Created on Jul 29, 2004
 */
package org.springframework.jmx;

import org.springframework.core.NestedRuntimeException;

/**
 * @author robh
 */
public class NotificationException extends NestedRuntimeException {

    /**
     * @param arg0
     */
    public NotificationException(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     * @param arg1
     */
    public NotificationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

}
