/*
 * Created on 17-Nov-2004
 */
package org.springframework.jmx.adapters;

import org.springframework.core.NestedRuntimeException;

/**
 * Thrown if an Exception occurs during adapter startup. See cause for more
 * details.
 * 
 * @author robh
 */
public class AdapterStartupException extends NestedRuntimeException {

    public AdapterStartupException(String msg) {
        super(msg);

    }

    public AdapterStartupException(String msg, Throwable ex) {
        super(msg, ex);

    }

}