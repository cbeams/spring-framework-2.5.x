/*
 * Created on Jul 29, 2004
 */
package org.springframework.jmx;

import java.util.List;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.springframework.jmx.factory.MBeanServerFactoryBean;

import junit.framework.TestCase;

/**
 * @author robh
 */
public class MBeanServerFactoryBeanTests extends TestCase {

    public void testGetObject() throws Exception{
        MBeanServerFactoryBean bean = new MBeanServerFactoryBean();
        bean.afterPropertiesSet();
        
        MBeanServer server = (MBeanServer)bean.getObject();
        assertNotNull("The MBeanServer should not be null", server);
    }
    
    public void testDefaultDomain() throws Exception {
        MBeanServerFactoryBean bean = new MBeanServerFactoryBean();
        bean.setDefaultDomain("foo");
        bean.afterPropertiesSet();
        
        MBeanServer server = bean.getServer();
        assertEquals("The default domain should be foo", "foo", server.getDefaultDomain());
    }
    
    public void testCreateMBeanServer() throws Exception {
        testCreation(true, "The server should be available in the list");
    }
    
    public void testNewMBeanServer() throws Exception {
        testCreation(false, "The server should not be available in the list");
    }
    
    private void testCreation(boolean referenceShouldExist, String failMsg) throws Exception {
        MBeanServerFactoryBean bean = new MBeanServerFactoryBean();
        bean.setHaveFactoryHoldReference(referenceShouldExist);
        bean.afterPropertiesSet();
        
        MBeanServer server = bean.getServer();
        List servers = MBeanServerFactory.findMBeanServer(null);
        
        boolean found = false;
        
        for(int x = 0; x < servers.size(); x++) {
            if(servers.get(x) == server) {
                found = true;
                break;
            }
        }
        
        if(!(found == referenceShouldExist)) {
            fail(failMsg);
        }
    }
}
