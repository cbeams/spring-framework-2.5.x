/*
 * Created on Sep 15, 2004
 */
package org.springframework.jmx.adpters;

import javax.management.ObjectName;

/**
 * @author robh
 */
public interface AdaptorHost {

    public ObjectName getObjectName();
    
    public void start();
    
    public Object getAdaptor();
}
