/*
 * Created on 11-Aug-2004
 */
package org.springframework.jmx;

import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectInstance;
import javax.management.ObjectName;

/**
 * @author robh
 */
public class RequiredModelMBeanTests extends AbstractJmxTests {

    public RequiredModelMBeanTests(String name) {
        super(name);
    }

    public void testUseRequiredModelMBean() throws Exception {
        String name = "bean:name=foo";
        
        JmxMBeanAdapter adapter = new JmxMBeanAdapter();
        adapter.setUseRequiredModelMBean(true);
        
        Map beans = new HashMap();
        beans.put(name, new JmxTestBean());
        
        adapter.setBeans(beans);
        adapter.afterPropertiesSet();
        adapter.registerBeans();
        
        ObjectInstance oi = server.getObjectInstance(ObjectName.getInstance(name));
        
        assertNotNull("Object instance should not be null", oi);
        
    }
}
