
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

package org.springframework.web.servlet.mvc;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class ControllerTestSuite extends TestCase {

	public void testParameterizableViewControllerWith() throws Exception {
		String viewName = "viewName";
		ParameterizableViewController pvc = new ParameterizableViewController();
		pvc.setViewName(viewName);
		pvc.initApplicationContext();
		// We don't care about the params.
		ModelAndView mv = pvc.handleRequest(new MockHttpServletRequest("GET", "foo.html"), null);
		assertTrue("model has no data", mv.getModel().size() == 0);
		assertTrue("model has correct viewname", mv.getViewName().equals(viewName));
		assertTrue("getViewName matches", pvc.getViewName().equals(viewName));
	}

	public void testParameterizableViewControllerWithPropertyNotSet() {
		ParameterizableViewController pvc = new ParameterizableViewController();
		try {
			pvc.initApplicationContext();
			fail("should require viewName property to be set");
		}
		catch (IllegalArgumentException ex){
			// expected
			assertTrue("meaningful exception message", ex.getMessage().indexOf("viewName") != -1);
		}
	}

	public void testServletWrappingController() throws Exception {
		MockControl requestControl = MockControl.createControl(HttpServletRequest.class);
		HttpServletRequest request = (HttpServletRequest) requestControl.getMock();
		MockControl responseControl = MockControl.createControl(HttpServletResponse.class);
		HttpServletResponse response = (HttpServletResponse) responseControl.getMock();
		MockControl contextControl = MockControl.createControl(ServletContext.class);
		ServletContext context = (ServletContext) contextControl.getMock();
		MockControl dispatcherControl = MockControl.createControl(RequestDispatcher.class);
		RequestDispatcher dispatcher = (RequestDispatcher) dispatcherControl.getMock();

		request.getMethod();
		requestControl.setReturnValue("GET", 1);
		context.getNamedDispatcher("action");
		contextControl.setReturnValue(dispatcher, 1);
		dispatcher.forward(request, response);
		dispatcherControl.setVoidCallable(1);
		requestControl.replay();
		contextControl.replay();
		dispatcherControl.replay();

		StaticWebApplicationContext sac = new StaticWebApplicationContext();
		sac.setServletContext(context);
		ServletWrappingController swc = new ServletWrappingController();
		swc.setServletName("action");
		swc.setApplicationContext(sac);
		swc.initApplicationContext();
		assertNull(swc.handleRequest(request, response));

		requestControl.verify();
		contextControl.verify();
		dispatcherControl.verify();
	}

	public void testServletWrappingControllerWithPropertyNotSet() {
		ServletWrappingController swc = new ServletWrappingController();
		try {
			swc.initApplicationContext();
			fail("should require servletName property to be set");
		}
		catch (IllegalArgumentException ex){
			// expected
			assertTrue("meaningful exception message", ex.getMessage().indexOf("servletName") != -1);
		}
	}

}
