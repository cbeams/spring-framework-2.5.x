/*
 * Created on Jul 14, 2004
 */
package org.springframework.jmx;

import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * @author robh
 */
public class IdentityNamingStrategyTests extends AbstractJmxTests {

    public IdentityNamingStrategyTests(String name) {
        super(name);
    }

    public void testNaming() throws Exception {
        JmxTestBean testBean = (JmxTestBean) getContext().getBean("testBean");
        String desiredName = testBean.getClass().getPackage().getName();
        desiredName += ":class=" + ClassUtils.getShortName(testBean.getClass());
        desiredName += ",hashCode="
                + ObjectUtils.getIdentityHexString(testBean);

        ObjectInstance instance = server.getObjectInstance(ObjectName.getInstance(desiredName));
        
        assertNotNull("ObjectInstance should not be null", instance);
    }
}