/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.web.portlet.mvc;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import junit.framework.TestCase;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.mock.web.portlet.MockActionRequest;
import org.springframework.mock.web.portlet.MockActionResponse;
import org.springframework.mock.web.portlet.MockPortletConfig;
import org.springframework.mock.web.portlet.MockPortletContext;
import org.springframework.mock.web.portlet.MockRenderRequest;
import org.springframework.mock.web.portlet.MockRenderResponse;
import org.springframework.web.portlet.context.ConfigurablePortletApplicationContext;
import org.springframework.web.portlet.context.StaticPortletApplicationContext;

/**
 * @author Mark Fisher
 */
public class PortletWrappingControllerTests extends TestCase {

	private PortletWrappingController controller;
	private ConfigurablePortletApplicationContext applicationContext;
	
	public void setUp() {
		this.applicationContext = new MyApplicationContext();
		MockPortletConfig config = new MockPortletConfig(new MockPortletContext(), "wrappedPortlet");
		this.applicationContext.setPortletConfig(config);
		this.applicationContext.refresh();
		controller = (PortletWrappingController) this.applicationContext.getBean("controller");	
	}
	
	public void testActionRequest() throws Exception {
		MockActionRequest request = new MockActionRequest();
		MockActionResponse response = new MockActionResponse();
		request.setParameter("test", "test");
		controller.handleActionRequest(request, response);
		String result = response.getRenderParameter("result");
		assertEquals("myPortlet-action", result);
	}

	public void testRenderRequest() throws Exception {
		MockRenderRequest request = new MockRenderRequest();
		MockRenderResponse response = new MockRenderResponse();
		controller.handleRenderRequest(request, response);
		String result = response.getContentAsString();
		assertEquals("myPortlet-view", result);
	}
	
	public void testActionRequestWithNoParameters() throws Exception {
		MockActionRequest request = new MockActionRequest();
		MockActionResponse response = new MockActionResponse();
		try {
			controller.handleActionRequest(request, response);
			fail("Should have thrown IllegalArgumentException");
		}
		catch(IllegalArgumentException ex) {
			//expected
		}
	}

	public void testPortletName() throws Exception {
		MockActionRequest request = new MockActionRequest();
		MockActionResponse response = new MockActionResponse();	
		request.setParameter("portletName", "test");
		controller.handleActionRequest(request, response);
		String result = response.getRenderParameter("result");
		assertEquals("wrappedPortlet", result);
	}

	public static class MyPortlet implements Portlet {
		
		private PortletConfig portletConfig;

		public void init(PortletConfig portletConfig) throws PortletException {
			this.portletConfig = portletConfig;
		}

		public void processAction(ActionRequest request, ActionResponse response) throws PortletException {
			if(request.getParameter("test") != null) {
				response.setRenderParameter("result", "myPortlet-action");
			}
			else if(request.getParameter("portletName") != null) {
				response.setRenderParameter("result", getPortletConfig().getPortletName());
			}
			else {
				throw new IllegalArgumentException("no request parameters");
			}
		}

		public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
			response.getWriter().write("myPortlet-view");
		}
		
		public PortletConfig getPortletConfig() {
			return this.portletConfig;
		}

		public void destroy() {}
	}
	
	public static class MyApplicationContext extends StaticPortletApplicationContext {
		
		public void refresh() throws BeansException {
			MutablePropertyValues pvs = new MutablePropertyValues();
			pvs.addPropertyValue("portletClass", MyPortlet.class);
			registerSingleton("controller", PortletWrappingController.class, pvs);
			super.refresh();
		}
	}
}
