/*
 * Created on Sep 17, 2004
 */
package org.springframework.web.servlet.view.jasperreports;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;

/**
 * @author robh
 *
 */
public class JasperReportsPdfView extends AbstractOutputStreamDrivenJasperReportsView {

	public JasperReportsPdfView() {
		setContentType("application/pdf");
	}
		
	protected JRAbstractExporter getExporter() {
		return new JRPdfExporter();
	}

}
