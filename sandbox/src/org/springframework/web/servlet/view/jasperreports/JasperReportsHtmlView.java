/*
 * Created on Sep 17, 2004
 */
package org.springframework.web.servlet.view.jasperreports;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;

/**
 * @author robh
 *
 */
public class JasperReportsHtmlView extends AbstractWriterDrivenJasperReportsView {

	public JasperReportsHtmlView() {
		setContentType("text/html");
	}
	
	protected JRAbstractExporter getExporter() {
		return new JRHtmlExporter();
	}
}
