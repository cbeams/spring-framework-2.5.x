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

import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.web.bind.EscapedErrors;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.mock.MockHttpServletResponse;
import org.springframework.web.mock.MockServletConfig;
import org.springframework.web.mock.MockServletContext;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.BaseCommandController;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.theme.AbstractThemeResolver;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class DispatcherServletTestSuite extends TestCase {

	private ServletConfig servletConfig;
	
	private DispatcherServlet simpleDispatcherServlet;

	private DispatcherServlet complexDispatcherServlet;

	protected void setUp() throws ServletException {
		servletConfig = new MockServletConfig(new MockServletContext(), "simple");

		simpleDispatcherServlet = new DispatcherServlet();
		simpleDispatcherServlet.setContextClass(SimpleWebApplicationContext.class);
		simpleDispatcherServlet.init(servletConfig);

		complexDispatcherServlet = new DispatcherServlet();
		complexDispatcherServlet.setContextClass(ComplexWebApplicationContext.class);
		complexDispatcherServlet.setNamespace("test");
		complexDispatcherServlet.setPublishContext(false);
		complexDispatcherServlet.init(new MockServletConfig(servletConfig.getServletContext(), "complex"));
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
		simpleDispatcherServlet.doGet(request, response);
		assertTrue("Not forwarded", response.forwarded == null);
		assertTrue("correct error code", response.getStatusCode() == HttpServletResponse.SC_NOT_FOUND);
	}

	public void testFormRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/form.do");
		request.addPreferredLocale(Locale.CANADA);
		MockHttpServletResponse response = new MockHttpServletResponse();

		simpleDispatcherServlet.doGet(request, response);
		assertTrue("forwarded to form", "form".equals(response.forwarded));
		DefaultMessageSourceResolvable resolvable = new DefaultMessageSourceResolvable(new String[] {"test"}, null);
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
		simpleDispatcherServlet.doGet(request, response);
		assertTrue("Not forwarded", response.forwarded == null);
	}

	public void testUnknownRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/unknown.do");
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.doGet(request, response);
		assertEquals("forwarded to failed", "failed0.jsp", response.forwarded);
		assertTrue("Exception exposed", request.getAttribute("exception").getClass().equals(ServletException.class));
	}

	public void testAnotherFormRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/form.do;jsessionid=xxx");
		request.addPreferredLocale(Locale.CANADA);
		MockHttpServletResponse response = new MockHttpServletResponse();

		complexDispatcherServlet.doGet(request, response);
		assertTrue("forwarded to form", "myform.jsp".equals(response.forwarded));
		assertTrue("has RequestContext attribute", request.getAttribute("rc") != null);
		DefaultMessageSourceResolvable resolvable = new DefaultMessageSourceResolvable(new String[] {"test"}, null);

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
		complexDispatcherServlet.doGet(request, response);
		assertTrue("Not forwarded", response.forwarded == null);
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
		complexDispatcherServlet.doGet(multipartRequest, response);
		multipartResolver.cleanupMultipart(multipartRequest);
		assertTrue(multipartResolver.cleaned);
	}

	public void testHandlerInterceptorAbort() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addParameter("abort", "true");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.doGet(request, response);
		assertTrue("Not forwarded", response.forwarded == null);
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
			complexDispatcherServlet.doGet(request, response);
			assertTrue("forwarded to failed", "failed1.jsp".equals(response.forwarded));
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
		complexDispatcherServlet.doGet(request, response);
		assertEquals("forwarded to failed", "failed2.jsp", response.forwarded);
		assertTrue("Exception exposed", request.getAttribute("exception") instanceof IllegalAccessException);
	}

	public void testSimpleMappingExceptionResolverWithSpecificHandler2() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		request.addParameter("servlet", "yes");
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.doGet(request, response);
		assertEquals("forwarded to failed", "failed3.jsp", response.forwarded);
		assertTrue("Exception exposed", request.getAttribute("exception") instanceof ServletException);
	}

	public void testSimpleMappingExceptionResolverWithAllHandlers1() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/loc.do");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		request.addParameter("access", "yes");
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.doGet(request, response);
		assertEquals("forwarded to failed", "failed1.jsp", response.forwarded);
		assertTrue("Exception exposed", request.getAttribute("exception") instanceof IllegalAccessException);
	}

	public void testSimpleMappingExceptionResolverWithAllHandlers2() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/loc.do");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		request.addParameter("servlet", "yes");
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.doGet(request, response);
		assertEquals("forwarded to failed", "failed1.jsp", response.forwarded);
		assertTrue("Exception exposed", request.getAttribute("exception") instanceof ServletException);
	}

	public void testSimpleMappingExceptionResolverWithDefaultErrorView() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		request.addParameter("exception", "yes");
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.doGet(request, response);
		assertEquals("forwarded to failed", "failed0.jsp", response.forwarded);
		assertTrue("Exception exposed", request.getAttribute("exception").getClass().equals(RuntimeException.class));
	}

	public void testLocaleChangeInterceptor1() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.GERMAN);
		request.addRole("role2");
		request.addParameter("locale", "en");
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.doGet(request, response);
		assertEquals("forwarded to failed", "failed0.jsp", response.forwarded);
		assertTrue("Exception exposed", request.getAttribute("exception").getClass().equals(ServletException.class));
	}

	public void testLocaleChangeInterceptor2() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.GERMAN);
		request.addRole("role2");
		request.addParameter("locale", "en");
		request.addParameter("locale2", "en_CA");
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.doGet(request, response);
		assertTrue("Not forwarded", response.forwarded == null);
	}

	public void testThemeChangeInterceptor1() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		request.addParameter("theme", "mytheme");
		MockHttpServletResponse response = new MockHttpServletResponse();
		complexDispatcherServlet.doGet(request, response);
		assertEquals("forwarded to failed", "failed0.jsp", response.forwarded);
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
			complexDispatcherServlet.doGet(request, response);
			assertTrue("Not forwarded", response.forwarded == null);
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
			complexDispatcherServlet.doGet(request, response);
			assertTrue("Correct response", response.getStatusCode() == HttpServletResponse.SC_FORBIDDEN);
		}
		catch (ServletException ex) {
			fail("Should not have thrown ServletException: " + ex.getMessage());
		}
	}

	public void testThrowawayController() throws Exception {
		SimpleWebApplicationContext.TestThrowawayController.counter = 0;
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/throwaway.do");
		request.addParameter("myInt", "5");
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			simpleDispatcherServlet.doGet(request, response);
			assertTrue("Correct response", "view5".equals(response.forwarded));
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
			simpleDispatcherServlet.doGet(request, response);
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
