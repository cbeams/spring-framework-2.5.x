/*
 * Created on Jul 8, 2004
 */
package org.springframework.jmx;

import javax.management.MBeanInfo;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

/**
 * @author robh
 *  
 */
public abstract class AbstractJmxAssemblerTests extends AbstractJmxTests {

    public AbstractJmxAssemblerTests(String name) {
        super(name);
    }

    protected abstract String getObjectName();

    public void testMBeanRegistration() throws Exception {

        // beans are registered at this point - just grab them from the server
        ObjectInstance instance = getObjectInstance();
        assertNotNull("Bean should not be null", instance);
    }

    public void testRegisterOperations() throws Exception {
        JmxTestBean bean = (JmxTestBean) getContext().getBean("testBean");

        MBeanInfo inf = getMBeanInfo();

        assertEquals("Incorrect number of operations registered",
                getExpectedOperationCount(), inf.getOperations().length);
    }

    public void testRegisterAttributes() throws Exception {
        JmxTestBean bean = (JmxTestBean) getContext().getBean("testBean");

        MBeanInfo inf = getMBeanInfo();

        assertEquals("Incorrect number of attributes registered",
                getExpectedAttributeCount(), inf.getAttributes().length);
    }

    protected MBeanInfo getMBeanInfo() throws Exception {
        return server.getMBeanInfo(ObjectName.getInstance(getObjectName()));
    }
    
    protected ObjectInstance getObjectInstance() throws Exception {
        return server.getObjectInstance(ObjectName.getInstance(getObjectName()));
    }
    
    protected abstract int getExpectedOperationCount();

    protected abstract int getExpectedAttributeCount();
}