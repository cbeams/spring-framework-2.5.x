package org.springframework.web.servlet.view.jasperreports;

import net.sf.jasperreports.engine.export.JRHtmlExporter;

/**
 * @author robh
 */
public class ConfigurableJasperReportsViewWithStreamTests extends AbstractConfigurableJasperReportsViewTests {

	protected AbstractJasperReportsView getViewImplementation() {
		ConfigurableJasperReportsView view = new ConfigurableJasperReportsView();
		view.setExporterClass(JRHtmlExporter.class);
		view.setUseWriter(true);
		view.setContentType("application/pdf");
		return view;
	}

	protected String getDesiredContentType() {
		return "application/pdf";
	}
}
 