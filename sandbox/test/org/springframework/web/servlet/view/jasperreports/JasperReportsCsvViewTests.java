/*
 * Created on Sep 18, 2004
 */
package org.springframework.web.servlet.view.jasperreports;

/**
 * @author robh
 *
 */
public class JasperReportsCsvViewTests extends AbstractJasperReportsTest {

	protected AbstractJasperReportsView getViewImplementation() {
		return new JasperReportsCsvView();
	}

	protected String getDesiredContentType() {
		return "text/csv";
	}

}
