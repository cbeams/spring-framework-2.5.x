/*
 * Created on Sep 17, 2004
 */
package org.springframework.web.servlet.view.jasperreports;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

/**
 * Abstract base class for Jasper Reports view that require
 * an OutputStream to write to.
 * @author robh
 */
public abstract class AbstractOutputStreamDrivenJasperReportsView extends
		AbstractJasperReportsView {

	protected abstract JRAbstractExporter getExporter();
	
	protected void renderView(JasperReport report, Map model,
			JRDataSource dataSource, HttpServletResponse response)
			throws Exception {
		
		JasperPrint print = JasperFillManager.fillReport(report, model, dataSource);
		
		JRAbstractExporter exporter = getExporter();
		
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.getOutputStream());
		
		exporter.exportReport();
	}

}
