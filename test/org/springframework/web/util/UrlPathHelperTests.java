package org.springframework.web.util;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Rob Harrop
 */
public class UrlPathHelperTests extends TestCase {

	public void testGetPathWithinApplication() {
		UrlPathHelper helper = new UrlPathHelper();

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setContextPath("/petclinic");
		request.setRequestURI("/petclinic/welcome.html");

		assertEquals("Incorrect path returned", "/welcome.html", helper.getPathWithinApplication(request));
	}

	public void testGetPathWithinApplicationForRootWithNoLeadingSlash() {
		UrlPathHelper helper = new UrlPathHelper();

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setContextPath("/petclinic");
		request.setRequestURI("/petclinic");

		assertEquals("Incorrect root path returned", "/", helper.getPathWithinApplication(request));
	}

}
