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

package org.springframework.web.servlet.view.velocity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.tools.VelocityFormatter;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.easymock.MockControl;

import org.springframework.context.ApplicationContextException;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * @author Rod Johnson
 */
public class VelocityViewTests extends TestCase {

	public void testNoVelocityConfig() throws Exception {
		VelocityView vv = new VelocityView();
		MockControl wmc = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wmc.getMock();
		wac.getBeansOfType(VelocityConfig.class, true, true);
		wmc.setReturnValue(new HashMap());
		wac.getParentBeanFactory();
		wmc.setReturnValue(null);
		wmc.replay();

		vv.setUrl("anythingButNull");
		try {
			vv.setApplicationContext(wac);
			fail();
		}
		catch (ApplicationContextException ex) {
			// Check there's a helpful error message
			assertTrue(ex.getMessage().indexOf("VelocityConfig") != -1);
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
		catch (IllegalArgumentException ex) {
			// Check there's a helpful error message
			assertTrue(ex.getMessage().indexOf("url") != -1);
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
		VelocityConfig vc = new VelocityConfig() {
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
		wac.getBeansOfType(VelocityConfig.class, true, true);
		Map configurers = new HashMap();
		configurers.put("velocityConfigurer", vc);
		wmc.setReturnValue(configurers);
		wmc.replay();

		VelocityView vv = new VelocityView();
		//vv.setExposeDateFormatter(false);
		//vv.setExposeCurrencyFormatter(false);
		vv.setUrl(templateName);

		try {
			vv.setApplicationContext(wac);
			fail();
		}
		catch (ApplicationContextException ex) {
			assertEquals(ex.getCause(), templateLookupException);
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
	 * @param mergeTemplateFailureException may be null in which case mergeTemplate override will succeed.
	 * If it's non null it will be checked
	 */
	private void testValidTemplateName(final Exception mergeTemplateFailureException) throws Exception {
		Map model = new HashMap();
		model.put("foo", "bar");

		final String templateName = "test.vm";

		MockControl wmc = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wmc.getMock();
		wac.getParentBeanFactory();
		wmc.setReturnValue(null);
		final Template expectedTemplate = new Template();
		VelocityConfig vc = new VelocityConfig() {
			public VelocityEngine getVelocityEngine() {
				return new TestVelocityEngine(templateName, expectedTemplate);
			}
		};
		wac.getBeansOfType(VelocityConfig.class, true, true);
		Map configurers = new HashMap();
		configurers.put("velocityConfigurer", vc);
		wmc.setReturnValue(configurers);
		wmc.replay();

		MockControl reqControl = MockControl.createControl(HttpServletRequest.class);
		HttpServletRequest req = (HttpServletRequest) reqControl.getMock();
		reqControl.replay();

		final HttpServletResponse expectedResponse = new MockHttpServletResponse();

		VelocityView vv = new VelocityView() {
			protected void mergeTemplate(Template template, Context context, HttpServletResponse response) throws Exception {
				assertTrue(template == expectedTemplate);
				assertTrue(context.getKeys().length >= 1);
				assertTrue(context.get("foo").equals("bar"));
				assertTrue(response == expectedResponse);
				if (mergeTemplateFailureException != null) {
					throw mergeTemplateFailureException;
				}
			}
		};
		vv.setUrl(templateName);
		vv.setApplicationContext(wac);

		try {
			vv.render(model, req, expectedResponse);
			if (mergeTemplateFailureException != null) {
				fail();
			}
		}
		catch (Exception ex) {
			assertNotNull(mergeTemplateFailureException);
			assertEquals(ex, mergeTemplateFailureException);
		}

		wmc.verify();
		reqControl.verify();
	}

	public void testExposeHelpers() throws Exception {
		final String templateName = "test.vm";

		MockControl wmc = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wmc.getMock();
		wac.getParentBeanFactory();
		wmc.setReturnValue(null);
		final Template expectedTemplate = new Template();
		VelocityConfig vc = new VelocityConfig() {
			public VelocityEngine getVelocityEngine() {
				return new TestVelocityEngine(templateName, expectedTemplate);
			}
		};
		wac.getBeansOfType(VelocityConfig.class, true, true);
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
				assertTrue(response == expectedResponse);
				assertEquals("myValue", context.get("myHelper"));
				assertTrue(context.get("velocityFormatter") instanceof VelocityFormatter);

				assertTrue(context.get("dateTool") instanceof DateTool);
				DateTool dateTool = (DateTool) context.get("dateTool");
				assertTrue(dateTool.getLocale().equals(Locale.CANADA));

				assertTrue(context.get("numberTool") instanceof NumberTool);
				NumberTool numberTool = (NumberTool) context.get("numberTool");
				assertTrue(numberTool.getLocale().equals(Locale.CANADA));
			}

			protected void exposeHelpers(Map model, HttpServletRequest request) throws Exception {
				model.put("myHelper", "myValue");
			}
		};
		
		vv.setUrl(templateName);
		vv.setApplicationContext(wac);
		vv.setVelocityFormatterAttribute("velocityFormatter");
		vv.setDateToolAttribute("dateTool");
		vv.setNumberToolAttribute("numberTool");
		vv.render(new HashMap(), req, expectedResponse);

		wmc.verify();
		reqControl.verify();
	}

	public void testVelocityViewResolver() {
		VelocityViewResolver resolver = new VelocityViewResolver();
		resolver.setPrefix("prefix_");
		resolver.setSuffix("_suffix");
		resolver.setApplicationContext(new StaticWebApplicationContext());
		VelocityView view = (VelocityView) resolver.loadView("test", Locale.CANADA);
		assertEquals("test", view.getBeanName());
		assertEquals("prefix_test_suffix", view.getUrl());
	}


	//	Damn thing is a class so we can't mock it
	static class TestVelocityEngine extends VelocityEngine {
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
