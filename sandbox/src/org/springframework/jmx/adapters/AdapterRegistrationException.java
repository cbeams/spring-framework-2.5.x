/*
 * Created on 17-Nov-2004
 */
package org.springframework.jmx.adapters;

import org.springframework.core.NestedRuntimeException;


/**
 * @author robh
 */
public class AdapterRegistrationException extends NestedRuntimeException {

   
    public AdapterRegistrationException(String message) {
        super(message);
    }


    public AdapterRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }

}
