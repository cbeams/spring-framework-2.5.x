/*
 * Created on Sep 18, 2004
 */
package org.springframework.web.servlet.view.jasperreports;

/**
 * @author robh
 *
 */
public class JasperReportsHtmlViewTests extends AbstractJasperReportsTest {

	protected AbstractJasperReportsView getViewImplementation() {
		return new JasperReportsHtmlView();
	}


	protected String getDesiredContentType() {
		return "text/html";
	}

}
