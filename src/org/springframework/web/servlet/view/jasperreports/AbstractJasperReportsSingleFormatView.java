/*
 * Copyright 2002-2005 the original author or authors.
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
import net.sf.jasperreports.engine.JasperPrint;

import org.springframework.ui.jasperreports.JasperReportsUtils;

/**
 * Extends <code>AbstractJasperReportsView</code> to provide basic rendering logic for
 * views that are fixed format, i.e. always PDF or always HTML.
 *
 * <p>Subclasses need to implement two template methods: <code>createExporter</code>
 * to create a JasperReports exporter for a specific output format, and
 * <code>useWriter</code> to determine whether to write text or binary content.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.1.5
 * @see #createExporter()
 * @see #useWriter()
 */
public abstract class AbstractJasperReportsSingleFormatView extends AbstractJasperReportsView {

	/**
	 * Initial size for the output array.
	 */
	private static final int OUTPUT_BYTE_ARRAY_INITIAL_SIZE = 4096;

	/**
	 * Perform rendering for a single Jasper Reports exporter,
	 * i.e. a pre-defined output format.
	 */
	protected void renderReport(
			JasperPrint populatedReport, Map parameters, HttpServletResponse response)
			throws Exception {

		// Prepare report for rendering.
		JRAbstractExporter exporter = createExporter();

		if (getConvertedExporterParameters() != null) {
			exporter.setParameters(getConvertedExporterParameters());
		}

		if (useWriter()) {
			// Render report into HttpServletResponse's Writer.
			JasperReportsUtils.render(exporter, populatedReport, response.getWriter());
		}
		else {
			// Render report into local OutputStream.
			// IE workaround: write into byte array first.
			ByteArrayOutputStream baos = new ByteArrayOutputStream(OUTPUT_BYTE_ARRAY_INITIAL_SIZE);
			JasperReportsUtils.render(exporter, populatedReport, baos);

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
