/*
 * Created on 11-Aug-2004
 */
package org.springframework.jmx;

import javax.management.ObjectInstance;
import javax.management.ObjectName;


/**
 * @author robh
 */
public abstract class AbstractAutodetectTest extends AbstractJmxTests {


    public AbstractAutodetectTest(String name) {
        super(name);
   
    }
    
    public void testAutodetect() throws Exception{
        ObjectInstance oi = server.getObjectInstance(getExpectedObjectName());
        assertNotNull("The managed resource was not found", oi);
    }

    
    protected abstract ObjectName getExpectedObjectName() throws Exception;
    

}
