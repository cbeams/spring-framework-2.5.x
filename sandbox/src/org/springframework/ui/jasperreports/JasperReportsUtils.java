/*
 * Created on Nov 6, 2004
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
 * @author robh
 * 
 */
public class JasperReportsUtils {

	private static final Log log = LogFactory.getLog(JasperReportsUtils.class);

	private static final JRAbstractExporter CSV_EXPORTER = new JRCsvExporter();

	private static final JRAbstractExporter HTML_EXPORTER = new JRHtmlExporter();

	private static final JRAbstractExporter PDF_EXPORTER = new JRPdfExporter();

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

	public static JasperReport compileReport(InputStream inputStream)
			throws JRException {
		JRBshCompiler compiler = new JRBshCompiler();
		return compiler.compileReport(JasperManager.loadXmlDesign(inputStream));
	}

	public static JasperReport loadReport(String jasperPath) throws JRException {
		return JasperManager.loadReport(jasperPath);
	}

	public static JasperReport loadReport(InputStream inputStream)
			throws JRException {
		return JasperManager.loadReport(inputStream);
	}

	public static void renderAsCsv(JasperReport report, Map model,
			Writer writer, Collection reportData) throws JRException {
		renderAsCsv(report, model, writer, new JRBeanCollectionDataSource(
				reportData));
	}

	public static void renderAsCsv(JasperReport report, Map model,
			Writer writer, JRDataSource dataSource) throws JRException {
		JasperPrint print = fillReport(report, model, dataSource);
		render(print, writer, CSV_EXPORTER);
	}

	public static void renderAsHtml(JasperReport report, Map model,
			Writer writer, Collection reportData) throws JRException {
		renderAsHtml(report, model, writer, new JRBeanCollectionDataSource(
				reportData));
	}

	public static void renderAsHtml(JasperReport report, Map model,
			Writer writer, JRDataSource dataSource) throws JRException {
		JasperPrint print = fillReport(report, model, dataSource);
		render(print, writer, HTML_EXPORTER);
	}

	public static void renderAsPdf(JasperReport report, Map model,
			OutputStream outputStream, Collection reportData)
			throws JRException {
		renderAsPdf(report, model, outputStream,
				new JRBeanCollectionDataSource(reportData));
	}

	public static void renderAsPdf(JasperReport report, Map model,
			OutputStream outputStream, JRDataSource dataSource)
			throws JRException {
		JasperPrint print = fillReport(report, model, dataSource);
		render(print, outputStream, PDF_EXPORTER);
	}

	public static void renderAsXls(JasperReport report, Map model,
			OutputStream outputStream, Collection reportData)
			throws JRException {
		renderAsXls(report, model, outputStream,
				new JRBeanCollectionDataSource(reportData));
	}

	public static void renderAsXls(JasperReport report, Map model,
			OutputStream outputStream, JRDataSource dataSource)
			throws JRException {
		JasperPrint print = fillReport(report, model, dataSource);
		// using the same instance of JRXlsExporter multiple times
		// causes the 2nd workbook created to be truncated.
		render(print, outputStream, new JRXlsExporter());
	}

	public static JasperPrint fillReport(JasperReport report, Map model,
			JRDataSource dataSource) throws JRException {
		return JasperFillManager.fillReport(report, model, dataSource);
	}

	public static void render(JasperPrint print, Writer writer,
			JRAbstractExporter exporter) throws JRException {
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
		exporter.setParameter(JRExporterParameter.OUTPUT_WRITER, writer);

		exporter.exportReport();
	}

	public static void render(JasperPrint print, OutputStream outputStream,
			JRAbstractExporter exporter) throws JRException {
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);

		exporter.exportReport();
	}

}
