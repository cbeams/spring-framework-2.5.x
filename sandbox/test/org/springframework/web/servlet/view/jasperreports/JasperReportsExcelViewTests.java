/*
 * Created on Sep 18, 2004
 */
package org.springframework.web.servlet.view.jasperreports;

/**
 * @author robh
 *
 */
public class JasperReportsExcelViewTests extends AbstractJasperReportsTest {


	protected AbstractJasperReportsView getViewImplementation() {
		return new JasperReportsExcelView();
	}


	protected String getDesiredContentType() {
		return "application/vnd.ms-excel";
	}

}
