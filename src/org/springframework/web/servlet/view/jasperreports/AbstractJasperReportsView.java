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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
 * The resource path of the main report needs to be specified as <code>url</code>.
 *
 * <p>This class is responsible for getting report data from the model that has
 * been provided to the view. The default implementation checks for a model object
 * under the specified <code>reportDataKey</code> first, then falls back to looking
 * for a value of type <code>JRDataSource</code>, <code>java.util.Collection</code>,
 * object array (in that order).
 *
 * <p>Subclasses need to implement two template methods: <code>createExporter</code>
 * to create a JasperReports exporter for a specific output format, and
 * <code>useWriter</code> to determine whether to write text or binary content.
 *
 * <p>Provides support for sub-reports through the <code>subReportUrls</code> and
 * <code>subReportDataKeys</code> properties.
 *
 * <p>When using sub-reports, the master report should be configured using the
 * <code>url</code> property and the sub-reports files should be configured using
 * the <code>subReportUrls</code> property. Each entry in the <code>subReportUrls</code>
 * Map corresponds to an individual sub-report. The key of an entry must match up
 * to a sub-report parameter in your report file of type
 * <code>net.sf.jasperreports.engine.JasperReport</code>,
 * and the value of an entry must be the URL for the sub-report file.
 *
 * <p>For sub-reports that require an instance of <code>JRDataSource</code>, that is,
 * they don't have a hard-coded query for data retrieval, you can include the
 * appropriate data in your model as would with the data source for the parent report.
 * However, you must provide a List of parameter names that need to be converted to
 * <code>JRDataSource</code> instances for the sub-report via the
 * <code>subReportDataKeys</code> property. When using <code>JRDataSource</code>
 * instances for sub-reports, you <i>must</i> specify a value for the
 * <code>reportDataKey</code> property, indicating the data to use for the main report.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @see #setUrl
 * @see #getReportData
 * @see #createExporter
 * @see #useWriter
 * @since 1.1.3
 */
public abstract class AbstractJasperReportsView extends AbstractUrlBasedView {

	/**
	 * A String key used to lookup the <code>JRDataSource</code> in the model.
	 */
	private String reportDataKey;

	/**
	 * Stores the paths to any sub-report files used by this top-level report,
	 * along with the keys they are mapped to in the top-level report file.
	 */
	private Properties subReportUrls;

	/**
	 * Stores the names of any data source objects that need to be converted to
	 * <code>JRDataSource</code> instances and included in the report parameters
	 * to be passed on to a sub-report.
	 */
	private String[] subReportDataKeys;

	/**
	 * The <code>JasperReport</code> that is used to render the view.
	 */
	private JasperReport report;

	/**
	 * Holds mappings between sub-report keys and <code>JasperReport</code> objects.
	 */
	private Map subReports;


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
	 * Specify resource paths which must be loaded as instances of
	 * <code>JasperReport</code> and passed to the JasperReports engine for
	 * rendering as sub-reports, under the same keys as in this mapping.
	 * @param subReports mapping between model keys and resource paths
	 * (Spring resource locations)
	 * @see #setUrl
	 * @see org.springframework.context.ApplicationContext#getResource
	 */
	public void setSubReportUrls(Properties subReports) {
		this.subReportUrls = subReports;
	}

	/**
	 * Set the list of names corresponding to the model parameters that will contain
	 * data source objects for use in sub-reports. Spring will convert these objects
	 * to instances of <code>JRDataSource</code> where applicable and will then
	 * include the resulting <code>JRDataSource</code> in the parameters passed into
	 * the JasperReports engine.
	 * <p>The name specified in the list should correspond to an attribute in the
	 * model Map, and to a sub-report data source parameter in your report file.
	 * If you pass in <code>JRDataSource</code> objects as model attributes,
	 * specifing this list of keys is not required.
	 * <p>If you specify a list of sub-report data keys, it is required to also
	 * specify a <code>reportDataKey</code> for the main report, to avoid confusion
	 * between the data source objects for the various reports involved.
	 * @param subReportDataKeys list of names for sub-report data source objects
	 * @see #setReportDataKey
	 * @see #convertReportData
	 * @see net.sf.jasperreports.engine.JRDataSource
	 * @see net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
	 * @see net.sf.jasperreports.engine.data.JRBeanArrayDataSource
	 */
	public void setSubReportDataKeys(String[] subReportDataKeys) {
		this.subReportDataKeys = subReportDataKeys;
	}


	/**
	 * Checks to see that a valid report file URL is supplied in the
	 * configuration. Compiles the report file is necessary.
	 */
	protected void initApplicationContext() throws ApplicationContextException {
		super.initApplicationContext();

		Resource mainReport = getApplicationContext().getResource(getUrl());
		this.report = loadReport(mainReport);

		// Load sub reports if required, and check data source parameters.
		if (this.subReportUrls != null) {
			if (this.subReportDataKeys != null && this.subReportDataKeys.length > 0 &&
					this.reportDataKey == null) {
				throw new ApplicationContextException(
						"'reportDataKey' for main report is required when specifying a value for 'subReportDataKeys'");
			}
			this.subReports = new HashMap(this.subReportUrls.size());
			for (Enumeration urls = this.subReportUrls.propertyNames(); urls.hasMoreElements();) {
				String key = (String) urls.nextElement();
				String path = this.subReportUrls.getProperty(key);
				Resource resource = getApplicationContext().getResource(path);
				this.subReports.put(key, loadReport(resource));
			}
		}
	}

	/**
	 * Loads a <code>JasperReport</code> from the specified <code>Resource</code>. If
	 * the <code>Resource</code> points to an uncompiled report design file then the
	 * report file is compiled dynamically and loaded into memory.
	 * @param resource the <code>Resource</code> containing the report definition or design.
	 * @return a <code>JasperReport</code> instance.
	 */
	private JasperReport loadReport(Resource resource) {
		try {
			String fileName = resource.getFilename();
			if (fileName.endsWith(".jasper")) {
				// load pre-compiled report
				if (logger.isInfoEnabled()) {
					logger.info("Loading pre-compiled Jasper Report from " + resource);
				}
				return (JasperReport) JRLoader.loadObject(resource.getInputStream());
			}
			else if (fileName.endsWith(".jrxml")) {
				// compile report on-the-fly
				if (logger.isInfoEnabled()) {
					logger.info("Compiling Jasper Report loaded from " + resource);
				}
				JasperDesign design = JRXmlLoader.load(resource.getInputStream());
				return getReportCompiler().compileReport(design);
			}
			else {
				throw new IllegalArgumentException("Report URL [" + getUrl() + "] must end in either .jasper or .jrxml");
			}
		}
		catch (IOException ex) {
			throw new ApplicationContextException("Could not load JasperReports report for URL [" + getUrl() + "]", ex);
		}
		catch (JRException ex) {
			throw new ApplicationContextException("Could not parse JasperReports report for URL [" + getUrl() + "]", ex);
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
	 * @param model the model map, as passed in for view rendering. Must contain
	 * a report data value that can be converted to a <code>JRDataSource</code>,
	 * acccording to the <code>getReportData</code> method.
	 * @see #getReportData
	 */
	protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		response.setContentType(getContentType());

		// Determine JRDataSource for main report.
		JRDataSource dataSource = getReportData(model);

		if (this.subReports != null) {
			// Expose sub-reports as model attributes.
			model.putAll(this.subReports);

			// Transform any collections etc into JRDataSources for sub reports.
			if (this.subReportDataKeys != null) {
				for (int i = 0; i < this.subReportDataKeys.length; i++) {
					String key = this.subReportDataKeys[i];
					model.put(key, convertReportData(model.get(key)));
				}
			}
		}

		renderReport(this.report, model, dataSource, response);
	}

	/**
	 * Find an instance of <code>JRDataSource</code> in the given model map or create an
	 * appropriate JRDataSource for passed-in report data.
	 * <p>The default implementation checks for a model object under the
	 * specified "reportDataKey" first, then falls back to looking for a value
	 * of type <code>JRDataSource</code>, <code>java.util.Collection</code>,
	 * object array (in that order).
	 * @param model the model map, as passed in for view rendering
	 * @return the <code>JRDataSource</code>
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
