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
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import org.springframework.ui.jasperreports.JasperReportsUtils;

/**
 * Extends <code>AbstractJasperReportsView</code> to provide basic rendering logic for
 * views that are fixed format, i.e. always PDF or always HTML.
 * @author Rob Harrop
 * @author Juergen Hoeller
 */
public abstract class AbstractJasperReportsSingleFormatView extends AbstractJasperReportsView {

	/**
	 * Subclasses should implement this method to perform the actual rendering process.
	 *
	 * @param report the <code>JasperReport</code> to render
	 * @param parameters the map containing report parameters
	 * @param dataSource the <code>JRDataSource</code> containing the report data
	 * @param response the HTTP response the report should be rendered to
	 * @throws Exception if rendering failed
	 */
	protected void renderReport(JasperReport report, Map parameters, JRDataSource dataSource, HttpServletResponse response)
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
	 *
	 * @see #useWriter
	 */
	protected abstract JRAbstractExporter createExporter();

	/**
	 * Return whether to use a <code>java.io.Writer</code> to write text content
	 * to the HTTP response. Else, a <code>java.io.OutputStream</code> will be used,
	 * to write binary content to the response.
	 *
	 * @see javax.servlet.ServletResponse#getWriter
	 * @see javax.servlet.ServletResponse#getOutputStream
	 */
	protected abstract boolean useWriter();
}
