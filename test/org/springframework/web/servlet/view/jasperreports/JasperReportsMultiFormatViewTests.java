
package org.springframework.web.servlet.view.jasperreports;

import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author robh
 */
public class JasperReportsMultiFormatViewTests extends AbstractJasperReportsTests{

	protected void extendModel(Map model) {
		model.put("format", "csv");
	}

	public void testSimpleHtmlRender() throws Exception{
    AbstractJasperReportsView view = getView(UNCOMPILED_REPORT);

		Map model = new HashMap();
		model.put("ReportTitle", "Foo");
		model.put("dataSource", getData());
		model.put("format", "html");

		view.render(model, request, response);

		assertEquals("Invalid content type", "text/html", response.getContentType());
	}

	protected AbstractJasperReportsView getViewImplementation() {
		JasperReportsMultiFormatView view = new JasperReportsMultiFormatView();
		return view;
	}

	protected String getDesiredContentType() {
		return "text/csv";
	}
}
