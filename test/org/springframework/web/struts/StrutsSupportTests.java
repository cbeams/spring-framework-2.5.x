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

package org.springframework.web.struts;

import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import junit.framework.TestCase;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.ModuleConfig;
import org.easymock.MockControl;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;

/**
 * @author Juergen Hoeller
 * @since 09.04.2004
 */
public class StrutsSupportTests extends TestCase {

	public void testActionSupportWithContextLoaderPlugIn() throws ServletException {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.addMessage("test", Locale.getDefault(), "testmessage");
		final ServletContext servletContext = new MockServletContext();
		wac.setServletContext(servletContext);
		wac.refresh();
		servletContext.setAttribute(ContextLoaderPlugIn.SERVLET_CONTEXT_PREFIX, wac);

		ActionServlet actionServlet = new ActionServlet() {
			public ServletContext getServletContext() {
				return servletContext;
			}
		};
		ActionSupport action = new ActionSupport() {
		};
		action.setServlet(actionServlet);

		assertEquals(wac, action.getWebApplicationContext());
		assertEquals(servletContext, action.getServletContext());
		assertEquals("testmessage", action.getMessageSourceAccessor().getMessage("test"));

		action.setServlet(null);
	}

	public void testActionSupportWithRootContext() throws ServletException {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.addMessage("test", Locale.getDefault(), "testmessage");
		final ServletContext servletContext = new MockServletContext();
		wac.setServletContext(servletContext);
		wac.refresh();
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);

		ActionServlet actionServlet = new ActionServlet() {
			public ServletContext getServletContext() {
				return servletContext;
			}
		};
		ActionSupport action = new ActionSupport() {
		};
		action.setServlet(actionServlet);

		assertEquals(wac, action.getWebApplicationContext());
		assertEquals(servletContext, action.getServletContext());
		assertEquals("testmessage", action.getMessageSourceAccessor().getMessage("test"));

		action.setServlet(null);
	}

	public void testDispatchActionSupportWithContextLoaderPlugIn() throws ServletException {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.addMessage("test", Locale.getDefault(), "testmessage");
		final ServletContext servletContext = new MockServletContext();
		wac.setServletContext(servletContext);
		wac.refresh();
		servletContext.setAttribute(ContextLoaderPlugIn.SERVLET_CONTEXT_PREFIX, wac);

		ActionServlet actionServlet = new ActionServlet() {
			public ServletContext getServletContext() {
				return servletContext;
			}
		};
		DispatchActionSupport action = new DispatchActionSupport() {
		};
		action.setServlet(actionServlet);

		assertEquals(wac, action.getWebApplicationContext());
		assertEquals(servletContext, action.getServletContext());
		assertEquals("testmessage", action.getMessageSourceAccessor().getMessage("test"));

		action.setServlet(null);
	}

	public void testDispatchActionSupportWithRootContext() throws ServletException {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.addMessage("test", Locale.getDefault(), "testmessage");
		final ServletContext servletContext = new MockServletContext();
		wac.setServletContext(servletContext);
		wac.refresh();
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);

		ActionServlet actionServlet = new ActionServlet() {
			public ServletContext getServletContext() {
				return servletContext;
			}
		};
		DispatchActionSupport action = new DispatchActionSupport() {
		};
		action.setServlet(actionServlet);

		assertEquals(wac, action.getWebApplicationContext());
		assertEquals(servletContext, action.getServletContext());
		assertEquals("testmessage", action.getMessageSourceAccessor().getMessage("test"));

		action.setServlet(null);
	}

	public void testLookupDispatchActionSupportWithContextLoaderPlugIn() throws ServletException {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.addMessage("test", Locale.getDefault(), "testmessage");
		final ServletContext servletContext = new MockServletContext();
		wac.setServletContext(servletContext);
		wac.refresh();
		servletContext.setAttribute(ContextLoaderPlugIn.SERVLET_CONTEXT_PREFIX, wac);

		ActionServlet actionServlet = new ActionServlet() {
			public ServletContext getServletContext() {
				return servletContext;
			}
		};
		LookupDispatchActionSupport action = new LookupDispatchActionSupport() {
			protected Map getKeyMethodMap() {
				return new HashMap();
			}
		};
		action.setServlet(actionServlet);

		assertEquals(wac, action.getWebApplicationContext());
		assertEquals(servletContext, action.getServletContext());
		assertEquals("testmessage", action.getMessageSourceAccessor().getMessage("test"));

		action.setServlet(null);
	}

	public void testLookupDispatchActionSupportWithRootContext() throws ServletException {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.addMessage("test", Locale.getDefault(), "testmessage");
		final ServletContext servletContext = new MockServletContext();
		wac.setServletContext(servletContext);
		wac.refresh();
		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);

		ActionServlet actionServlet = new ActionServlet() {
			public ServletContext getServletContext() {
				return servletContext;
			}
		};
		LookupDispatchActionSupport action = new LookupDispatchActionSupport() {
			protected Map getKeyMethodMap() {
				return new HashMap();
			}
		};
		action.setServlet(actionServlet);

		assertEquals(wac, action.getWebApplicationContext());
		assertEquals(servletContext, action.getServletContext());
		assertEquals("testmessage", action.getMessageSourceAccessor().getMessage("test"));

		action.setServlet(null);
	}

	public void testDelegatingActionProxy() throws Exception {
		final MockServletContext servletContext = new MockServletContext("/org/springframework/web/struts/");
		ContextLoaderPlugIn plugin = new ContextLoaderPlugIn();
		ActionServlet actionServlet = new ActionServlet() {
			public String getServletName() {
				return "action";
			}
			public ServletContext getServletContext() {
				return servletContext;
			}
		};

		MockControl moduleConfigControl = MockControl.createControl(ModuleConfig.class);
		ModuleConfig moduleConfig = (ModuleConfig) moduleConfigControl.getMock();
		moduleConfig.getPrefix();
		moduleConfigControl.setReturnValue("", 3);
		moduleConfigControl.replay();

		plugin.init(actionServlet, moduleConfig);
		assertTrue(servletContext.getAttribute(ContextLoaderPlugIn.SERVLET_CONTEXT_PREFIX) != null);

		DelegatingActionProxy proxy = new DelegatingActionProxy();
		proxy.setServlet(actionServlet);
		ActionMapping mapping = new ActionMapping();
		mapping.setPath("/test");
		mapping.setModuleConfig(moduleConfig);
		ActionForward forward = proxy.execute(mapping, null,
																					new MockHttpServletRequest(servletContext),
																					new MockHttpServletResponse());
		assertEquals("/test", forward.getPath());

		TestAction testAction = (TestAction) plugin.getWebApplicationContext().getBean("/test");
		assertTrue(testAction.getServlet() != null);
		proxy.setServlet(null);
		plugin.destroy();
		assertTrue(testAction.getServlet() == null);

		moduleConfigControl.verify();
	}

	public void testDelegatingActionProxyWithModule() throws Exception {
		final MockServletContext servletContext = new MockServletContext("/org/springframework/web/struts/WEB-INF");
		ContextLoaderPlugIn plugin = new ContextLoaderPlugIn();
		plugin.setContextConfigLocation("action-servlet.xml");
		ActionServlet actionServlet = new ActionServlet() {
			public String getServletName() {
				return "action";
			}
			public ServletContext getServletContext() {
				return servletContext;
			}
		};

		MockControl moduleConfigControl = MockControl.createControl(ModuleConfig.class);
		ModuleConfig moduleConfig = (ModuleConfig) moduleConfigControl.getMock();
		moduleConfig.getPrefix();
		moduleConfigControl.setReturnValue("/module", 3);
		moduleConfigControl.replay();

		plugin.init(actionServlet, moduleConfig);
		assertTrue(servletContext.getAttribute(ContextLoaderPlugIn.SERVLET_CONTEXT_PREFIX) == null);
		assertTrue(servletContext.getAttribute(ContextLoaderPlugIn.SERVLET_CONTEXT_PREFIX + "/module") != null);

		DelegatingActionProxy proxy = new DelegatingActionProxy();
		proxy.setServlet(actionServlet);
		ActionMapping mapping = new ActionMapping();
		mapping.setPath("/test2");
		mapping.setModuleConfig(moduleConfig);
		ActionForward forward = proxy.execute(mapping, null,
																					new MockHttpServletRequest(servletContext),
																					new MockHttpServletResponse());
		assertEquals("/module/test2", forward.getPath());

		TestAction testAction = (TestAction) plugin.getWebApplicationContext().getBean("/module/test2");
		assertTrue(testAction.getServlet() != null);
		proxy.setServlet(null);
		plugin.destroy();
		assertTrue(testAction.getServlet() == null);

		moduleConfigControl.verify();
	}

	public void testDelegatingActionProxyWithModuleAndDefaultContext() throws Exception {
		final MockServletContext servletContext = new MockServletContext("/org/springframework/web/struts/WEB-INF");
		ContextLoaderPlugIn plugin = new ContextLoaderPlugIn();
		plugin.setContextConfigLocation("action-servlet.xml");
		ActionServlet actionServlet = new ActionServlet() {
			public String getServletName() {
				return "action";
			}
			public ServletContext getServletContext() {
				return servletContext;
			}
		};

		MockControl defaultModuleConfigControl = MockControl.createControl(ModuleConfig.class);
		ModuleConfig defaultModuleConfig = (ModuleConfig) defaultModuleConfigControl.getMock();
		defaultModuleConfig.getPrefix();
		defaultModuleConfigControl.setReturnValue("", 1);
		defaultModuleConfigControl.replay();

		MockControl moduleConfigControl = MockControl.createControl(ModuleConfig.class);
		ModuleConfig moduleConfig = (ModuleConfig) moduleConfigControl.getMock();
		moduleConfig.getPrefix();
		moduleConfigControl.setReturnValue("/module", 2);
		moduleConfigControl.replay();

		plugin.init(actionServlet, defaultModuleConfig);
		assertTrue(servletContext.getAttribute(ContextLoaderPlugIn.SERVLET_CONTEXT_PREFIX) != null);
		assertTrue(servletContext.getAttribute(ContextLoaderPlugIn.SERVLET_CONTEXT_PREFIX + "/module") == null);

		DelegatingActionProxy proxy = new DelegatingActionProxy();
		proxy.setServlet(actionServlet);
		ActionMapping mapping = new ActionMapping();
		mapping.setPath("/test2");
		mapping.setModuleConfig(moduleConfig);
		ActionForward forward = proxy.execute(mapping, null,
																					new MockHttpServletRequest(servletContext),
																					new MockHttpServletResponse());
		assertEquals("/module/test2", forward.getPath());

		TestAction testAction = (TestAction) plugin.getWebApplicationContext().getBean("/module/test2");
		assertTrue(testAction.getServlet() != null);
		proxy.setServlet(null);
		plugin.destroy();
		assertTrue(testAction.getServlet() == null);

		moduleConfigControl.verify();
	}

}
