/*
 * Created on 17-Nov-2004
 */
package org.springframework.jmx;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.springframework.jmx.adpters.ri.HtmlAdapterHost;

/**
 * @author robh
 */
public class HtmlAdapterHostTests extends AbstractJmxTests {


    public HtmlAdapterHostTests(String name) {
        super(name);
    }
    
    public void testStartAndStop() throws Exception{
        HtmlAdapterHost host = getAdapter();
        host.afterPropertiesSet();
        
        // attempt to connect 
        URL url = new URL("http://localhost:9090");        
        assertAdapter(url);
        
        host.stop();
    }
    
    public void testStartAndStopWithConfiguredPort() throws Exception{
        HtmlAdapterHost host = getAdapter();
        host.setPort(9001);
        host.afterPropertiesSet();
        
        // attempt to connect 
        URL url = new URL("http://localhost:9001");        
        assertAdapter(url);
        
        host.stop();
    }
    
    public void testStartAndStopWithLocatedServer() throws Exception{
        HtmlAdapterHost host = getAdapter();
        host.setServer(null);
        host.afterPropertiesSet();
        
        // attempt to connect 
        URL url = new URL("http://localhost:9090");        
        assertAdapter(url);
        
        host.stop();
    }
    
    private void assertAdapter(URL url) throws IOException {
        URLConnection connection = url.openConnection();


        // validate connection
        assertNotNull(connection);
        assertEquals("text/html", connection.getContentType());
        assertTrue(connection.getContentLength() > 0);
    }
    
    private HtmlAdapterHost getAdapter() {
        HtmlAdapterHost host = new HtmlAdapterHost();
        host.setObjectName("adapter:name=test");
        host.setRegisterWithServer(true);
        host.setServer(this.server);
        return host;
    }

}
