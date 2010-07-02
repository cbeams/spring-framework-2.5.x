/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.apptests.jpetstore;


import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.apptests.AbstractTestCase;

import com.meterware.httpunit.*;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebResponse;



/**
 * AllTests
 * 
 * Not every use case is covered here since the point is to exercise the 
 * Spring code rather than the application code.  Tests implemented here 
 * are sufficient to check all aspects of Spring that the sample app
 * makes use of.
 * 
 * @author Darren Davison
 */
public class AllTests extends AbstractTestCase {

	private WebConversation wc;
	private WebResponse resp;
	private WebForm form;
	
	String shopRoot = testServer + "/jpetstore/shop/";
		
		
    /**
     * Constructor for AllTests.
     * @param arg0
     */
    public AllTests(String arg0) {
        super(arg0);  		
    }
    
    /**
     * home page - chk title
     */
    public void testHomePage() {
		try {
			wc = new WebConversation();
			resp = wc.getResponse( testServer + "/jpetstore/" );
            String title = resp.getTitle();
            assertEquals(title, "JPetStore Demo");
                        
        } catch (Exception e) {
			fail("Exception: " + e);
        }
    }
    
    /**
     * help page - chk freemarker config
     */
    public void testHelpPage() {
		try {
			wc = new WebConversation();
			resp = wc.getResponse( testServer + "/jpetstore/shop/help.do?param=freemarker" );
            String html = resp.getText();
            assertTrue("Expected parameter to be echoed in freemarker view", html.indexOf("Parameter: freemarker") > -1);
                        
        } catch (Exception e) {
			fail("Exception: " + e);
        }
    }
    
    /**
     * complete purchase case
     */
    public void testPurchase() {
    	try {
			wc = new WebConversation();
    		resp = wc.getResponse(testServer + "/jpetstore/");
    		    		    		
    		resp = wc.getResponse( shopRoot + "index.do" );
    		String html;		
			
			// links for pet categories apparent?
			boolean gotFish = false;
			boolean gotDogs = false;
			for (int z = 0; z < resp.getLinks().length; z++) {
				if (resp.getLinks()[z].getURLString().indexOf("categoryId=FISH") > -1) gotFish = true;
				if (resp.getLinks()[z].getURLString().indexOf("categoryId=DOGS") > -1) gotDogs = true;				
			}
			assertTrue("Expected to find a link for FISH category in page", gotFish);
			assertTrue("Expected to find a link for DOGS category in page", gotDogs);
			
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
			
			// double the order (tests a Velocity View)
			WebForm form = resp.getForms()[1];
			form.setParameter("EST-4", "2");
			resp = form.submit();
			html = resp.getText();
			assertTrue("Expected to find $37.00 subtotal in page", html.indexOf("Sub Total: $37.00") > -1);
			
			// checkout (tests an XSLT view)
			resp = wc.getResponse( shopRoot + "checkout.do");
			html = resp.getText();
			assertTrue("Expected to find $37.00 subtotal in page", html.indexOf("Cart total: $37.00") > -1);
			WebLink l = resp.getLinks()[0];
			
			// click continue link to place order
			resp = l.click();
			//resp = wc.getResponse( shopRoot + "newOrder.do");
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
			assertTrue("Expected order number confirmation for order 1000", html.indexOf("Order #1000") > -1 );
			
			//verify database tables look as expected
			jdbcTemplate.query("SELECT * FROM ORDERS", new org.springframework.jdbc.core.RowCallbackHandler() {
				int count = 0;
				public void processRow(ResultSet rs) throws SQLException {
					if (++count == 1) {
						assertEquals("Expected order #1000 in ORDERS table", rs.getInt("ORDERID"), 1000);
						assertEquals("Expected j2ee as USERID in ORDERS table", rs.getString("USERID"), "j2ee");
					}
				}
			});
			
			jdbcTemplate.query("SELECT * FROM INVENTORY WHERE ITEMID='EST-4'", new org.springframework.jdbc.core.RowCallbackHandler() {
				public void processRow(ResultSet rs) throws SQLException {
					assertEquals("Expected inventory for EST-4 to be 9998", rs.getInt("QTY"), 9998);
				}
			});
						
		} catch (Exception e) {
			fail("Exception: " + e);
		}
    }
    
	/**
	 * search
	 */
	public void testSearch() {
		try {
			wc = new WebConversation();
			resp = wc.getResponse( shopRoot + "index.do" );
			form = resp.getForms()[0];
			form.setParameter("keyword", "koi");
			resp = form.submit();
			String[][] srchList = resp.getTables()[2].asText();
			assertEquals("Expected to find koi in search results", "Koi", srchList[1][2]);
	                
		} catch (Exception e) {
			fail("Exception: " + e);
		}
	}
}