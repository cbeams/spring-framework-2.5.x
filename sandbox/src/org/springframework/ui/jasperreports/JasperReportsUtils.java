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
package org.springframework.ui.jasperreports;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JRBshCompiler;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility methods for working with JasperReports. Provides a set of convenience
 * methods for generating reports in a CSV, HTML, PDF and XLS formats.
 * 
 * @author robh
 */
public class JasperReportsUtils {

    /**
     * <code>Log</code> instance for this class
     */
    private static final Log log = LogFactory.getLog(JasperReportsUtils.class);

    /**
     * Implementation of <code>JRAbstractExporter</code> used to generate CSV
     * output. Used whenever exporting a report to CSV format.
     */
    private static final JRAbstractExporter CSV_EXPORTER = new JRCsvExporter();

    /**
     * Implementation of <code>JRAbstractExporter</code> used to generate HTML
     * output. Used whenever exporting a report to HTML format.
     */
    private static final JRAbstractExporter HTML_EXPORTER = new JRHtmlExporter();

    /**
     * Implementation of <code>JRAbstractExporter</code> used to generate PDF
     * output. Used whenever exporting a report to PDF format.
     */
    private static final JRAbstractExporter PDF_EXPORTER = new JRPdfExporter();

    /**
     * Compiles an XML report definition in a .jrxml file into an instance of
     * <code>JasperReport</code>.
     * 
     * @param jrxmlPath
     *            The path to the .jrxml file.
     * @return A <code>JasperReport</code> instance representing the compiled
     *         report.
     * @throws JRException
     *             when there is an error in the report definition.
     * @throws FileNotFoundException
     *             when the .jrxml file does not exist.
     */
    public static JasperReport compileReport(String jrxmlPath)
            throws JRException, FileNotFoundException {
        InputStream is = new FileInputStream(jrxmlPath);

        try {
            return compileReport(is);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                log.warn("Failed to close report file.", ex);
            }
        }
    }

    /**
     * Compiles an XML report definition into an instance of
     * <code>JasperReport</code>.
     * 
     * @param inputStream
     *            An <code>InputStream</code> containing the XML report
     *            definition data.
     * @return A <code>JasperReport</code> instance representing the compiled
     *         report.
     * @throws JRException
     *             when there is an error in the report definition.
     */
    public static JasperReport compileReport(InputStream inputStream)
            throws JRException {
        JRBshCompiler compiler = new JRBshCompiler();
        return compiler.compileReport(JasperManager.loadXmlDesign(inputStream));
    }

    /**
     * Loads a pre-compiled <code>JasperReport</code> from a file.
     * 
     * @param jasperPath
     *            The path to the compiled report file.
     * @return A <code>JasperReport</code> instance representing the compiled
     *         report.
     * @throws JRException
     *             when there is an error with the compiled report file.
     */
    public static JasperReport loadReport(String jasperPath) throws JRException {
        return JasperManager.loadReport(jasperPath);
    }

    /**
     * Loads a pre-compiled <code>JasperReport</code> from a stream.
     * 
     * @param inputStream
     * @return A <code>JasperReport</code> instance representing the compiled
     *         report.
     * @throws JRException
     *             when there is an error with the compiled report file.
     */
    public static JasperReport loadReport(InputStream inputStream)
            throws JRException {
        return JasperManager.loadReport(inputStream);
    }

    /**
     * Render a report in CSV format using the supplied <code>Collection</code>
     * as the report data. This <code>Collection</code> should contain
     * JavaBeans with properties corresponding to the field definitions in the
     * report. Writes the results to the supplied <code>Writer</code>.
     * 
     * @param report
     *            The <code>JasperReport</code> instance to render
     * @param model
     *            The parameters to use for rendering.
     * @param writer
     *            The <code>Writer</code> to write the rendered report to.
     * @param reportData
     *            A <code>Collection</code> of JavaBeans representing the data
     *            with which to fill the report.
     * @throws JRException
     *             if the render operation fails.
     */
    public static void renderAsCsv(JasperReport report, Map model,
            Writer writer, Collection reportData) throws JRException {
        renderAsCsv(report, model, writer, new JRBeanCollectionDataSource(
                reportData));
    }

    /**
     * Render a report in CSV format using the supplied
     * <code>JRDataSource</code>. Writes the results to the supplied
     * <code>Writer</code>.
     * 
     * @param report
     *            The <code>JasperReport</code> instance to render
     * @param model
     *            The parameters to use for rendering.
     * @param writer
     *            The <code>Writer</code> to write the rendered report to.
     * @param dataSource
     *            A <code>JRDataSource</code> containing the data with which
     *            to fill the report.
     * @throws JRException
     *             if the render operation fails.
     */
    public static void renderAsCsv(JasperReport report, Map model,
            Writer writer, JRDataSource dataSource) throws JRException {
        JasperPrint print = fillReport(report, model, dataSource);
        render(print, writer, CSV_EXPORTER);
    }

    /**
     * Render a report in HTML format using the supplied <code>Collection</code>
     * as the report data. This <code>Collection</code> should contain
     * JavaBeans with properties corresponding to the field definitions in the
     * report. Writes the results to the supplied <code>Writer</code>.
     * 
     * @param report
     *            The <code>JasperReport</code> instance to render
     * @param model
     *            The parameters to use for rendering.
     * @param writer
     *            The <code>Writer</code> to write the rendered report to.
     * @param reportData
     *            A <code>Collection</code> of JavaBeans representing the data
     *            with which to fill the report.
     * @throws JRException
     *             if the render operation fails.
     */
    public static void renderAsHtml(JasperReport report, Map model,
            Writer writer, Collection reportData) throws JRException {
        renderAsHtml(report, model, writer, new JRBeanCollectionDataSource(
                reportData));
    }

    /**
     * Render a report in HTML format using the supplied
     * <code>JRDataSource</code>. Writes the results to the supplied
     * <code>Writer</code>.
     * 
     * @param report
     *            The <code>JasperReport</code> instance to render
     * @param model
     *            The parameters to use for rendering.
     * @param writer
     *            The <code>Writer</code> to write the rendered report to.
     * @param dataSource
     *            A <code>JRDataSource</code> containing the data with which
     *            to fill the report.
     * @throws JRException
     *             if the render operation fails.
     */
    public static void renderAsHtml(JasperReport report, Map model,
            Writer writer, JRDataSource dataSource) throws JRException {
        JasperPrint print = fillReport(report, model, dataSource);
        render(print, writer, HTML_EXPORTER);
    }

    /**
     * Render a report in PDF format using the supplied <code>Collection</code>
     * as the report data. This <code>Collection</code> should contain
     * JavaBeans with properties corresponding to the field definitions in the
     * report. Writes the results to the supplied <code>OutputStream</code>.
     * 
     * @param report
     *            The <code>JasperReport</code> instance to render
     * @param model
     *            The parameters to use for rendering.
     * @param outputStream
     *            The <code>OutputStream</code> to write the rendered report
     *            to.
     * @param reportData
     *            A <code>Collection</code> of JavaBeans representing the data
     *            with which to fill the report.
     * @throws JRException
     *             if the render operation fails.
     */
    public static void renderAsPdf(JasperReport report, Map model,
            OutputStream outputStream, Collection reportData)
            throws JRException {
        renderAsPdf(report, model, outputStream,
                new JRBeanCollectionDataSource(reportData));
    }

    /**
     * Render a report in PDF format using the supplied
     * <code>JRDataSource</code>. Writes the results to the supplied
     * <code>OutputStream</code>.
     * 
     * @param report
     *            The <code>JasperReport</code> instance to render
     * @param model
     *            The parameters to use for rendering.
     * @param outputStream
     *            The <code>OutputStream</code> to write the rendered report
     *            to.
     * @param dataSource
     *            A <code>JRDataSource</code> containing the data with which
     *            to fill the report.
     * @throws JRException
     *             if the render operation fails.
     */
    public static void renderAsPdf(JasperReport report, Map model,
            OutputStream outputStream, JRDataSource dataSource)
            throws JRException {
        JasperPrint print = fillReport(report, model, dataSource);
        render(print, outputStream, PDF_EXPORTER);
    }

    /**
     * Render a report in XLS format using the supplied <code>Collection</code>
     * as the report data. This <code>Collection</code> should contain
     * JavaBeans with properties corresponding to the field definitions in the
     * report. Writes the results to the supplied <code>OutputStream</code>.
     * 
     * @param report
     *            The <code>JasperReport</code> instance to render
     * @param model
     *            The parameters to use for rendering.
     * @param outputStream
     *            The <code>OutputStream</code> to write the rendered report
     *            to.
     * @param reportData
     *            A <code>Collection</code> of JavaBeans representing the data
     *            with which to fill the report.
     * @throws JRException
     *             if the render operation fails.
     */
    public static void renderAsXls(JasperReport report, Map model,
            OutputStream outputStream, Collection reportData)
            throws JRException {
        renderAsXls(report, model, outputStream,
                new JRBeanCollectionDataSource(reportData));
    }

    /**
     * Render a report in XLS format using the supplied
     * <code>JRDataSource</code>. Writes the results to the supplied
     * <code>OutputStream</code>.
     * 
     * @param report
     *            The <code>JasperReport</code> instance to render
     * @param model
     *            The parameters to use for rendering.
     * @param outputStream
     *            The <code>OutputStream</code> to write the rendered report
     *            to.
     * @param dataSource
     *            A <code>JRDataSource</code> containing the data with which
     *            to fill the report.
     * @throws JRException
     *             if the render operation fails.
     */
    public static void renderAsXls(JasperReport report, Map model,
            OutputStream outputStream, JRDataSource dataSource)
            throws JRException {
        JasperPrint print = fillReport(report, model, dataSource);
        // using the same instance of JRXlsExporter multiple times
        // causes the 2nd workbook created to be truncated.
        render(print, outputStream, new JRXlsExporter());
    }

    /**
     * Fills the given <code>JasperReport</code> with the data in the supplied
     * <code>JRDataSource</code> and <code>Map</code> objects.
     * 
     * @param report
     *            The <code>JasperReport</code> to fill.
     * @param model
     *            A <code>Map</code> containing the parameter data for the
     *            report.
     * @param dataSource
     *            A <code>JRDataSource</code> containing the report data.
     * @return A <code>JasperPrint</code> instance ready for rendering.
     * @throws JRException
     *             if the fill operation fails.
     */
    public static JasperPrint fillReport(JasperReport report, Map model,
            JRDataSource dataSource) throws JRException {
        return JasperFillManager.fillReport(report, model, dataSource);
    }

    /**
     * Render the supplied <code>JasperPrint</code> instance using the
     * supplied <code>JRAbstractExporter</code> instance and write the results
     * to the supplied <code>Writer</code>. Make sure that the
     * <code>JRAbstractExporter</code> implementation you supply is capable of
     * writing to a <code>Writer</code>.
     * 
     * @param print
     *            The <code>JasperPrint</code> instance to render.
     * @param writer
     *            The <code>Writer</code> to write the results to.
     * @param exporter
     *            The <code>JRAbstractExporter</code> to use to create the
     *            rendered report.
     * @throws JRException
     *             if rendering fails.
     */
    public static void render(JasperPrint print, Writer writer,
            JRAbstractExporter exporter) throws JRException {
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
        exporter.setParameter(JRExporterParameter.OUTPUT_WRITER, writer);

        exporter.exportReport();
    }

    /**
     * Render the supplied <code>JasperPrint</code> instance using the
     * supplied <code>JRAbstractExporter</code> instance and write the results
     * to the supplied <code>OutputStream</code>. Make sure that the
     * <code>JRAbstractExporter</code> implementation you supply is capable of
     * writing to a <code>OutputStream</code>.
     * 
     * @param print
     *            The <code>JasperPrint</code> instance to render.
     * @param outputStream
     *            The <code>OutputStream</code> to write the results to.
     * @param exporter
     *            The <code>JRAbstractExporter</code> to use to create the
     *            rendered report.
     * @throws JRException
     *             if rendering fails.
     */
    public static void render(JasperPrint print, OutputStream outputStream,
            JRAbstractExporter exporter) throws JRException {
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);

        exporter.exportReport();
    }

}
