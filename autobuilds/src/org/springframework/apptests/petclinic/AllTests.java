/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.apptests.petclinic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.apptests.AbstractTestCase;

import com.meterware.httpunit.*;



/**
 * AllTests
 * 
 * Not every use case is covered here since the point is to exrcise the 
 * Spring code rather than the petclinic code.  Tests implemented here 
 * are sufficient to check all aspects of Spring that the sample app
 * makes use of.
 * 
 * @author Darren Davison
 * @version $Id: AllTests.java,v 1.1 2003-12-19 01:44:36 davison Exp $
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
			resp = wc.getResponse( testServer + "/petclinic/welcome.htm" );
            WebLink[] links = resp.getLinks();
            assertTrue("Expected 5 links on Petclinic homepage", links.length == 5);
            assertEquals("/petclinic/findOwners.htm", links[0].getURLString());
			assertEquals("/petclinic/vets.htm", links[1].getURLString());
                        
        } catch (Exception e) {
			fail("Exception: " + e);
        }

    }
    
	public void testAllVets() {
		try {
			resp = wc.getResponse( testServer + "/petclinic/vets.htm" );
			String[][] vets = resp.getTables()[0].asText();
			assertEquals( "Name",  vets[0][0] );
			assertEquals( "Specialties",  vets[0][1] );
			assertEquals( "James Carter",  vets[1][0] );
			assertTrue( vets[1][1].indexOf("none") > -1 );
			assertEquals( "Linda Douglas",  vets[2][0] );
			assertTrue( vets[2][1].indexOf("dentistry") > -1 );
			assertTrue( vets[2][1].indexOf("surgery") > -1 );
			assertEquals( "Sharon Jenkins",  vets[3][0] );
			assertEquals( "Helen Leary",  vets[4][0] );
        
		} catch (Exception e) {
			fail("Exception: " + e);
		}

	}
	
	public void testFindOwner() {
		try {
			resp = wc.getResponse( testServer + "/petclinic/findOwners.htm" );
			form = resp.getForms()[0];
			
			// search for davis should get 2 results
			form.setParameter("lastName", "davis");  
			resp = form.submit();
			
			String[][] owners = resp.getTables()[0].asText();
			assertEquals( "Name",  owners[0][0] );
			assertEquals( "Address",  owners[0][1] );
			
			// betty
			assertEquals( "638 Cardinal Ave.",  owners[1][1] );
			assertEquals( "Sun Prairie",  owners[1][2] );
			
			// harold
			assertEquals( "563 Friendly St.",  owners[2][1] );
			assertEquals( "Windsor",  owners[2][2] );
			assertEquals( "6085553198",  owners[2][3] );
			assertTrue( owners[2][4].indexOf("Iggy") > -1 );
			
		} catch (Exception e) {
			fail("Exception: " + e);
		}
	}
	
	public void testEditOwner() {
		try {
			resp = wc.getResponse( testServer + "/petclinic/findOwners.htm" );
			form = resp.getForms()[0];
		
			// search for davis should get 2 results
			form.setParameter("lastName", "davis");  
			resp = form.submit();
		
			// submit the first form to give us Betty's details
			form = resp.getForms()[0];
			resp = form.submit();
			
			String[][] owner = resp.getTables()[0].asText();
			assertTrue(owner[0][0].indexOf("Name") > -1);
			assertTrue(owner[0][1].indexOf("Betty Davis") > -1);
			
			// edit betty
			form = resp.getForms()[0];
			resp = form.submit();
			
			// check defaults
			form = resp.getForms()[0]; 
			assertEquals( "Betty", form.getParameterValue( "firstName" ) );
			assertEquals( "Davis", form.getParameterValue( "lastName" ) );
			assertEquals( "638 Cardinal Ave.", form.getParameterValue( "address" ) );
			
			// change phone number
			form.setParameter("telephone", "9471555806");
			resp = form.submit();
			owner = resp.getTables()[0].asText();
			assertTrue("Phone number not updated as expected", owner[3][1].indexOf("9471555806") > -1);

		
		} catch (Exception e) {
			fail("Exception: " + e);
		}
	}
	
	public void testEditPet() {
		try {
			// get betty back
			resp = wc.getResponse( testServer + "/petclinic/owner.htm?ownerId=2" );
			form = resp.getForms()[1]; //edit pet Basil
			resp = form.submit();
		
			// check defaults
			form = resp.getForms()[0]; 
			assertEquals( "Basil", form.getParameterValue( "name" ) );
			assertEquals( "2002-08-06", form.getParameterValue( "birthDate" ) );
			assertEquals( "6", form.getParameterValue( "typeId" ) );
			
			// try submitting some duff data
			form.setParameter("name", "");
			resp = form.submit();
			
			String html = resp.getText();
			assertTrue("Expected 'is required' message attempting to update an invalid name", html.indexOf("is required") > -1);
			
			form.setParameter("name", "Basil");
			form.setParameter("birthDate", "abcdef");
			resp = form.submit();
			
			html = resp.getText();
			assertTrue("Expected 'invalid date' message attempting to update an invalid date", html.indexOf("invalid date") > -1);

			// change pet name
			form.setParameter("name", "Sybil");
			form.setParameter("birthDate", "2002-08-06");
			resp = form.submit();
			assertTrue(resp.getForms().length > 1);
			html = resp.getText();
			assertTrue(html.indexOf("Sybil") > -1);
		
		} catch (Exception e) {
			fail("Exception: " + e);
		}
	}
	
	public void testAddVisit() {
		try {
			// get betty back
			resp = wc.getResponse( testServer + "/petclinic/owner.htm?ownerId=2" );
			form = resp.getForms()[2]; //add visit
			resp = form.submit();
	
			// check defaults
			form = resp.getForms()[0]; 
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date now = new Date();
			String today = df.format(now);			
			assertEquals( today, form.getParameterValue( "date" ) );
		
			// try submitting some duff data
			form.setParameter("description", "");
			resp = form.submit();
		
			String html = resp.getText();
			assertTrue("Expected 'is required' message attempting to update empty description", html.indexOf("is required") > -1);
		
			form.setParameter("description", "test pet visit");
			resp = form.submit();
		
			html = resp.getText();
			assertTrue(html.indexOf("test pet visit") > -1);
	
		} catch (Exception e) {
			fail("Exception: " + e);
		}
	}
	
	public void testErrors() {
		try {
			// data access failure
			resp = wc.getResponse(testServer + "/petclinic/editPet.htm?petId=-1");
			String html = resp.getText();
			assertTrue(html.indexOf("Data access failure") > -1);
		} catch (Exception e) {
			fail("Exception: " + e);
		}
	}
	

}
