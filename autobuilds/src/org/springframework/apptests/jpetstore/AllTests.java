/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.apptests.jpetstore;


import org.springframework.apptests.AbstractTestCase;

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
 * @version $Id: AllTests.java,v 1.2 2003-12-22 00:38:43 davison Exp $
 */
public class AllTests extends AbstractTestCase {

	private WebConversation wc;
	private WebResponse resp;
	private WebForm form;
	
    /**
     * Constructor for AllTests.
     * @param arg0
     */
    public AllTests(String arg0) {
        super(arg0);  
		wc = new WebConversation();		      
    }
    
    public void testHomePage() {
		try {
            resp = wc.getResponse( testServer + "/jpetstore/" );
            
                        
        } catch (Exception e) {
			fail("Exception: " + e);
        }

    }
}