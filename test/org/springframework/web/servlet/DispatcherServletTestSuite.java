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

package org.springframework.web.servlet;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.validation.BindException;
import org.springframework.web.bind.EscapedErrors;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.BaseCommandController;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.theme.AbstractThemeResolver;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class DispatcherServletTestSuite extends TestCase {
	
	static final String URL_KNOWN_ONLY_PARENT = "/knownOnlyToParent.do";

	private MockServletConfig servletConfig;

	private DispatcherServlet simpleDispatcherServlet;

	private DispatcherServlet complexDispatcherServlet;

	protected void setUp() throws ServletException {
		servletConfig = new MockServletConfig(new MockServletContext(), "simple");
		MockServletConfig complexConfig = new MockServletConfig(servletConfig.getServletContext(), "complex");
		complexConfig.addInitParameter("publishContext", "false");

		simpleDispatcherServlet = new DispatcherServlet();
		simpleDispatcherServlet.setContextClass(SimpleWebApplicationContext.class);
		simpleDispatcherServlet.init(servletConfig);

		complexDispatcherServlet = new DispatcherServlet();
		complexDispatcherServlet.setContextClass(ComplexWebApplicationContext.class);
		complexDispatcherServlet.setNamespace("test");
		complexDispatcherServlet.addRequiredProperty("publishContext");
		complexDispatcherServlet.init(complexConfig);
	}

	public void testDispatcherServlets() {
		assertTrue("Correct namespace", ("simple" + FrameworkServlet.DEFAULT_NAMESPACE_SUFFIX).equals(simpleDispatcherServlet.getNamespace()));
		assertTrue("Correct attribute", (FrameworkServlet.SERVLET_CONTEXT_PREFIX + "simple").equals(simpleDispatcherServlet.getServletContextAttributeName()));
		assertTrue("Context published", simpleDispatcherServlet.getWebApplicationContext() == servletConfig.getServletContext().getAttribute(FrameworkServlet.SERVLET_CONTEXT_PREFIX + "simple"));

		assertTrue("Correct namespace", "test".equals(complexDispatcherServlet.getNamespace()));
		assertTrue("Correct attribute", (FrameworkServlet.SERVLET_CONTEXT_PREFIX + "complex").equals(complexDispatcherServlet.getServletContextAttributeName()));
		assertTrue("Context not published", servletConfig.getServletContext().getAttribute(FrameworkServlet.SERVLET_CONTEXT_PREFIX + "complex") == null);
	}

	public void testInvalidRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/invalid.do");
		MockHttpServletResponse response = new MockHttpServletResponse();
		simpleDispatcherServlet.service(request, response);
		assertTrue("Not forwarded", response.getForwardedUrl() == null);
		assertTrue("correct error code", response.getStatus() == HttpServletResponse.SC_NOT_FOUND);
	}

	public void testFormRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/form.do");
		request.addPreferredLocale(Locale.CANADA);
		MockHttpServletResponse response = new MockHttpServletResponse();

		simpleDispatcherServlet.service(request, response);
		assertTrue("forwarded to form", "form".equals(response.getForwardedUrl()));
		DefaultMessageSourceResolvable resolvable = new DefaultMessageSourceResolvable(new String[] {"test"});
		RequestContext rc = new RequestContext(request);

		assertTrue("hasn't RequestContext attribute", request.getAttribute("rc") == null);
		assertTrue("Correct WebApplicationContext", RequestContextUtils.getWebApplicationContext(request) instanceof SimpleWebApplicationContext);
		assertTrue("Correct context path", rc.getContextPath().equals(request.getContextPath()));
		assertTrue("Correct locale", Locale.CANADA.equals(RequestContextUtils.getLocale(request)));
		assertTrue("Correct theme", AbstractThemeResolver.ORIGINAL_DEFAULT_THEME_NAME.equals(RequestContextUtils.getTheme(request).getName()));
		assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage("test")));

		assertTrue("Correct WebApplicationContext", rc.getWebApplicationContext() == simpleDispatcherServlet.getWebApplicationContext());
		assertTrue("Correct Errors", !(rc.getErrors(BaseCommandController.DEFAULT_COMMAND_NAME) instanceof EscapedErrors));
		assertTrue("Correct Errors", !(rc.getErrors(BaseCommandController.DEFAULT_COMMAND_NAME, false) instanceof EscapedErrors));
		assertTrue("Correct Errors", rc.getErrors(BaseCommandController.DEFAULT_COMMAND_NAME, true) instanceof EscapedErrors);
		assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage("test")));
		assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage("test", null, false)));
		assertTrue("Correct message", "Canadian &#38; test message".equals(rc.getMessage("test", null, true)));
		assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage(resolvable)));
		assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage(resolvable, false)));
		assertTrue("Correct message", "Canadian &#38; test message".equals(rc.getMessage(resolvable, true)));
		assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage("test", "default")));
		assertTrue("Correct message", "default".equals(rc.getMessage("testa", "default")));
		assertTrue("Correct message", "default &#38;".equals(rc.getMessage("testa", null, "default &", true)));
	}

	public void testLocaleRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.CANADA);
		MockHttpServletResponse response = new MockHttpServletResponse();
		assertEquals(98, simpleDispatcherServlet.getLastModified(request));
		simpleDispatcherServlet.service(request, response);
		assertTrue("Not forwarded", response.getForwardedUrl() == null);
	}

	public void testUnknownRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/unknown.do");
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.service(request, response);
		assertEquals("forwarded to failed", "failed0.jsp", response.getForwardedUrl());
		assertTrue("Exception exposed", request.getAttribute("exception").getClass().equals(ServletException.class));
	}

	public void testAnotherFormRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/form.do;jsessionid=xxx");
		request.addPreferredLocale(Locale.CANADA);
		MockHttpServletResponse response = new MockHttpServletResponse();

		complexDispatcherServlet.service(request, response);
		assertTrue("forwarded to form", "myform.jsp".equals(response.getForwardedUrl()));
		assertTrue("has RequestContext attribute", request.getAttribute("rc") != null);
		DefaultMessageSourceResolvable resolvable = new DefaultMessageSourceResolvable(new String[] {"test"});

		RequestContext rc = (RequestContext) request.getAttribute("rc");
		assertTrue("Not in HTML escaping mode", !rc.isDefaultHtmlEscape());
		assertTrue("Correct WebApplicationContext", rc.getWebApplicationContext() == complexDispatcherServlet.getWebApplicationContext());
		assertTrue("Correct context path", rc.getContextPath().equals(request.getContextPath()));
		assertTrue("Correct locale", Locale.CANADA.equals(rc.getLocale()));
		assertTrue("Correct Errors", !(rc.getErrors(BaseCommandController.DEFAULT_COMMAND_NAME) instanceof EscapedErrors));
		assertTrue("Correct Errors", !(rc.getErrors(BaseCommandController.DEFAULT_COMMAND_NAME, false) instanceof EscapedErrors));
		assertTrue("Correct Errors", rc.getErrors(BaseCommandController.DEFAULT_COMMAND_NAME, true) instanceof EscapedErrors);
		assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage("test")));
		assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage("test", null, false)));
		assertTrue("Correct message", "Canadian &#38; test message".equals(rc.getMessage("test", null, true)));
		assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage(resolvable)));
		assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage(resolvable, false)));
		assertTrue("Correct message", "Canadian &#38; test message".equals(rc.getMessage(resolvable, true)));

		rc.setDefaultHtmlEscape(true);
		assertTrue("Is in HTML escaping mode", rc.isDefaultHtmlEscape());
		assertTrue("Correct Errors", rc.getErrors(BaseCommandController.DEFAULT_COMMAND_NAME) instanceof EscapedErrors);
		assertTrue("Correct Errors", !(rc.getErrors(BaseCommandController.DEFAULT_COMMAND_NAME, false) instanceof EscapedErrors));
		assertTrue("Correct Errors", rc.getErrors(BaseCommandController.DEFAULT_COMMAND_NAME, true) instanceof EscapedErrors);
		assertTrue("Correct message", "Canadian &#38; test message".equals(rc.getMessage("test")));
		assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage("test", null, false)));
		assertTrue("Correct message", "Canadian &#38; test message".equals(rc.getMessage("test", null, true)));
		assertTrue("Correct message", "Canadian &#38; test message".equals(rc.getMessage(resolvable)));
		assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage(resolvable, false)));
		assertTrue("Correct message", "Canadian &#38; test message".equals(rc.getMessage(resolvable, true)));
	}

	public void testAnotherLocaleRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do;abc=def");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		MockHttpServletResponse response = new MockHttpServletResponse();
		assertEquals(99, complexDispatcherServlet.getLastModified(request));
		complexDispatcherServlet.service(request, response);
		assertTrue("Not forwarded", response.getForwardedUrl() == null);
		assertTrue(request.getAttribute("test1") != null);
		assertTrue(request.getAttribute("test1x") == null);
		assertTrue(request.getAttribute("test1y") == null);
		assertTrue(request.getAttribute("test2") != null);
		assertTrue(request.getAttribute("test2x") == null);
		assertTrue(request.getAttribute("test2y") == null);
	}

	public void testExistingMultipartRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do;abc=def");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ComplexWebApplicationContext.MockMultipartResolver multipartResolver =
				(ComplexWebApplicationContext.MockMultipartResolver) complexDispatcherServlet.getWebApplicationContext().getBean("multipartResolver");
		MultipartHttpServletRequest multipartRequest = multipartResolver.resolveMultipart(request);
		complexDispatcherServlet.service(multipartRequest, response);
		//System.out.println(response.getForwardedUrl());
		multipartResolver.cleanupMultipart(multipartRequest);
		assertTrue(multipartResolver.cleaned);
	}

	public void testMultipartResolutionFailed() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do;abc=def");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		request.setAttribute("fail", Boolean.TRUE);
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.service(request, response);
		assertTrue("forwarded to failed", "failed0.jsp".equals(response.getForwardedUrl()));
		assertTrue("correct exception", request.getAttribute(SimpleMappingExceptionResolver.DEFAULT_EXCEPTION_ATTRIBUTE) instanceof MaxUploadSizeExceededException);
	}

	public void testHandlerInterceptorAbort() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addParameter("abort", "true");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.service(request, response);
		assertTrue("Not forwarded", response.getForwardedUrl() == null);
		assertTrue(request.getAttribute("test1") != null);
		assertTrue(request.getAttribute("test1x") != null);
		assertTrue(request.getAttribute("test1y") == null);
		assertTrue(request.getAttribute("test2") == null);
		assertTrue(request.getAttribute("test2x") == null);
		assertTrue(request.getAttribute("test2y") == null);
	}

	public void testModelAndViewDefiningException() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		request.addParameter("fail", "yes");
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			complexDispatcherServlet.service(request, response);
			assertTrue("forwarded to failed", "failed1.jsp".equals(response.getForwardedUrl()));
		}
		catch (ServletException ex) {
			fail("Should not have thrown ServletException: " + ex.getMessage());
		}
	}

	public void testSimpleMappingExceptionResolverWithSpecificHandler1() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		request.addParameter("access", "yes");
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.service(request, response);
		assertEquals("forwarded to failed", "failed2.jsp", response.getForwardedUrl());
		assertTrue("Exception exposed", request.getAttribute("exception") instanceof IllegalAccessException);
	}

	public void testSimpleMappingExceptionResolverWithSpecificHandler2() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		request.addParameter("servlet", "yes");
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.service(request, response);
		assertEquals("forwarded to failed", "failed3.jsp", response.getForwardedUrl());
		assertTrue("Exception exposed", request.getAttribute("exception") instanceof ServletException);
	}

	public void testSimpleMappingExceptionResolverWithAllHandlers1() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/loc.do");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		request.addParameter("access", "yes");
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.service(request, response);
		assertEquals("forwarded to failed", "failed1.jsp", response.getForwardedUrl());
		assertTrue("Exception exposed", request.getAttribute("exception") instanceof IllegalAccessException);
	}

	public void testSimpleMappingExceptionResolverWithAllHandlers2() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/loc.do");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		request.addParameter("servlet", "yes");
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.service(request, response);
		assertEquals("forwarded to failed", "failed1.jsp", response.getForwardedUrl());
		assertTrue("Exception exposed", request.getAttribute("exception") instanceof ServletException);
	}

	public void testSimpleMappingExceptionResolverWithDefaultErrorView() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		request.addParameter("exception", "yes");
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.service(request, response);
		assertEquals("forwarded to failed", "failed0.jsp", response.getForwardedUrl());
		assertTrue("Exception exposed", request.getAttribute("exception").getClass().equals(RuntimeException.class));
	}

	public void testLocaleChangeInterceptor1() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.GERMAN);
		request.addRole("role2");
		request.addParameter("locale", "en");
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.service(request, response);
		assertEquals("forwarded to failed", "failed0.jsp", response.getForwardedUrl());
		assertTrue("Exception exposed", request.getAttribute("exception").getClass().equals(ServletException.class));
	}

	public void testLocaleChangeInterceptor2() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.GERMAN);
		request.addRole("role2");
		request.addParameter("locale", "en");
		request.addParameter("locale2", "en_CA");
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.service(request, response);
		assertTrue("Not forwarded", response.getForwardedUrl() == null);
	}

	public void testThemeChangeInterceptor1() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		request.addParameter("theme", "mytheme");
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.service(request, response);
		assertEquals("forwarded to failed", "failed0.jsp", response.getForwardedUrl());
		assertTrue("Exception exposed", request.getAttribute("exception").getClass().equals(ServletException.class));
	}

	public void testThemeChangeInterceptor2() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		request.addParameter("theme", "mytheme");
		request.addParameter("theme2", "theme");
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			complexDispatcherServlet.service(request, response);
			assertTrue("Not forwarded", response.getForwardedUrl() == null);
		}
		catch (ServletException ex) {
			fail("Should not have thrown ServletException: " + ex.getMessage());
		}
	}

	public void testNotAuthorized() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.CANADA);
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			complexDispatcherServlet.service(request, response);
			assertTrue("Correct response", response.getStatus() == HttpServletResponse.SC_FORBIDDEN);
		}
		catch (ServletException ex) {
			fail("Should not have thrown ServletException: " + ex.getMessage());
		}
	}

	public void testNotDetectAllHandlerMappings() throws ServletException, IOException {
		DispatcherServlet complexDispatcherServlet = new DispatcherServlet();
		complexDispatcherServlet.setContextClass(ComplexWebApplicationContext.class);
		complexDispatcherServlet.setNamespace("test");
		complexDispatcherServlet.setDetectAllHandlerMappings(false);
		complexDispatcherServlet.init(new MockServletConfig(servletConfig.getServletContext(), "complex"));

		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/unknown.do");
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.service(request, response);
		assertTrue(response.getStatus() == HttpServletResponse.SC_NOT_FOUND);
	}
	
	
	public void testHandlerNotMappedWithAutodetect() throws ServletException, IOException {
		DispatcherServlet complexDispatcherServlet = new DispatcherServlet();
		// No parent
		complexDispatcherServlet.setContextClass(ComplexWebApplicationContext.class);
		complexDispatcherServlet.setNamespace("test");
		complexDispatcherServlet.init(new MockServletConfig(servletConfig.getServletContext(), "complex"));

		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", 
				URL_KNOWN_ONLY_PARENT);
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.service(request, response);
		assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
	}
	
	public void testDetectHandlerMappingFromParent() throws ServletException, IOException {
		// Create a parent context that includes a mapping
		StaticWebApplicationContext parent = new StaticWebApplicationContext();
		parent.registerSingleton("parentHandler", ControllerFromParent.class, new MutablePropertyValues());
		
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("mappings", URL_KNOWN_ONLY_PARENT + "=parentHandler"));
		
		parent.registerSingleton("parentMapping", SimpleUrlHandlerMapping.class, pvs);
		parent.refresh();

		DispatcherServlet complexDispatcherServlet = new DispatcherServlet();
		// Will have parent
		complexDispatcherServlet.setContextClass(ComplexWebApplicationContext.class);
		complexDispatcherServlet.setNamespace("test");
		
		ServletConfig config = new MockServletConfig(servletConfig.getServletContext(), "complex");
		config.getServletContext().setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, parent);
		complexDispatcherServlet.init(config);

		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", 
				URL_KNOWN_ONLY_PARENT);
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.service(request, response);
		
		assertFalse("Matched through parent controller/handler pair: not response=" + response.getStatus(),
				response.getStatus() == HttpServletResponse.SC_NOT_FOUND);
	}

	public static class ControllerFromParent implements Controller {
		
		public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
			return new ModelAndView(ControllerFromParent.class.getName());
		}
	}
	
	public void testNotDetectAllHandlerExceptionResolvers() throws ServletException, IOException {
		DispatcherServlet complexDispatcherServlet = new DispatcherServlet();
		complexDispatcherServlet.setContextClass(ComplexWebApplicationContext.class);
		complexDispatcherServlet.setNamespace("test");
		complexDispatcherServlet.setDetectAllHandlerExceptionResolvers(false);
		complexDispatcherServlet.init(new MockServletConfig(servletConfig.getServletContext(), "complex"));

		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/unknown.do");
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			complexDispatcherServlet.service(request, response);
			fail("Should have thrown ServletException");
		}
		catch (ServletException ex) {
			// expected
			assertTrue(ex.getMessage().indexOf("No adapter for handler") != -1);
		}
	}

	public void testNotDetectAllViewResolvers() throws ServletException, IOException {
		DispatcherServlet complexDispatcherServlet = new DispatcherServlet();
		complexDispatcherServlet.setContextClass(ComplexWebApplicationContext.class);
		complexDispatcherServlet.setNamespace("test");
		complexDispatcherServlet.setDetectAllViewResolvers(false);
		complexDispatcherServlet.init(new MockServletConfig(servletConfig.getServletContext(), "complex"));

		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/unknown.do");
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			complexDispatcherServlet.service(request, response);
			fail("Should have thrown ServletException");
		}
		catch (ServletException ex) {
			// expected
			assertTrue(ex.getMessage().indexOf("failed0") != -1);
		}
	}

	public void testThrowawayController() throws Exception {
		SimpleWebApplicationContext.TestThrowawayController.counter = 0;
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/throwaway.do");
		request.addParameter("myInt", "5");
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			simpleDispatcherServlet.service(request, response);
			assertTrue("Correct response", "view5".equals(response.getForwardedUrl()));
			assertEquals(1, SimpleWebApplicationContext.TestThrowawayController.counter);
		}
		catch (ServletException ex) {
			fail("Should not have thrown ServletException: " + ex.getMessage());
		}
	}

	public void testThrowawayControllerWithBindingFailure() throws Exception {
		SimpleWebApplicationContext.TestThrowawayController.counter = 0;
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/throwaway.do");
		request.addParameter("myInt", "5x");
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			simpleDispatcherServlet.service(request, response);
			fail("Should have thrown ServletException");
		}
		catch (ServletException ex) {
			// expected
			assertTrue(ex.getRootCause() instanceof BindException);
			assertEquals(1, SimpleWebApplicationContext.TestThrowawayController.counter);
		}
	}

	public void testWebApplicationContextLookup() {
		MockServletContext servletContext = new MockServletContext();
		MockHttpServletRequest request = new MockHttpServletRequest(servletContext, "GET", "/invalid.do");

		try {
			RequestContextUtils.getWebApplicationContext(request);
			fail("Should have thrown IllegalStateException");
		}
		catch (IllegalStateException ex) {
			// expected
		}

		try {
			RequestContextUtils.getWebApplicationContext(request, servletContext);
			fail("Should have thrown IllegalStateException");
		}
		catch (IllegalStateException ex) {
			// expected
		}

		servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, new StaticWebApplicationContext());
		try {
			RequestContextUtils.getWebApplicationContext(request, servletContext);
		}
		catch (IllegalStateException ex) {
			fail("Should not have thrown IllegalStateException: " + ex.getMessage());
		}
	}

}
