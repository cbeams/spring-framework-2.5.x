/*
 * Created on Jul 21, 2004
 */
package org.springframework.jmx;

import javax.management.ObjectInstance;

/**
 * @author robh
 */
public class MetadataNamingStrategyTests extends AbstractJmxTests {

    private static final String OBJECT_NAME = "spring:bean=test";
    
    public MetadataNamingStrategyTests(String name) {
        super(name);
    }
    
    public void testNaming() throws Exception{
        ObjectInstance instance = server.getObjectInstance(ObjectNameManager.getInstance(OBJECT_NAME));
        assertNotNull("The instance should not be null", instance);
    }

    protected String getApplicationContextPath() {
        return "org/springframework/jmx/metadataAssembler.xml";
    }
}
