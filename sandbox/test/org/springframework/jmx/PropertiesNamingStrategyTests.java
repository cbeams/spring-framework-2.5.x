/*
 * Created on Jul 14, 2004
 */
package org.springframework.jmx;

import javax.management.ObjectInstance;

/**
 * @author robh
 */
public class PropertiesNamingStrategyTests extends AbstractJmxTests {

    private static final String OBJECT_NAME = "bean:name=namingTest";

    public PropertiesNamingStrategyTests(String name) {
        super(name);
    }

    public void testNaming() throws Exception {
        ObjectInstance instance = server.getObjectInstance(ObjectNameManager.getInstance(OBJECT_NAME));
        assertNotNull("ObjectInstance should not be null", instance);
    }
}