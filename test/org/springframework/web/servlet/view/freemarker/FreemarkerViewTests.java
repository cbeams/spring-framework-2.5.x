package org.springframework.web.servlet.view.freemarker;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.mockobjects.servlet.MockHttpServletResponse;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.context.ApplicationContextException;

/**
 * @author Juergen Hoeller
 * @since 14.03.2004
 */
public class FreemarkerViewTests extends TestCase {

	public void testNoFreemarkerConfig() {
		FreemarkerView fv = new FreemarkerView();

		MockControl wmc = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wmc.getMock();
		wac.getBeansOfType(FreemarkerConfig.class, true, true);
		wmc.setReturnValue(new HashMap());
		wac.getParentBeanFactory();
		wmc.setReturnValue(null);
		wmc.replay();

		fv.setUrl("anythingButNull");
		try {
			fv.setApplicationContext(wac);
			fail();
		}
		catch (ApplicationContextException ex) {
			// Check there's a helpful error message
			assertTrue(ex.getMessage().indexOf("FreemarkerConfig") != -1);
		}

		wmc.verify();
	}

	public void testNoTemplateName() {
		FreemarkerView fv = new FreemarkerView();

		MockControl wmc = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wmc.getMock();
		// Expect no calls
		wmc.replay();

		try {
			fv.setApplicationContext(wac);
			fail();
		}
		catch (IllegalArgumentException ex) {
			// Check there's a helpful error message
			assertTrue(ex.getMessage().indexOf("url") != -1);
		}

		wmc.verify();
	}

	public void testValidTemplateName() throws Exception {
		FreemarkerView fv = new FreemarkerView();

		MockControl wmc = MockControl.createControl(WebApplicationContext.class);
		WebApplicationContext wac = (WebApplicationContext) wmc.getMock();
		wac.getBeansOfType(FreemarkerConfig.class, true, true);
		Map configs = new HashMap();
		FreemarkerConfigurer configurer = new FreemarkerConfigurer();
		configurer.setFreemarkerConfiguration(new TestConfiguration());
		configs.put("freemarkerConfig", configurer);
		wmc.setReturnValue(configs);
		wac.getParentBeanFactory();
		wmc.setReturnValue(null);
		wmc.replay();

		fv.setUrl("templateName");
		fv.setApplicationContext(wac);

		MockHttpServletRequest request = new MockHttpServletRequest(null, "GET", "/test");
		request.addPreferredLocale(Locale.US);
		request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, new AcceptHeaderLocaleResolver());
		HttpServletResponse response = new MockHttpServletResponse();

		Map model = new HashMap();
		model.put("myattr", "myvalue");
		fv.render(model, request, response);

		wmc.verify();
	}


	private class TestConfiguration extends Configuration {

		public Template getTemplate(String name, final Locale locale) throws IOException {
			assertEquals("templateName", name);
			return new Template(name, new StringReader("test")) {
				public void process(Object model, Writer writer) throws TemplateException, IOException {
					assertEquals(Locale.US, locale);
					assertTrue(model instanceof Map);
					Map modelMap = (Map) model;
					assertEquals("myvalue", modelMap.get("myattr"));
				}
			};
		}

	}

}
