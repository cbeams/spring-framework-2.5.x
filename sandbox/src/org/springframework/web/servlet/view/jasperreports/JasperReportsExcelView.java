/*
 * Created on Sep 17, 2004
 */
package org.springframework.web.servlet.view.jasperreports;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;

/**
 * @author robh
 *
 */
public class JasperReportsExcelView extends
		AbstractOutputStreamDrivenJasperReportsView {

	
	public JasperReportsExcelView() {
		setContentType("application/vnd.ms-excel");
	}
	
	protected JRAbstractExporter getExporter() {
		return new JRXlsExporter();
	}

}
