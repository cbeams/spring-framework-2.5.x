/*
 * Created on 11-Aug-2004
 */
package org.springframework.jmx;

import javax.management.ObjectName;

/**
 * @author robh
 */
public class MetadataAssemblerAutodetectTests extends AbstractAutodetectTest {


    public MetadataAssemblerAutodetectTests(String name) {
        super(name);
    }
    
    protected String getApplicationContextPath() {
        return "./sandbox/test/org/springframework/jmx/metadata-autodetect.xml";
    }
    
    protected ObjectName getExpectedObjectName() throws Exception{
        return ObjectName.getInstance("spring:bean=test");
    }

}
