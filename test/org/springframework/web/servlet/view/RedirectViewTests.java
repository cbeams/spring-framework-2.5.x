/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.servlet.view;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import org.easymock.MockControl;

/**
 * Tests for redirect view, and query string construction.
 * Doesn't test URL encoding, although it does check it's called.
 * Uses mock objects.
 * @author Rod Johnson
 * @since 27-May-2003
 * @version $Id: RedirectViewTests.java,v 1.5 2004-03-09 08:27:28 jhoeller Exp $
 */
public class RedirectViewTests extends TestCase {

	private void test(final Map m, String url, boolean contextRelative, String expectedUrlForEncoding) throws Exception {
		class TestRedirectView extends RedirectView {
			public boolean valid;
			
			/**
			 * Test this callback method is called with correct args
			 */
			protected Map queryProperties(Map model) {
				// They may not be the same model instance, but they're still equal
				assertTrue(m.equals(model));
				valid = true;
				return super.queryProperties(model);
			}
		}

		TestRedirectView rv = new TestRedirectView();
		rv.setUrl(url);
		rv.setContextRelative(contextRelative);

		MockControl rc = MockControl.createControl(HttpServletRequest.class);
		HttpServletRequest request = (HttpServletRequest) rc.getMock();
		if (contextRelative) {
			expectedUrlForEncoding = "/context" + expectedUrlForEncoding;
			request.getContextPath();
			rc.setReturnValue("/context");
		}
		rc.replay();
		
		MockControl mc = MockControl.createControl(HttpServletResponse.class);
		HttpServletResponse resp = (HttpServletResponse) mc.getMock();
		resp.encodeRedirectURL(expectedUrlForEncoding);
		mc.setReturnValue(expectedUrlForEncoding);
		resp.sendRedirect(expectedUrlForEncoding);
		mc.setVoidCallable(1);
		mc.replay();
		
		rv.render(m, request, resp);
		assertTrue(rv.valid);
		mc.verify();
	}
	
	public void testEmptyMap() throws Exception {
		String url = "/myUrl";
		test(new HashMap(), url, false, url);
	}
	
	public void testEmptyMapWithContextRelative() throws Exception {
		String url = "/myUrl";
		test(new HashMap(), url, true, url);
	}

	public void testSingleParam() throws Exception {
		String url = "http://url.somewhere.com";
		String key = "foo";
		String val = "bar";
		Map m = new HashMap();
		m.put(key, val);
		String expectedUrlForEncoding = url + "?" + key + "=" + val;
		test(m, url, false, expectedUrlForEncoding);
	}
	
	public void testTwoParams() throws Exception {
		String url = "http://url.somewhere.com";
		String key = "foo";
		String val = "bar";
		String key2 = "thisIsKey2";
		String val2 = "andThisIsVal2";
		Map m = new HashMap();
		m.put(key, val);
		m.put(key2, val2);
		String expectedUrlForEncoding = url + "?" + key + "=" + val + "&" + key2 + "=" + val2;
		test(m, url, false, expectedUrlForEncoding);
	}
	
	public void testObjectConversion() throws Exception {
		String url = "http://url.somewhere.com";
		String key = "foo";
		String val = "bar";
		String key2 = "int2";
		Object val2 = new Long(611);
		Map m = new HashMap();
		m.put(key, val);
		m.put(key2, val2);
		String expectedUrlForEncoding = url + "?" + key + "=" + val + "&" + key2 + "=" + val2;
		test(m, url, false, expectedUrlForEncoding);
	}
	
	public void testNoUrlSet() throws Exception {
		RedirectView rv = new RedirectView();
		try {
			rv.initApplicationContext();
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

}
