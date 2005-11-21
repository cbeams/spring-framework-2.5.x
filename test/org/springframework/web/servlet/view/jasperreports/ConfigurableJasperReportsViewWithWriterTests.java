package org.springframework.web.servlet.view.jasperreports;

import net.sf.jasperreports.engine.export.JRPdfExporter;

/**
 * @author robh
 */
public class ConfigurableJasperReportsViewWithWriterTests extends AbstractConfigurableJasperReportsViewTests {

	protected AbstractJasperReportsView getViewImplementation() {
		ConfigurableJasperReportsView view = new ConfigurableJasperReportsView();
		view.setExporterClass(JRPdfExporter.class);
		view.setUseWriter(false);
		view.setContentType("text/html");
		return view;       
	}

	protected String getDesiredContentType() {
		return "text/html";
	}
}
