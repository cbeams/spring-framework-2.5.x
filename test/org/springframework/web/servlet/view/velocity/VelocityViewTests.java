package org.springframework.web.servlet.view.velocity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
 */
public class VelocityViewTests extends TestCase {

	/**
	 * Constructor for VelocityViewTests.
	 * @param arg0
	 */
	public VelocityViewTests(String arg0) {
		super(arg0);
	}

	public void testNoVelocityConfiguration() throws Exception {
		VelocityView vv = new VelocityView();
		MockControl wmc = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wmc.getMock();
		wac.getBeanDefinitionNames(VelocityConfiguration.class);
		wmc.setReturnValue(new String[0]);
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

	public void testValidTemplateName() throws Exception {
		Map model = new HashMap();
		model.put("foo", "bar");

		final String templateName = "test.vm";

		MockControl wmc = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wmc.getMock();
		wac.getBeanDefinitionNames(VelocityConfiguration.class);
		String configurerName = "velocityConfigurer";
		wmc.setReturnValue(new String[] { configurerName });
		wac.getParentBeanFactory();
		wmc.setReturnValue(null);
		wac.getBean(configurerName);
		final Template expectedTemplate = new Template();
		wmc.setReturnValue(new VelocityConfiguration() {
			public VelocityEngine getVelocityEngine() {
				return new TestVelocityEngine(templateName, expectedTemplate);
			}
		});
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
			protected void mergeTemplate(Template template, Context context, HttpServletResponse response) {
				assertTrue(template == expectedTemplate);
				assertTrue(context.getKeys().length >= 1);
				assertTrue(context.get("foo").equals("bar"));
				assertTrue(response == expectedResponse);
			}

		};
		//vv.setExposeDateFormatter(false);
		//vv.setExposeCurrencyFormatter(false);
		vv.setTemplateName(templateName);
		vv.setApplicationContext(wac);

		vv.render(model, req, expectedResponse);

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
