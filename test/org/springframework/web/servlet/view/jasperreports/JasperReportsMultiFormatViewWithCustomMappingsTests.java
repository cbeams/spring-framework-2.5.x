
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

		view.setFormatMappings(props);
		return view;
	}

	protected void extendModel(Map model) {
		model.put("fmt", "comma-separated");
	}

}
