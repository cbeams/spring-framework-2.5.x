package org.springframework.mock.web;

import junit.framework.TestCase;

import java.util.Enumeration;

/**
 * Unit tests for the <code>MockHttpServletRequest</code> class.
 *
 * @author Rick Evans
 * @since 2.0.1
 */
public final class MockHttpServletRequestTests extends TestCase {

	public void testHttpHeaderNameCasingIsPreserved() throws Exception {
		final String headerName = "Header1";

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(headerName, "value1");
		Enumeration requestHeaders = request.getHeaderNames();
		assertNotNull(requestHeaders);
		assertEquals("HTTP header casing not being preserved", headerName, requestHeaders.nextElement());
	}

}
