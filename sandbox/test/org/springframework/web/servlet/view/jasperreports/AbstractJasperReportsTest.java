/*
 * Created on Sep 18, 2004
 */
package org.springframework.web.servlet.view.jasperreports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author robh
 *  
 */
public abstract class AbstractJasperReportsTest extends TestCase {

	private static final String COMPILED_REPORT = "./sandbox/test/org/springframework/web/servlet/view/jasperreports/DataSourceReport.jasper";

	private static final String UNCOMPILED_REPORT = "./sandbox/test/org/springframework/web/servlet/view/jasperreports/DataSourceReport.jrxml";

	private MockHttpServletRequest request;

	private MockHttpServletResponse response;

	protected abstract AbstractJasperReportsView getViewImplementation();
	
	protected abstract String getDesiredContentType();

	public void setUp() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	/**
	 * Simple test to see if compiled report
	 * succeeds.
	 */
	public void testCompiledReport() throws Exception{
		AbstractJasperReportsView view = getView(COMPILED_REPORT);
		view.render(getModel(), request, response);
	}
	
	public void testUncompiledReport() throws Exception {
		AbstractJasperReportsView view = getView(UNCOMPILED_REPORT);
		view.render(getModel(), request, response);
	}
	
	public void testWithInvalidPath() throws Exception {
		try {
			getView("foo.jasper");
			fail("Invalid path should throw ReportFileNotFoundException");
		} catch(ReportFileNotFoundException ex) {
			// good!
		}
	}
	
	public void testInvalidExtension() {
		try {
			getView("foo.bar");
			fail("Invalid extension should throw UnrecognizedReportExtensionException");
		} catch(UnrecognizedReportExtensionException ex) {
			// good
		}
	}
	
	public void testContentType() throws Exception {
		AbstractJasperReportsView view = getView(COMPILED_REPORT);
		
		assertEquals("View content type is incorrect", getDesiredContentType(), view.getContentType());
		
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
		} catch(NoDataSourceException ex) {
			// good!
		}
	}
	
	private AbstractJasperReportsView getView(String url) {
		AbstractJasperReportsView view = getViewImplementation();
		view.setUrl(url);
		view.setApplicationContext(new MockApplicationContext());
		view.initApplicationContext();
		return view;
	}
	
	private Map getModel() {
		Map model = new HashMap();
		model.put("ReportTitle", "Dear Lord!");
		model.put("dataSource", new JRBeanCollectionDataSource(getData()));

		return model;
	}

	private List getData() {
		List list = new ArrayList();

		for (int x = 0; x < 10; x++) {
			MyBean bean = new MyBean();
			bean.setId(x);
			bean.setName("Rob Harrop");
			bean.setStreet("foo");

			list.add(bean);
		}

		return list;
	}
}