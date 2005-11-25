package org.springframework.web.servlet.view.jasperreports;

import net.sf.jasperreports.engine.JRExporter;

import org.springframework.beans.BeanUtils;

/**
 * Configurable JasperReports View, allowing to specify the JasperReports exporter
 * to be specified through bean properties rather than through the view class name.
 *
 * @author Rob Harrop
 * @since 1.3
 * @see JasperReportsCsvView
 * @see JasperReportsHtmlView
 * @see JasperReportsPdfView
 * @see JasperReportsXlsView
 */
public class ConfigurableJasperReportsView extends AbstractJasperReportsSingleFormatView {

	private Class exporterClass;

	private boolean useWriter = true;


	/**
	 * Set the {@link JRExporter} implementation <code>Class</code> to use. Throws
	 * {@link IllegalArgumentException} if the <code>Class</code> doesn't implement
	 * {@link JRExporter}. Required setting, as it does not have a default.
	 */
	public void setExporterClass(Class exporterClass) {
		if (!(JRExporter.class.isAssignableFrom(exporterClass))) {
			throw new IllegalArgumentException(
					"Exporter class [" + exporterClass.getName() + "] does not implement JRExporter");
		}
		this.exporterClass = exporterClass;
	}

	/**
	 * Specifies whether or not the {@link JRExporter} writes to the {@link java.io.PrintWriter}
	 * of the associated with the request (<code>true</code>) or whether it writes directly to the
	 * {@link java.io.InputStream} of the request (<code>false</code>). Default is <code>true</code>.
	 */
	public void setUseWriter(boolean useWriter) {
		this.useWriter = useWriter;
	}

	/**
	 * Checks that the {@link #setExporterClass(Class) exporterClass} property is specified.
	 */
	protected void onInit() {
		if (this.exporterClass == null) {
			throw new IllegalArgumentException("exporterClass is required");
		}
	}


	/**
	 * Returns a new instance of the specified {@link JRExporter} class.
	 * @see #setExporterClass(Class)
	 * @see BeanUtils#instantiateClass(Class)
	 */
	protected JRExporter createExporter() {
		return (JRExporter) BeanUtils.instantiateClass(this.exporterClass);
	}

	/**
	 * Indicates how the {@link JRExporter} should render its data.
	 * @see #setUseWriter(boolean)
	 */
	protected boolean useWriter() {
		return this.useWriter;
	}

}
