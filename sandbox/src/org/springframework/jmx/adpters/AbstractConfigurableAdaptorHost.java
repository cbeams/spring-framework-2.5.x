/*
 * Created on Sep 15, 2004
 */
package org.springframework.jmx.adpters;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.ObjectNameManager;
import org.springframework.jmx.exceptions.ObjectNamingException;

/**
 * @author robh
 */
public abstract class AbstractConfigurableAdaptorHost implements AdaptorHost, InitializingBean {

    private ObjectName objectName;

    public ObjectName getObjectName() {
        return objectName;
    }

    public void setObjectName(ObjectName objectName) {
        this.objectName = objectName;
    }
    
    public void afterPropertiesSet() {
        if(objectName == null) {
            throw new IllegalArgumentException("Must specify ObjectName for AdaptorHost");
        }
        
        initAdaptorHost();
    }

    public void setObjectName(String objectName) {
        try {
            this.objectName = ObjectNameManager.getInstance(objectName);
        } catch (MalformedObjectNameException ex) {
            throw new ObjectNamingException(
                    "You supplied an invalid ObjectName for AdaptorHost of type: "
                            + this.getClass().getName(), ex);
        }
    }
    
    /**
     * Implementation can override to perform some additional initialization.
     *
     */
    protected void initAdaptorHost(){};
}