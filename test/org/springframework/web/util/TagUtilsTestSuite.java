package org.springframework.web.util;

import javax.servlet.jsp.PageContext;

import junit.framework.TestCase;

/**
 * @author Alef Arendsen
 */
public class TagUtilsTestSuite extends TestCase {
	
	public void testTagUtils() {
		// it's simple, test all scope, plus a non-existing one
		// (which should evaluate to SCOPE_PAGE)
		
		assertEquals(TagUtils.SCOPE_PAGE, "page");
		assertEquals(TagUtils.SCOPE_APPLICATION, "application");
		assertEquals(TagUtils.SCOPE_SESSION, "session");
		assertEquals(TagUtils.SCOPE_REQUEST, "request");
		
		assertEquals(TagUtils.getScope("page"), PageContext.PAGE_SCOPE);
		assertEquals(TagUtils.getScope("request"), PageContext.REQUEST_SCOPE);
		assertEquals(TagUtils.getScope("session"), PageContext.SESSION_SCOPE);
		assertEquals(TagUtils.getScope("application"), PageContext.APPLICATION_SCOPE);
		assertEquals(TagUtils.getScope("bla"), PageContext.PAGE_SCOPE);
		
		try {
			TagUtils.getScope(null);
			fail("Null scope, no excpetion thrown!");			
		}
		catch (IllegalArgumentException e) {
			// ok
		}
	}

}
