
/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.web.servlet.view;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockRequestDispatcher;
import org.springframework.web.context.WebApplicationContext;
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
		assertEquals(url, response.getForwardedUrl());
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
		assertEquals(url, response.getIncludedUrl());
		wacControl.verify();
		reqControl.verify();
	}

}
