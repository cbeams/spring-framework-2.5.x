/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.apptests.buildtest;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebResponse;



/**
 * AllTests
 * 
 * Not every use case is covered here since the point is to exrcise the 
 * Spring code rather than the application code.  Tests implemented here 
 * are sufficient to check all aspects of Spring that the sample app
 * makes use of.
 * 
 * @author Darren Davison
 */
public class AllTests extends TestCase {

	private WebConversation wc;
	private WebResponse resp;
	private WebForm form;
	private String testServer;
	
    /**
     * Constructor for AllTests.
     * @param arg0
     */
    public AllTests(String arg0) {
        super(arg0);  
		Properties props = new Properties();
		try {
			props.load(new FileInputStream("build.properties"));
			testServer = "http://localhost:" + props.getProperty("autobuilds.server.http.port", "13084");
		
		} catch (IOException ioe) {
			testServer = "http://localhost:13084";		
		}
		
		wc = new WebConversation();		      
    }
    
    public void testHomePage() {
		try {
            resp = wc.getResponse( testServer + "/buildtest/" );
            assertTrue(resp.getText().indexOf("buildtest was deployed successfully") > -1);
                        
        } catch (Exception e) {
			fail("Exception while testing URL " + testServer + "/buildtest:" + e);
        }

    }
}