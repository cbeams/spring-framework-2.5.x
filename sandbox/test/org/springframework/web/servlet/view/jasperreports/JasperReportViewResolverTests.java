package org.springframework.web.servlet.view.jasperreports;

import junit.framework.TestCase;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.web.servlet.view.velocity.VelocityView;

import java.util.Locale;

/**
 * @author Rob Harrop
 */
public class JasperReportViewResolverTests extends TestCase {

	public void testResolveView() throws Exception {
		StaticApplicationContext ctx = new StaticApplicationContext();

		String prefix = "org/springframework/ui/jasperreports/";
		String suffix = ".jrxml";
		String viewName = "DataSourceReport";

		JasperReportsViewResolver viewResolver = new JasperReportsViewResolver();
		viewResolver.setViewClass(JasperReportsHtmlView.class);
		viewResolver.setPrefix(prefix);
		viewResolver.setSuffix(suffix);
		viewResolver.setApplicationContext(ctx);

		AbstractJasperReportsView view = (AbstractJasperReportsView) viewResolver.resolveViewName(viewName, Locale.ENGLISH);
		assertNotNull("View should not be null", view);
		assertEquals("Incorrect URL", prefix + viewName + suffix, view.getUrl());
	}

	public void testSetIncorrectViewClass() {
		try {
			new JasperReportsViewResolver().setViewClass(VelocityView.class);
			fail("Should not be able to set view class to a class that does not extend AbstractJasperReportsView");

		}
		catch (IllegalArgumentException ex) {
			// success
		}
	}
}
