package org.springframework.web.util;

import javax.servlet.http.HttpServletRequest;

import org.springframework.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

/**
 * 
 * @author Rod Johnson
 * @since 1.3
 */
public class RequestHolderTests extends TestCase {
	
	protected void tearDown() {
		RequestHolder.clear();
	}
	
	public void testNoBinding() {
		try {
			RequestHolder.currentRequest();
			fail("No threadbound request");
		}
		catch (IllegalStateException ex) {
			// Ok
		}
	}
	
	public void testBinding() {
		HttpServletRequest req = new MockHttpServletRequest();
		RequestHolder.bind(req);
		assertSame(req, RequestHolder.currentRequest());
		RequestHolder.clear();
		testNoBinding();
	}

}
