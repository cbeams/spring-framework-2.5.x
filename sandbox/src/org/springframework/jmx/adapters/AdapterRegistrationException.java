/*
 * Created on 17-Nov-2004
 */
package org.springframework.jmx.adapters;

import com.ibatis.common.exception.NestedRuntimeException;

/**
 * @author robh
 */
public class AdapterRegistrationException extends NestedRuntimeException {

    /**
     * 
     */
    public AdapterRegistrationException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     */
    public AdapterRegistrationException(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     */
    public AdapterRegistrationException(Throwable arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     * @param arg1
     */
    public AdapterRegistrationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

}
