/*
 * Created on Jul 21, 2004
 */
package org.springframework.jmx;

import javax.management.MBeanInfo;

/**
 * @author robh
 */
public class MetadataAssemblerTests extends AbstractJmxAssemblerTests {

    private static final String OBJECT_NAME = "bean:name=testBean3";

    public MetadataAssemblerTests(String name) {
        super(name);
    }

    public void testDescription() throws Exception {
        MBeanInfo info = getMBeanInfo();
        assertEquals("The descriptions are not the same", "My Managed Bean",
                info.getDescription());
    }

    protected String getObjectName() {
        return OBJECT_NAME;
    }

    protected int getExpectedAttributeCount() {
        return 2;
    }

    protected int getExpectedOperationCount() {
        return 2;
    }
}