package org.springframework.web.servlet.view.jasperreports;

import org.springframework.context.support.StaticApplicationContext;

/**
 * @author robh
 */
public abstract class AbstractConfigurableJasperReportsViewTests extends AbstractJasperReportsViewTests {

	public void testSetInvalidExporterClass() throws Exception {
		try {
			new ConfigurableJasperReportsView().setExporterClass(String.class);
			fail("Should not be able to set invalid view class.");
		}
		catch (IllegalArgumentException ex) {
			// success
		}
	}

	public void testNoConfiguredExporter() throws Exception {
		ConfigurableJasperReportsView view = new ConfigurableJasperReportsView();
		view.setUrl(COMPILED_REPORT);
		try {
			view.setApplicationContext(new StaticApplicationContext());
			fail("Should not be able to setup view class without an exporter class.");
		}
		catch (IllegalArgumentException e) {
			// success
		}
	}
}
