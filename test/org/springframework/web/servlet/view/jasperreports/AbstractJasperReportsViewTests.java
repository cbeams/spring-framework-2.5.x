
package org.springframework.web.servlet.view.jasperreports;

import java.util.Map;
import java.util.LinkedList;
import java.util.Properties;

import org.springframework.context.ApplicationContextException;
import org.springframework.context.support.StaticApplicationContext;

/**
 * @author robh
 */
public abstract class AbstractJasperReportsViewTests extends AbstractJasperReportsTests{

	protected abstract AbstractJasperReportsView getViewImplementation();

	protected abstract String getDesiredContentType();
	
	protected AbstractJasperReportsView getView(String url) throws Exception {
		AbstractJasperReportsView view = getViewImplementation();
		view.setUrl(url);
		view.setApplicationContext(new StaticApplicationContext());
		return view;
	}

	/**
	 * Simple test to see if compiled report succeeds.
	 */
	public void testCompiledReport() throws Exception {
		AbstractJasperReportsView view = getView(COMPILED_REPORT);
		view.render(getModel(), request, response);
		assertTrue(response.getContentAsByteArray().length > 0);
	}

	public void testUncompiledReport() throws Exception {
		AbstractJasperReportsView view = getView(UNCOMPILED_REPORT);
		view.render(getModel(), request, response);
		assertTrue(response.getContentAsByteArray().length > 0);
	}

	public void testWithInvalidPath() throws Exception {
		try {
			getView("foo.jasper");
			fail("Invalid path should throw ApplicationContextException");
		}
		catch (ApplicationContextException ex) {
			// good!
		}
	}

	public void testInvalidExtension() throws Exception {
		try {
			getView("foo.bar");
			fail("Invalid extension should throw IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testContentType() throws Exception {
		AbstractJasperReportsView view = getView(COMPILED_REPORT);

		// removed assert because not all views no in advance what the content type will be,
		// plus the important test is the finished response.
		//assertEquals("View content type is incorrect", getDesiredContentType(), view.getContentType());
		view.render(getModel(), request, response);
		assertEquals("Response content type is incorrect", getDesiredContentType(), response.getContentType());
	}

	public void testWithoutDatasource() throws Exception {
		Map model = getModel();
		model.remove("dataSource");
		try {
			AbstractJasperReportsView view = getView(COMPILED_REPORT);
			view.render(model, request, response);
			fail("No data source should result in NoDataSourceException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testWithCollection() throws Exception {
		Map model = getModel();
		model.remove("dataSource");
		model.put("reportData", getData());
		AbstractJasperReportsView view = getView(COMPILED_REPORT);
		view.render(model, request, response);
		assertTrue(response.getContentAsByteArray().length > 0);
	}

	public void testWithMultipleCollections() throws Exception {
		Map model = getModel();
		model.remove("dataSource");
		model.put("reportData", getData());
		model.put("otherData", new LinkedList());
		try {
			AbstractJasperReportsView view = getView(COMPILED_REPORT);
			view.render(model, request, response);
			fail("No data source should result in NoDataSourceException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testWithSpecificCollection() throws Exception {
		Map model = getModel();
		model.remove("dataSource");
		model.put("reportData", getData());
		model.put("otherData", new LinkedList());
		AbstractJasperReportsView view = getView(COMPILED_REPORT);
		view.setReportDataKey("reportData");
		view.render(model, request, response);
		assertTrue(response.getContentAsByteArray().length > 0);
	}

	public void testWithArray() throws Exception {
		Map model = getModel();
		model.remove("dataSource");
		model.put("reportData", getData().toArray());
		AbstractJasperReportsView view = getView(COMPILED_REPORT);
		view.render(model, request, response);
		assertTrue(response.getContentAsByteArray().length > 0);
	}

	public void testWithMultipleArrays() throws Exception {
		Map model = getModel();
		model.remove("dataSource");
		model.put("reportData", getData().toArray());
		model.put("otherData", new String[0]);
		try {
			AbstractJasperReportsView view = getView(COMPILED_REPORT);
			view.render(model, request, response);
			fail("No data source should result in NoDataSourceException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testWithSpecificArray() throws Exception {
		Map model = getModel();
		model.remove("dataSource");
		model.put("reportData", getData().toArray());
		model.put("otherData", new String[0]);
		AbstractJasperReportsView view = getView(COMPILED_REPORT);
		view.setReportDataKey("reportData");
		view.render(model, request, response);
		assertTrue(response.getContentAsByteArray().length > 0);
	}

	public void testWithSubReport() throws Exception {
		Map model = getModel();
		model.put("SubReportData", getProductData());

		Properties subReports = new Properties();
		subReports.put("ProductsSubReport", "org/springframework/ui/jasperreports/subReportChild.jrxml");

		AbstractJasperReportsView view = getView(SUB_REPORT_PARENT);
		view.setReportDataKey("dataSource");
		view.setSubReportUrls(subReports);
		view.setSubReportDataKeys(new String[] {"SubReportData"});
		view.initApplicationContext();
		view.render(model, request, response);

		assertTrue(response.getContentAsByteArray().length > 0);
	}

	public void testWithNonExistentSubReport() throws Exception {
		Map model = getModel();
		model.put("SubReportData", getProductData());

		Properties subReports = new Properties();
		subReports.put("ProductsSubReport", "org/springframework/ui/jasperreports/subReportChildFalse.jrxml");

		AbstractJasperReportsView view = getView(SUB_REPORT_PARENT);
		view.setReportDataKey("dataSource");
		view.setSubReportUrls(subReports);
		view.setSubReportDataKeys(new String[] {"SubReportData"});

		try {
			view.initApplicationContext();
			fail("Invalid report URL should throw ApplicationContext Exception");
		}
		catch (ApplicationContextException ex) {
			// success
		}
	}

	public void testSubReportWithUnspecifiedParentDataSource() throws Exception {
		Map model = getModel();
		model.put("SubReportData", getProductData());

		Properties subReports = new Properties();
		subReports.put("ProductsSubReport", "org/springframework/ui/jasperreports/subReportChildFalse.jrxml");

		AbstractJasperReportsView view = getView(SUB_REPORT_PARENT);
		view.setSubReportUrls(subReports);
		view.setSubReportDataKeys(new String[] {"SubReportData"});

		try {
			view.initApplicationContext();
			fail("Unspecified reportDataKey should throw exception when subReportDataSources is specified");
		}
		catch (ApplicationContextException ex) {
			// success
		}
	}

	public void testContentDisposition() throws Exception {
		AbstractJasperReportsView view = getView(COMPILED_REPORT);
		view.render(getModel(), request, response);
		assertEquals("Invalid content type", "inline", response.getHeader("Content-Disposition"));

	}

	public void testOverrideContentDisposition() throws Exception {
		Properties headers = new Properties();
		String cd = "attachment";
		headers.setProperty("Content-Disposition", cd);

		AbstractJasperReportsView view = getView(COMPILED_REPORT);
		view.setHeaders(headers);
		view.render(getModel(), request, response);
		assertEquals("Invalid content type", cd, response.getHeader("Content-Disposition"));
	}

	public void testSetCustomHeaders() throws Exception {
		Properties headers = new Properties();

		String key = "foo";
		String value = "bar";

		headers.setProperty(key, value);

		AbstractJasperReportsView view = getView(COMPILED_REPORT);
		view.setHeaders(headers);
		view.render(getModel(), request, response);

		assertNotNull("Header not present", response.getHeader(key));
		assertEquals("Invalid header value", value, response.getHeader(key));

	}
}
