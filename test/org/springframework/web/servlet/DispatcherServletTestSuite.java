package org.springframework.web.servlet;

import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.springframework.context.support.MessageSourceResolvableImpl;
import org.springframework.web.bind.EscapedErrors;
import org.springframework.web.context.RootWebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.mock.MockHttpServletResponse;
import org.springframework.web.mock.MockServletConfig;
import org.springframework.web.mock.MockServletContext;
import org.springframework.web.servlet.mvc.BaseCommandController;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.theme.AbstractThemeResolver;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $RevisionId$
 */
public class DispatcherServletTestSuite extends TestCase {

	private ServletConfig servletConfig;
	
	private DispatcherServlet simpleControllerServlet;

	private DispatcherServlet complexControllerServlet;

	public DispatcherServletTestSuite() throws ServletException {
		servletConfig = new MockServletConfig(new MockServletContext(), "simple");

		simpleControllerServlet = new DispatcherServlet();
		simpleControllerServlet.setContextClass(SimpleWebApplicationContext.class);
		simpleControllerServlet.init(servletConfig);

		complexControllerServlet = new DispatcherServlet();
		complexControllerServlet.setContextClass(ComplexWebApplicationContext.class);
		complexControllerServlet.setNamespace("test");
		complexControllerServlet.setPublishContext(false);
		complexControllerServlet.init(new MockServletConfig(servletConfig.getServletContext(), "complex"));
	}

	public void testControllerServlets() {
		assertTrue("Correct namespace", ("simple" + FrameworkServlet.DEFAULT_NAMESPACE_SUFFIX).equals(simpleControllerServlet.getNamespace()));
		assertTrue("Correct attribute", (FrameworkServlet.SERVLET_CONTEXT_PREFIX + "simple").equals(simpleControllerServlet.getServletContextAttributeName()));
		assertTrue("Context published", simpleControllerServlet.getWebApplicationContext() == servletConfig.getServletContext().getAttribute(FrameworkServlet.SERVLET_CONTEXT_PREFIX + "simple"));

		assertTrue("Correct namespace", "test".equals(complexControllerServlet.getNamespace()));
		assertTrue("Correct attribute", (FrameworkServlet.SERVLET_CONTEXT_PREFIX + "complex").equals(complexControllerServlet.getServletContextAttributeName()));
		assertTrue("Context not published", servletConfig.getServletContext().getAttribute(FrameworkServlet.SERVLET_CONTEXT_PREFIX + "complex") == null);
	}

	public void testInvalidRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/invalid.do");
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			simpleControllerServlet.doGet(request, response);
			assertTrue("Not forwarded", response.forwarded == null);
			assertTrue("correct error code", response.getStatusCode() == HttpServletResponse.SC_NOT_FOUND);
		}
		catch (ServletException ex) {
			fail("Should not have thrown ServletException: " + ex.getMessage());
		}
	}

	public void testFormRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/form.do");
		request.addPreferredLocale(Locale.CANADA);
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			simpleControllerServlet.doGet(request, response);
			assertTrue("forwarded to form", "form".equals(response.forwarded));
			MessageSourceResolvableImpl resolvable = new MessageSourceResolvableImpl(new String[] {"test"}, null);
			RequestContext rc = new RequestContext(request);

			assertTrue("hasn't RequestContext attribute", request.getAttribute("rc") == null);
			assertTrue("Correct WebApplicationContext", RequestContextUtils.getWebApplicationContext(request) instanceof SimpleWebApplicationContext);
			assertTrue("Correct context path", rc.getContextPath().equals(request.getContextPath()));
			assertTrue("Correct locale", Locale.CANADA.equals(RequestContextUtils.getLocale(request)));
			assertTrue("Correct theme", AbstractThemeResolver.ORIGINAL_DEFAULT_THEME_NAME.equals(RequestContextUtils.getTheme(request).getName()));
			assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage("test")));

			assertTrue("Correct WebApplicationContext", rc.getWebApplicationContext() == simpleControllerServlet.getWebApplicationContext());
			assertTrue("Correct Errors", !(rc.getErrors(BaseCommandController.DEFAULT_COMMAND_NAME) instanceof EscapedErrors));
			assertTrue("Correct Errors", !(rc.getErrors(BaseCommandController.DEFAULT_COMMAND_NAME, false) instanceof EscapedErrors));
			assertTrue("Correct Errors", rc.getErrors(BaseCommandController.DEFAULT_COMMAND_NAME, true) instanceof EscapedErrors);
			assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage("test")));
			assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage("test", null, false)));
			assertTrue("Correct message", "Canadian &amp; test message".equals(rc.getMessage("test", null, true)));
			assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage(resolvable)));
			assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage(resolvable, false)));
			assertTrue("Correct message", "Canadian &amp; test message".equals(rc.getMessage(resolvable, true)));
			assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage("test", "default")));
			assertTrue("Correct message", "default".equals(rc.getMessage("testa", "default")));
			assertTrue("Correct message", "default &amp;".equals(rc.getMessage("testa", null, "default &", true)));
		}
		catch (ServletException ex) {
			fail("Should not have thrown ServletException: " + ex.getMessage());
		}
	}

	public void testLocaleRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.CANADA);
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			assertEquals(98, simpleControllerServlet.getLastModified(request));
			simpleControllerServlet.doGet(request, response);
			assertTrue("Not forwarded", response.forwarded == null);
		}
		catch (ServletException ex) {
			fail("Should not have thrown ServletException: " + ex.getMessage());
		}
	}

	public void testUnknownRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/unknown.do");
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			complexControllerServlet.doGet(request, response);
			fail("Should have thrown ServletException");
		}
		catch (ServletException ex) {
			// expected
		}
	}

	public void testAnotherFormRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/form.do;jsessionid=xxx");
		request.addPreferredLocale(Locale.CANADA);
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			complexControllerServlet.doGet(request, response);
			assertTrue("forwarded to form", "myform.jsp".equals(response.forwarded));
			assertTrue("has RequestContext attribute", request.getAttribute("rc") != null);
			MessageSourceResolvableImpl resolvable = new MessageSourceResolvableImpl(new String[] {"test"}, null);

			RequestContext rc = (RequestContext) request.getAttribute("rc");
			assertTrue("Not in HTML escaping mode", !rc.isDefaultHtmlEscape());
			assertTrue("Correct WebApplicationContext", rc.getWebApplicationContext() == complexControllerServlet.getWebApplicationContext());
			assertTrue("Correct context path", rc.getContextPath().equals(request.getContextPath()));
			assertTrue("Correct locale", Locale.CANADA.equals(rc.getLocale()));
			assertTrue("Correct Errors", !(rc.getErrors(BaseCommandController.DEFAULT_COMMAND_NAME) instanceof EscapedErrors));
			assertTrue("Correct Errors", !(rc.getErrors(BaseCommandController.DEFAULT_COMMAND_NAME, false) instanceof EscapedErrors));
			assertTrue("Correct Errors", rc.getErrors(BaseCommandController.DEFAULT_COMMAND_NAME, true) instanceof EscapedErrors);
			assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage("test")));
			assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage("test", null, false)));
			assertTrue("Correct message", "Canadian &amp; test message".equals(rc.getMessage("test", null, true)));
			assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage(resolvable)));
			assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage(resolvable, false)));
			assertTrue("Correct message", "Canadian &amp; test message".equals(rc.getMessage(resolvable, true)));

			rc.setDefaultHtmlEscape(true);
			assertTrue("Is in HTML escaping mode", rc.isDefaultHtmlEscape());
			assertTrue("Correct Errors", rc.getErrors(BaseCommandController.DEFAULT_COMMAND_NAME) instanceof EscapedErrors);
			assertTrue("Correct Errors", !(rc.getErrors(BaseCommandController.DEFAULT_COMMAND_NAME, false) instanceof EscapedErrors));
			assertTrue("Correct Errors", rc.getErrors(BaseCommandController.DEFAULT_COMMAND_NAME, true) instanceof EscapedErrors);
			assertTrue("Correct message", "Canadian &amp; test message".equals(rc.getMessage("test")));
			assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage("test", null, false)));
			assertTrue("Correct message", "Canadian &amp; test message".equals(rc.getMessage("test", null, true)));
			assertTrue("Correct message", "Canadian &amp; test message".equals(rc.getMessage(resolvable)));
			assertTrue("Correct message", "Canadian & test message".equals(rc.getMessage(resolvable, false)));
			assertTrue("Correct message", "Canadian &amp; test message".equals(rc.getMessage(resolvable, true)));
		}
		catch (ServletException ex) {
			fail("Should not have thrown ServletException: " + ex.getMessage());
		}
	}

	public void testAnotherLocaleRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do;abc=def");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			assertEquals(99, complexControllerServlet.getLastModified(request));
			complexControllerServlet.doGet(request, response);
			assertTrue("Not forwarded", response.forwarded == null);

			assertTrue(request.getAttribute("test1") != null);
			assertTrue(request.getAttribute("test1x") == null);
			assertTrue(request.getAttribute("test1y") == null);
			assertTrue(request.getAttribute("test2") != null);
			assertTrue(request.getAttribute("test2x") == null);
			assertTrue(request.getAttribute("test2y") == null);
		}
		catch (ServletException ex) {
			fail("Should not have thrown ServletException: " + ex.getMessage());
		}
	}

	public void testModelAndViewDefiningException() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		request.addParameter("fail", "yes");
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			complexControllerServlet.doGet(request, response);
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
		try {
			complexControllerServlet.doGet(request, response);
			assertEquals("forwarded to failed", "failed2.jsp", response.forwarded);
			assertTrue("Exception exposed", request.getAttribute("exception") instanceof IllegalAccessException);
		}
		catch (ServletException ex) {
			fail("Should not have thrown ServletException: " + ex.getMessage());
		}
	}

	public void testSimpleMappingExceptionResolverWithSpecificHandler2() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		request.addParameter("servlet", "yes");
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			complexControllerServlet.doGet(request, response);
			assertEquals("forwarded to failed", "failed3.jsp", response.forwarded);
			assertTrue("Exception exposed", request.getAttribute("exception") instanceof ServletException);
		}
		catch (ServletException ex) {
			fail("Should not have thrown ServletException: " + ex.getMessage());
		}
	}

	public void testSimpleMappingExceptionResolverWithAllHandlers1() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/loc.do");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		request.addParameter("access", "yes");
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			complexControllerServlet.doGet(request, response);
			assertEquals("forwarded to failed", "failed1.jsp", response.forwarded);
			assertTrue("Exception exposed", request.getAttribute("exception") instanceof IllegalAccessException);
		}
		catch (ServletException ex) {
			fail("Should not have thrown ServletException: " + ex.getMessage());
		}
	}

	public void testSimpleMappingExceptionResolverWithAllHandlers2() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/loc.do");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		request.addParameter("servlet", "yes");
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			complexControllerServlet.doGet(request, response);
			assertEquals("forwarded to failed", "failed1.jsp", response.forwarded);
			assertTrue("Exception exposed", request.getAttribute("exception") instanceof ServletException);
		}
		catch (ServletException ex) {
			fail("Should not have thrown ServletException: " + ex.getMessage());
		}
	}

	public void testLocaleChangeInterceptor1() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.GERMAN);
		request.addRole("role2");
		request.addParameter("locale", "en");
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			complexControllerServlet.doGet(request, response);
			assertTrue("Not forwarded", response.forwarded == null);
			fail("Should have thrown ServletException");
		}
		catch (ServletException ex) {
			// expected
		}
	}

	public void testLocaleChangeInterceptor2() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.GERMAN);
		request.addRole("role2");
		request.addParameter("locale", "en");
		request.addParameter("locale2", "en_CA");
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			complexControllerServlet.doGet(request, response);
			assertTrue("Not forwarded", response.forwarded == null);
		}
		catch (ServletException ex) {
			fail("Should not have thrown ServletException: " + ex.getMessage());
		}
	}

	public void testThemeChangeInterceptor1() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		request.addParameter("theme", "mytheme");
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			complexControllerServlet.doGet(request, response);
			assertTrue("Not forwarded", response.forwarded == null);
			fail("Should have thrown ServletException");
		}
		catch (ServletException ex) {
			// expected
		}
	}

	public void testThemeChangeInterceptor2() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(servletConfig.getServletContext(), "GET", "/locale.do");
		request.addPreferredLocale(Locale.CANADA);
		request.addRole("role1");
		request.addParameter("theme", "mytheme");
		request.addParameter("theme2", "theme");
		MockHttpServletResponse response = new MockHttpServletResponse();
		try {
			complexControllerServlet.doGet(request, response);
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
			complexControllerServlet.doGet(request, response);
			assertTrue("Correct response", response.getStatusCode() == HttpServletResponse.SC_FORBIDDEN);
		}
		catch (ServletException ex) {
			fail("Should not have thrown ServletException: " + ex.getMessage());
		}
	}

	public void testWebApplicationContextLookup() {
		MockServletContext servletContext = new MockServletContext();
		MockHttpServletRequest request = new MockHttpServletRequest(servletContext, "GET", "/invalid.do");

		try {
			RequestContextUtils.getWebApplicationContext(request);
			fail("Should have thrown ServletException");
		}
		catch (ServletException ex) {
			// expected
		}

		try {
			RequestContextUtils.getWebApplicationContext(request, servletContext);
			fail("Should have thrown ServletException");
		}
		catch (ServletException ex) {
			// expected
		}

		servletContext.setAttribute(RootWebApplicationContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE_NAME, new StaticWebApplicationContext());
		try {
			RequestContextUtils.getWebApplicationContext(request, servletContext);
		}
		catch (ServletException ex) {
			fail("Should not have thrown ServletException: " + ex.getMessage());
		}
	}

}
