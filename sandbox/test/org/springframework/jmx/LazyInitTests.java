package org.springframework.jmx;

import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.aop.support.AopUtils;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * @author robh
 */
public class LazyInitTests extends TestCase{


    public void testLazyInit() {
        try {
            ApplicationContext ctx = new ClassPathXmlApplicationContext(getApplicationContextPath());
            assertTrue(true);
        } catch(RuntimeException ex) {
            ex.printStackTrace();
            fail("ExceptionOnInitBean was instantiated");

        }
    }

    public void testInvokeOnLazyInitBean() throws Exception{
        ExceptionOnInitBean bean = null;
        ApplicationContext ctx = new ClassPathXmlApplicationContext(getApplicationContextPath());
        MBeanServer server = (MBeanServer)ctx.getBean("server");
        ObjectName oname = ObjectNameManager.getInstance("bean:name=testBean2");

        String name = (String) server.getAttribute(oname, "name");

        assertEquals("Invalid name returned", "foo", name);
    }

    private String getApplicationContextPath() {
        return "org/springframework/jmx/lazyInit.xml";
    }
}
