package org.springframework.jmx;

import javax.management.ObjectName;

/**
 * @author robh
 */
public class PropertyPlaceholderConfigurerTests extends AbstractJmxTests {
    public PropertyPlaceholderConfigurerTests(String name) {
        super(name);
    }

    protected String getApplicationContextPath() {
        return "org/springframework/jmx/propertyPlaceholderConfigurer.xml";
    }

    public void testPropertiesReplaced() {
        IJmxTestBean bean = (IJmxTestBean)getContext().getBean("testBean");

        assertEquals("Name is incorrect", "Rob Harrop", bean.getName());
        assertEquals("Age is incorrect", 100, bean.getAge());
    }

    public void testPropertiesCorrectInJmx() throws Exception{
        ObjectName oname = new ObjectName("bean:name=proxyTestBean1");
        Object name = server.getAttribute(oname, "name");
        Integer age = (Integer)server.getAttribute(oname, "age");

        assertEquals("Name is incorrect in JMX", "Rob Harrop", name);
        assertEquals("Age is incorrect in JMX", 100, age.intValue());
    }
}
