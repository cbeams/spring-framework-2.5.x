
package org.springframework.web.servlet.view;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.mock.MockHttpServletResponse;
import org.springframework.web.mock.MockRequestDispatcher;
import org.springframework.web.util.UrlPathHelper;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class InternalResourceViewTests extends TestCase {

	/**
	 * Test that if the url property isn't supplied, view initialization fails.
	 */
	public void testRejectsNullUrl() throws Exception {
		MockControl mc = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) mc.getMock();
		mc.replay();
	
		InternalResourceView v = new InternalResourceView();
		try {
			v.setApplicationContext(wac);
			fail("Should be forced to set URL");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}
	
	public void testForward() throws Exception {
		HashMap model = new HashMap();
		Object obj = new Integer(1);
		model.put("foo", "bar");
		model.put("I", obj);
		
		MockControl wacControl = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wacControl.getMock();
		wacControl.replay();
		
		String url = "forward-to";
		
		MockControl reqControl = MockControl.createControl(HttpServletRequest.class);
		HttpServletRequest request = (HttpServletRequest) reqControl.getMock();
		Set keys = model.keySet();
		for (Iterator iter = keys.iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			request.setAttribute(key, model.get(key));
			reqControl.setVoidCallable(1);
		}

		request.getAttribute(UrlPathHelper.INCLUDE_URI_REQUEST_ATTRIBUTE);
		reqControl.setReturnValue(null);
		request.getRequestDispatcher(url);
		reqControl.setReturnValue(new MockRequestDispatcher(url));
		reqControl.replay();
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		InternalResourceView v = new InternalResourceView();
		v.setUrl(url);
		v.setApplicationContext(wac);
		
		// Can now try multiple tests
		v.render(model, request, response);
		assertEquals(url, response.forwarded);
		wacControl.verify();
		reqControl.verify();
	}
	
	public void testInclude() throws Exception {
		HashMap model = new HashMap();
		Object obj = new Integer(1);
		model.put("foo", "bar");
		model.put("I", obj);

		MockControl wacControl = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wacControl.getMock();
		wacControl.replay();

		String url = "forward-to";

		MockControl reqControl = MockControl.createControl(HttpServletRequest.class);
		HttpServletRequest request = (HttpServletRequest) reqControl.getMock();
		Set keys = model.keySet();
		for (Iterator iter = keys.iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			request.setAttribute(key, model.get(key));
			reqControl.setVoidCallable(1);
		}

		request.getAttribute(UrlPathHelper.INCLUDE_URI_REQUEST_ATTRIBUTE);
		reqControl.setReturnValue("somepath");
		request.getRequestDispatcher(url);
		reqControl.setReturnValue(new MockRequestDispatcher(url));
		reqControl.replay();

		MockHttpServletResponse response = new MockHttpServletResponse();
		InternalResourceView v = new InternalResourceView();
		v.setUrl(url);
		v.setApplicationContext(wac);

		// Can now try multiple tests
		v.render(model, request, response);
		assertEquals(url, response.included);
		wacControl.verify();
		reqControl.verify();
	}

	// TODO IO exception
	
	// TODO return null RequestDispatcher
	
	/*
	public void testRequestDispatcherThrowsIOException() throws Exception {
		HashMap model = new HashMap();
		model.put("foo", "bar");
			
		MockControl mc = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) mc.getMock();
		mc.replay();
			
		String url = "forward-to";
			
		MockHttpServletRequest request = new MockHttpServletRequest(new MockServletContext(), "GET", "some-url");
			
			
		// unused
		MockHttpServletResponse response = new MockHttpServletResponse();
			
		InternalResourceView v = new InternalResourceView();
		v.setUrl(url);
		v.setApplicationContext(wac);
			
		// Can now try multiple tests
		v.render(model, request, response);
		
		assertTrue(request.getAttribute("foo").equals(model.get("foo")));
			
		mc.verify();
	}
	*/

}
