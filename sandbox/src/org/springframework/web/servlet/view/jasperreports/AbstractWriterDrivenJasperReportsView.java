/*
 * Created on Sep 17, 2004
 */
package org.springframework.web.servlet.view.jasperreports;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import org.springframework.ui.jasperreports.JasperReportsUtils;

/**
 * @author robh
 *
 */
public abstract class AbstractWriterDrivenJasperReportsView extends
		AbstractJasperReportsView {

	protected abstract JRAbstractExporter getExporter();
	
	protected void renderView(JasperReport report, Map model,
			JRDataSource dataSource, HttpServletResponse response)
			throws Exception {
		
		JasperPrint print = JasperReportsUtils.fillReport(report, model, dataSource);
		
		JasperReportsUtils.render(print, response.getWriter(), getExporter());

	}

}
