/*
 * Created on Sep 17, 2004
 */
package org.springframework.web.servlet.view.jasperreports;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;

/**
 * @author robh
 *
 */
public class JasperReportsCsvView extends AbstractWriterDrivenJasperReportsView {

	public JasperReportsCsvView() {
		setContentType("text/csv");
	}

	protected JRAbstractExporter getExporter() {
		return new JRCsvExporter();
	}

}
