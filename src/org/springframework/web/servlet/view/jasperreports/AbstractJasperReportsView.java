/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet.view.jasperreports;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRBshCompiler;
import net.sf.jasperreports.engine.design.JRCompiler;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.springframework.context.ApplicationContextException;
import org.springframework.core.io.Resource;
import org.springframework.ui.jasperreports.JasperReportsUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

/**
 * Base class for all JasperReports views. Applies on-the-fly compilation
 * of report designs as required and coordinates the rendering process.
 *
 * <p>This class is responsible for getting report data from the model that has
 * been provided to the view. The default implementation checks for a model object
 * under the specified "reportDataKey" first, then falls back to looking for a
 * value of type <code>JRDataSource</code>, <code>java.util.Collection</code>,
 * object array (in that order).
 *
 * <p>Subclasses need to implement two template methods: <code>createExporter</code>
 * to create a JasperReports exporter for a specific output format, and
 * <code>useWriter</code> to determine whether to write text or binary content.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.1.3
 * @see #getReportData
 * @see #createExporter
 * @see #useWriter
 */
public abstract class AbstractJasperReportsView extends AbstractUrlBasedView {

    /**
     * A <code>String</code> key used to lookup the <code>JRDataSource</code> in the model.
     */
	private String reportDataKey;

	/**
	 * A <code>Resource</code> instance that is the source of the <code>JasperReport</code>.
	 */
	private Resource reportResource;

	/**
	 * The <code>JasperReport</code> that is used to render the view.
	 */
	private JasperReport report;


	/**
	 * Set the name of the model attribute that represents the report data.
	 * If not specified, the model map will be searched for a matching value type.
	 * <p>A <code>JRDataSource</code> will be taken as-is. For other types, conversion
	 * will apply: By default, a <code>java.util.Collection</code> will be converted
	 * to <code>JRBeanCollectionDataSource</code>, and an object array to
	 * <code>JRBeanArrayDataSource</code>.
	 * <p><b>Note:</b> If you pass in a Collection or object array in the model map
	 * for use as plain report parameter, rather than as report data to extract fields
	 * from, you need to specify the key for the actual report data to use, to avoid
	 * mis-detection of report data by type.
	 * @see #convertReportData
	 * @see net.sf.jasperreports.engine.JRDataSource
	 * @see net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
	 * @see net.sf.jasperreports.engine.data.JRBeanArrayDataSource
	 */
	public void setReportDataKey(String reportDataKey) {
		this.reportDataKey = reportDataKey;
	}

	/**
	 * Checks to see that a valid report file URL is supplied in the
	 * configuration. Compiles the report file is necessary.
	 */
	protected void initApplicationContext() throws ApplicationContextException {
		super.initApplicationContext();

		this.reportResource = getApplicationContext().getResource(getUrl());
		try {
			if (this.reportResource.getFilename().endsWith(".jasper")) {
				// load pre-compiled report
				if (logger.isInfoEnabled()) {
					logger.info("Loading pre-compiled Jasper Report from " + this.reportResource);
				}
				this.report = (JasperReport) JRLoader.loadObject(this.reportResource.getInputStream());
			}
			else if (this.reportResource.getFilename().endsWith(".jrxml")) {
				// compile report on-the-fly
				if (logger.isInfoEnabled()) {
					logger.info("Compiling Jasper Report loaded from " + this.reportResource);
				}
				JasperDesign design = JRXmlLoader.load(this.reportResource.getInputStream());
				this.report = getReportCompiler().compileReport(design);
			}
			else {
				throw new IllegalArgumentException("Report URL must end in either .jasper or .jrxml");
			}
		}
		catch (IOException ex) {
			throw new ApplicationContextException(
					"Could not load JasperReports report for URL [" + getUrl() + "]", ex);
		}
		catch (JRException ex) {
			throw new ApplicationContextException(
					"Could not parse JasperReports report for URL [" + getUrl() + "]", ex);
		}
	}

	/**
	 * Return the JasperReports compiler to use for compiling a ".jrxml"
	 * file into a a report class. Default is <code>JRBshCompiler</code>,
	 * which requires BeanShell on the class path.
	 * @see net.sf.jasperreports.engine.design.JRCompiler
	 * @see net.sf.jasperreports.engine.design.JRBshCompiler
	 */
	protected JRCompiler getReportCompiler() {
		return new JRBshCompiler();
	}


	/**
	 * Finds the report data to use for rendering the report and then invokes the
	 * <code>renderReport</code> method that should be implemented by the subclass.
	 * @param model the model map, as passed in for view renderin.
	 * Must contain a report data value that can be converted to a <code>JRDataSource</code>,
	 * acccording to the <code>getReportData</code> method.
	 * @see #getReportData
	 */
	protected void renderMergedOutputModel(
			Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JRDataSource dataSource = getReportData(model);
		response.setContentType(getContentType());
		renderReport(this.report, model, dataSource, response);
	}

	/**
	 * Find an instance of JRDataSource in the given model map or create an
	 * appropriate JRDataSource for passed-in report data.
	 * <p>The default implementation checks for a model object under the
	 * specified "reportDataKey" first, then falls back to looking for a value
	 * of type <code>JRDataSource</code>, <code>java.util.Collection</code>,
	 * object array (in that order).
	 * @param model the model map, as passed in for view rendering
	 * @return the JRDataSource
	 * @throws IllegalArgumentException if no JRDataSource found
	 * @see #setReportDataKey
	 * @see #convertReportData
	 * @see #getReportDataTypes
	 */
	protected JRDataSource getReportData(Map model) throws IllegalArgumentException {
		// Try model attribute with specified name.
		if (this.reportDataKey != null) {
			Object value = model.get(this.reportDataKey);
			return convertReportData(value);
		}

		// Try to find matching attribute, of given prioritized types.
		Object value = CollectionUtils.findValueOfType(model.values(), getReportDataTypes());
		if (value != null) {
			return convertReportData(value);
		}

		throw new IllegalArgumentException("No report data supplied in model " + model);
	}

	/**
	 * Convert the given report data value to a <code>JRDataSource</code>.
	 * <p>The default implementation delegates to <code>JasperReportUtils</code>.
	 * A <code>JRDataSource</code>, <code>java.util.Collection</code> or object array
	 * is detected. The latter are converted to <code>JRBeanCollectionDataSource</code>
	 * or <code>JRBeanArrayDataSource</code>, respectively.
	 * @param value the report data value to convert
	 * @return the JRDataSource
	 * @throws IllegalArgumentException if the value could not be converted
	 * @see org.springframework.ui.jasperreports.JasperReportsUtils#convertReportData
	 * @see net.sf.jasperreports.engine.JRDataSource
	 * @see net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
	 * @see net.sf.jasperreports.engine.data.JRBeanArrayDataSource
	 */
	protected JRDataSource convertReportData(Object value) throws IllegalArgumentException {
		return JasperReportsUtils.convertReportData(value);
	}

	/**
	 * Return the value types that can be converted to a JRDataSource,
	 * in prioritized order. Should only return types that the
	 * <code>convertReportData</code> method is actually able to convert.
	 * <p>Default value types are: <code>JRDataSource</code>,
	 * <code>java.util.Collection</code>, object array.
	 * @return the value types in prioritized order
	 * @see #convertReportData
	 */
	protected Class[] getReportDataTypes() {
		return new Class[] {JRDataSource.class, Collection.class, Object[].class};
	}

	/**
	 * Subclasses should implement this method to perform the actual rendering process.
	 * @param report the <code>JasperReport</code> to render
	 * @param parameters the map containing report parameters
	 * @param dataSource the <code>JRDataSource</code> containing the report data
	 * @param response the HTTP response the report should be rendered to
	 * @throws Exception if rendering failed
	 */
	protected void renderReport(
			JasperReport report, Map parameters, JRDataSource dataSource, HttpServletResponse response)
			throws Exception {

		// Prepare report for rendering.
		JRAbstractExporter exporter = createExporter();
		JasperPrint print = JasperFillManager.fillReport(report, parameters, dataSource);

		if (useWriter()) {
			// Render report into HttpServletResponse's Writer.
			JasperReportsUtils.render(exporter, print, response.getWriter());
		}

		else {
			// Render report into local OutputStream.
			// IE workaround: write into byte array first.
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			JasperReportsUtils.render(exporter, print, baos);

			// Write content length (determined via byte array).
			response.setContentLength(baos.size());

			// Flush byte array to servlet output stream.
			ServletOutputStream out = response.getOutputStream();
			baos.writeTo(out);
			out.flush();
		}
	}


	/**
	 * Create a JasperReports exporter for a specific output format,
	 * which will be used to render the report to the HTTP response.
	 * <p>The <code>useWriter</code> method determines whether the
	 * output will be written as text or as binary content.
	 * @see #useWriter
	 */
	protected abstract JRAbstractExporter createExporter();

	/**
	 * Return whether to use a <code>java.io.Writer</code> to write text content
	 * to the HTTP response. Else, a <code>java.io.OutputStream</code> will be used,
	 * to write binary content to the response.
	 * @see javax.servlet.ServletResponse#getWriter
	 * @see javax.servlet.ServletResponse#getOutputStream
	 */
	protected abstract boolean useWriter();

}
