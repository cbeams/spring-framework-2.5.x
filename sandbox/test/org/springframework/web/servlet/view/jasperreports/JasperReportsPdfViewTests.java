/*
 * Created on Sep 18, 2004
 */
package org.springframework.web.servlet.view.jasperreports;

/**
 * @author robh
 *
 */
public class JasperReportsPdfViewTests extends AbstractJasperReportsTest {

	protected AbstractJasperReportsView getViewImplementation() {
		return new JasperReportsPdfView();
	}

	protected String getDesiredContentType() {
		return "application/pdf";
	}
}
