package org.springframework.web.servlet.view.jasperreports;

import net.sf.jasperreports.engine.JRExporter;
import org.springframework.beans.BeanUtils;

/**
 * @author Rob Harrop
 */
public class ConfigurableJasperReportsView extends AbstractJasperReportsSingleFormatView {

	/**
	 * The {@link JRExporter} implementation <code>Class</code> to use.
	 */
	private Class exporterClass;

	/**
	 * Indicates where the {@link JRExporter} should write to.
	 * @see #setUseWriter(boolean)
	 */
	private boolean useWriter = true;

	/**
	 * Checks that the {@link #setExporterClass(Class) exporterClass} property is specified.
	 */
	protected void onInit() {
		if(this.exporterClass == null) {
			throw new IllegalArgumentException("Property [exporterClass] is required.");
		}
	}

	/**
	 * Sets the {@link JRExporter} implementation <code>Class</code> to use. Throws
	 * {@link IllegalArgumentException} if the <code>Class</code> doesn't implement
	 * {@link JRExporter}. Required.
	 */
	public void setExporterClass(Class exporterClass) {
		if(!(JRExporter.class.isAssignableFrom(exporterClass))) {
			throw new IllegalArgumentException("Exporter class [" + exporterClass.getName() + "] does not implement JRExporter.");
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
	 * Indicates how the {@link JRExporter} should render its data.
	 * @see #setUseWriter(boolean)
	 */
	protected boolean useWriter() {
		return this.useWriter;
	}

	/**
	 * Returns a new instance of the specified {@link JRExporter} class.
	 * @see #setExporterClass(Class)
	 * @see BeanUtils#instantiateClass(Class)
	 */
	protected JRExporter createExporter() {
		return (JRExporter) BeanUtils.instantiateClass(this.exporterClass);
	}

}
