
package org.springframework.web.servlet.view.jasperreports;

import java.util.Map;
import java.util.Properties;

/**
 * @author robh
 */
public class JasperReportsMultiFormatViewWithCustomMappingsTests extends JasperReportsMultiFormatViewTests {

	protected AbstractJasperReportsView getViewImplementation() {
		JasperReportsMultiFormatView view = new JasperReportsMultiFormatView();
		view.setDiscriminatorKey("fmt");

		Properties props = new Properties();
		props.setProperty("comma-separated", JasperReportsCsvView.class.getName());
    props.setProperty("html", JasperReportsHtmlView.class.getName());
		
		view.setFormatMappings(props);
		return view;
	}

	protected String getDiscriminatorKey() {
		return "fmt";
	}

	protected void extendModel(Map model) {
		model.put(getDiscriminatorKey(), "comma-separated");
	}

}
