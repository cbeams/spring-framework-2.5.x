
package org.springframework.web.servlet.view.jasperreports;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.context.support.StaticApplicationContext;

/**
 * @author robh
 */
public class ExporterParameterTests extends AbstractJasperReportsTests {

	public void testParameterParsing() throws Exception{

		Map params = new HashMap();
		params.put("net.sf.jasperreports.engine.export.JRHtmlExporterParameter.IMAGES_URI", "/foo/bar");

		AbstractJasperReportsView view = new AbstractJasperReportsView() {
			protected void renderReport(JasperReport report, Map model, JRDataSource dataSource,
					HttpServletResponse response) throws Exception {
				assertEquals("Invalid number of exporter parameters", 1, getExporterParameters().size());

				JRExporterParameter key = JRHtmlExporterParameter.IMAGES_URI;
				Object value = getExporterParameters().get(key);

				assertNotNull("Value not mapped to correct key", value);
				assertEquals("Incorrect value for parameter", "/foo/bar", value);
			}
		};

		setViewProperties(view, params);
		view.initApplicationContext();
		view.render(getModel(), new MockHttpServletRequest(), new MockHttpServletResponse());

	}

	public void testInvalidClass() throws Exception{
		Map params = new HashMap();
		params.put("foo.net.sf.jasperreports.engine.export.JRHtmlExporterParameter.IMAGES_URI", "/foo");

		AbstractJasperReportsView view = new JasperReportsHtmlView();
		setViewProperties(view, params);

		try {
			view.initApplicationContext();
			fail();
		} catch(IllegalArgumentException ex) {
        // good
		}
	}

	public void testInvalidField() {
		Map params = new HashMap();
		params.put("net.sf.jasperreports.engine.export.JRHtmlExporterParameter.IMAGES_URI_FOO", "/foo");

		AbstractJasperReportsView view = new JasperReportsHtmlView();
		setViewProperties(view, params);

		try {
			view.initApplicationContext();
			fail();
		} catch(IllegalArgumentException ex) {
        // good
		}
	}

	public void testInvalidType() {
		Map params = new HashMap();
		params.put("java.lang.Boolean.TRUE", "/foo");

		AbstractJasperReportsView view = new JasperReportsHtmlView();
		setViewProperties(view, params);

		try {
			view.initApplicationContext();
			fail();
		} catch(IllegalArgumentException ex) {
        // good
		}
	}

	private void setViewProperties(AbstractJasperReportsView view, Map params) {
		view.setUrl("org/springframework/ui/jasperreports/DataSourceReport.jasper");
		view.setApplicationContext(new StaticApplicationContext());
		view.setExporterParameters(params);
	}
}
