/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.apptests.jpetstore;


import org.springframework.apptests.AbstractTestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import com.meterware.httpunit.*;



/**
 * AllTests
 * 
 * Not every use case is covered here since the point is to exrcise the 
 * Spring code rather than the application code.  Tests implemented here 
 * are sufficient to check all aspects of Spring that the sample app
 * makes use of.
 * 
 * @author Darren Davison
 * @version $Id: AllTests.java,v 1.3 2004-01-01 13:44:21 davison Exp $
 */
public class AllTests extends AbstractTestCase {

	private WebConversation wc;
	private WebResponse resp;
	private WebForm form;
	
	private ClassPathXmlApplicationContext ctx;
	private JdbcTemplate jdbcTemplate;
	
    /**
     * Constructor for AllTests.
     * @param arg0
     */
    public AllTests(String arg0) {
        super(arg0);  
		wc = new WebConversation();      
    }
    
    /**
     * home page - chk title
     */
    public void testHomePage() {
		try {
            resp = wc.getResponse( testServer + "/jpetstore/" );
            String title = resp.getTitle();
            assertEquals(title, "JPetStore Demo");
                        
        } catch (Exception e) {
			fail("Exception: " + e);
        }
    }
    
    /**
     * complete purchase case
     */
    public void testPurchase() {
    	String shopRoot = testServer + "/jpetstore/shop/";
		try {
			resp = wc.getResponse( shopRoot + "index.do" );
			String html = resp.getText();		
			
			// links for pet categories apparent?
			assertTrue("Expected to find a link for FISH category in page", html.indexOf("viewCategory.do?categoryId=FISH") > -1);
			assertTrue("Expected to find a link for CATS category in page", html.indexOf("viewCategory.do?categoryId=CATS") > -1);
			assertTrue("Expected to find a link for DOGS category in page", html.indexOf("viewCategory.do?categoryId=DOGS") > -1);
			assertTrue("Expected to find a link for BIRDS category in page", html.indexOf("viewCategory.do?categoryId=BIRDS") > -1);
			
			// see fish
			resp = wc.getResponse( shopRoot + "viewCategory.do?categoryId=FISH" );
			// fish should be in the 3rd table
			String[][] fishList = resp.getTables()[2].asText();
			assertEquals("Koi", fishList[1][1]);
			assertEquals("Goldfish", fishList[2][1]);
			assertEquals("Angelfish", fishList[3][1]);
			assertEquals("Tiger Shark", fishList[4][1]);
			
			// view koi
			resp = wc.getResponse( shopRoot + "viewProduct.do?productId=FI-FW-01" );
			html = resp.getText();
			assertTrue("Expected to find a link for spotted koi in page", html.indexOf("viewItem.do?itemId=EST-4") > -1);
			assertTrue("Expected to find a link for spotless koi in page", html.indexOf("viewItem.do?itemId=EST-5") > -1);
			
			// add spotted koi to cart
			resp = wc.getResponse( shopRoot + "addItemToCart.do?workingItemId=EST-4");
			html = resp.getText();
			assertTrue("Expected to find $18.50 subtotal in page", html.indexOf("Sub Total: $18.50") > -1);
			
			// double the order
			WebForm form = resp.getForms()[1];
			form.setParameter("EST-4", "2");
			resp = form.submit();
			html = resp.getText();
			assertTrue("Expected to find $37.00 subtotal in page", html.indexOf("Sub Total: $37.00") > -1);
			
			// checkout
			resp = wc.getResponse( shopRoot + "newOrder.do");
			html = resp.getText();
			assertTrue("Expected to be prompted for login", html.indexOf("Please enter your username and password") > -1);
			form = resp.getForms()[1];
			// duff user
			form.setParameter("username", "j2ee9");
			resp = form.submit();
			html = resp.getText();
			assertTrue("Expected to be told of failed login", html.indexOf("Invalid username or password") > -1);
			resp = wc.getResponse( shopRoot + "newOrder.do");
			form = resp.getForms()[1];
			// good user
			form.setParameter("username", "j2ee");
			form.setParameter("password", "j2ee");
			resp = form.submit();
			html = resp.getText();
			assertTrue("Expected payment details", html.indexOf("Payment Details") > -1);
			
			// change registered name
			form = resp.getForms()[1];
			// duff value
			form.setParameter("order.billToFirstName", "");
			resp = form.submit();
			html = resp.getText();
			assertTrue("Expected error on first name field", html.indexOf("Billing Info: first name is required.") > -1);
			form = resp.getForms()[1];
			// ok value
			form.setParameter("order.billToFirstName", "Marmaduke");
			resp = form.submit();
			html = resp.getText();
			assertTrue("Expected confirmation of billing details", html.indexOf("Please confirm the information below") > -1);
			
			// finish up
			resp = wc.getResponse( shopRoot + "newOrder.do?_finish=true");
			html = resp.getText();
			assertTrue("Expected order number confirmation", html.indexOf("Order #1000") > -1 );
			
			//TODO verify database tables look as expected
			
						
		} catch (Exception e) {
			fail("Exception: " + e);
		}
    }
    

}