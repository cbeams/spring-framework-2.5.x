/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.servlet.view.velocity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.easymock.MockControl;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import com.mockobjects.servlet.MockHttpServletResponse;

/**
 * @author Rod Johnson
 * @version $Id: VelocityViewTests.java,v 1.5 2003-11-04 23:10:05 jhoeller Exp $
 */
public class VelocityViewTests extends TestCase {

	public void testNoVelocityConfiguration() throws Exception {
		VelocityView vv = new VelocityView();
		MockControl wmc = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wmc.getMock();
		wac.getBeansOfType(VelocityConfiguration.class, true, true);
		wmc.setReturnValue(new HashMap());
		wac.getParentBeanFactory();
		wmc.setReturnValue(null);
		wmc.replay();

		vv.setTemplateName("anythingButNull");
		try {
			vv.setApplicationContext(wac);
			fail();
		}
		catch (ApplicationContextException ex) {
			// Check there's a helpful error message
			assertTrue(ex.getMessage().indexOf("VelocityConfiguration") != -1);
		}

		wmc.verify();
	}

	public void testNoTemplateName() throws Exception {
		VelocityView vv = new VelocityView();
		MockControl wmc = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wmc.getMock();
		// Expect no calls
		wmc.replay();

		try {
			vv.setApplicationContext(wac);
			fail();
		}
		catch (ApplicationContextException ex) {
			// Check there's a helpful error message
			assertTrue(ex.getMessage().indexOf("templateName") != -1);
		}

		wmc.verify();
	}

	public void testCannotResolveTemplateNameResourceNotFoundException() throws Exception {
		testCannotResolveTemplateName(new ResourceNotFoundException(""));
	}

	public void testCannotResolveTemplateNameParseErrorException() throws Exception {
		testCannotResolveTemplateName(new ParseErrorException(""));
	}

	public void testCannotResolveTemplateNameNonspecificException() throws Exception {
		testCannotResolveTemplateName(new Exception(""));
	}

	/**
	 * Check for failure to lookup a template for a range of reasons
	 */
	private void testCannotResolveTemplateName(final Exception templateLookupException) throws Exception {
		final String templateName = "test.vm";

		MockControl wmc = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wmc.getMock();
		wac.getParentBeanFactory();
		wmc.setReturnValue(null);
		VelocityConfiguration vc = new VelocityConfiguration() {
			public VelocityEngine getVelocityEngine() {
				return new VelocityEngine() {
					public Template getTemplate(String tn)
						throws ResourceNotFoundException, ParseErrorException, Exception {
						assertEquals(tn, templateName);
						throw templateLookupException;
					}
				};
			}
		};
		wac.getBeansOfType(VelocityConfiguration.class, true, true);
		Map configurers = new HashMap();
		configurers.put("velocityConfigurer", vc);
		wmc.setReturnValue(configurers);
		wmc.replay();

		VelocityView vv = new VelocityView();
		//vv.setExposeDateFormatter(false);
		//vv.setExposeCurrencyFormatter(false);
		vv.setTemplateName(templateName);

		try {
			vv.setApplicationContext(wac);
			fail();
		}
		catch (ApplicationContextException ex) {
			assertEquals(ex.getRootCause(), templateLookupException);
		}

		wmc.verify();
	}
	
	public void testMergeTemplateSucceeds() throws Exception {
		testValidTemplateName(null);
	}
	
	public void testMergeTemplateFailureWithIOException() throws Exception {
		testValidTemplateName(new IOException());
	}
	
	public void testMergeTemplateFailureWithParseErrorException() throws Exception {
		testValidTemplateName(new ParseErrorException(""));
	}
		
	public void testMergeTemplateFailureWithUnspecifiedException() throws Exception {
		testValidTemplateName(new Exception(""));
	}

	/**
	 * 
	 * @param mergeTemplateFailureException may be null in which case mergeTemplate override will succeed.
	 * If it's non null it will be checked
	 * @throws Exception
	 */
	private void testValidTemplateName(final Exception mergeTemplateFailureException) throws Exception {
		Map model = new HashMap();
		model.put("foo", "bar");
		// This should be escaped to get rid of the illegal .
		model.put("has.dot", "escaped");

		final String templateName = "test.vm";

		MockControl wmc = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wmc.getMock();
		wac.getParentBeanFactory();
		wmc.setReturnValue(null);
		final Template expectedTemplate = new Template();
		VelocityConfiguration vc = new VelocityConfiguration() {
			public VelocityEngine getVelocityEngine() {
				return new TestVelocityEngine(templateName, expectedTemplate);
			}
		};
		wac.getBeansOfType(VelocityConfiguration.class, true, true);
		Map configurers = new HashMap();
		configurers.put("velocityConfigurer", vc);
		wmc.setReturnValue(configurers);
		wmc.replay();

		// Let it ask for locale
		MockControl reqControl = MockControl.createControl(HttpServletRequest.class);
		HttpServletRequest req = (HttpServletRequest) reqControl.getMock();
		req.getAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE);
		reqControl.setReturnValue(new AcceptHeaderLocaleResolver());
		req.getLocale();
		reqControl.setReturnValue(Locale.CANADA);
		reqControl.replay();

		final HttpServletResponse expectedResponse = new MockHttpServletResponse();

		VelocityView vv = new VelocityView() {
			protected void mergeTemplate(Template template, Context context, HttpServletResponse response) throws Exception {
				assertTrue(template == expectedTemplate);
				assertTrue(context.getKeys().length >= 1);
				assertTrue(context.get("foo").equals("bar"));
				// Check escaping of illegal dots
				assertTrue("escaped".equals(context.get("has_dot")));
				assertTrue(response == expectedResponse);
				if (mergeTemplateFailureException != null) {
					throw mergeTemplateFailureException;
				}
			}

		};
		//vv.setExposeDateFormatter(false);
		//vv.setExposeCurrencyFormatter(false);
		vv.setTemplateName(templateName);
		vv.setApplicationContext(wac);

		try {
			vv.render(model, req, expectedResponse);
			if (mergeTemplateFailureException != null) {
				fail();
			}
		}
		catch (ServletException ex) {
			assertNotNull(mergeTemplateFailureException);
			assertEquals(ex.getRootCause(), mergeTemplateFailureException);
		}

		wmc.verify();
		reqControl.verify();
	}
	
	

	//	Damn thing is a class so we can't mock it
	private class TestVelocityEngine extends VelocityEngine {
		private Template t;
		private String expectedName;

		public TestVelocityEngine(String expectedName, Template t) {
			this.t = t;
			this.expectedName = expectedName;
		}

		public Template getTemplate(String arg0) throws ResourceNotFoundException, ParseErrorException, Exception {
			assertEquals(arg0, expectedName);
			return t;
		}
	}

}
