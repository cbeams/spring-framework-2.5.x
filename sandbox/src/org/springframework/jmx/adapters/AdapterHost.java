/*
 * Created on Sep 15, 2004
 */
package org.springframework.jmx.adapters;

import javax.management.ObjectName;

/**
 * @author robh
 */
public interface AdapterHost {

    ObjectName getObjectName();
    
    void start();
    
    void stop();
    
    Object getAdaptor();
}
