/*
 * Created on Nov 6, 2004
 */
package org.springframework.ui.jasperreports;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.servlet.view.jasperreports.MyBean;

/**
 * @author robh
 *
 */
public class JasperReportsUtilsTests extends TestCase {

	private static final String UNCOMPILED_REPORT = "./test/org/springframework/web/servlet/view/jasperreports/DataSourceReport.jrxml";
	
	private static final String COMPILED_REPORT = "./test/org/springframework/web/servlet/view/jasperreports/DataSourceReport.jasper";
	
	public void testCompileReportWithStringPath()  throws Exception {
		JasperReport rpt = JasperReportsUtils.compileReport(UNCOMPILED_REPORT);
		
		assertNotNull("Report should not be null", rpt);
	}
	
	public void testCompileReportWithInputStream() throws Exception {
		InputStream is = new FileInputStream(UNCOMPILED_REPORT);
		JasperReport rpt = JasperReportsUtils.compileReport(is);
		
		assertNotNull("Report should not be null", rpt);
	}
	
	public void testLoadReportWithStringPath() throws Exception {
		JasperReport rpt = getReportByPath();
		
		assertNotNull("Report should not be null", rpt);
	}
	
	public void testLoadReportWithInputStream() throws Exception {
		JasperReport rpt = getReportByInputStream();
		
		assertNotNull("Report should not be null", rpt);
	}
	
	public void testRenderAsCsvWithDataSource() throws Exception {
		StringWriter writer = new StringWriter();
		JasperReportsUtils.renderAsCsv(getReportByPath(), getModel(), writer, getDataSource());
		
		String output = writer.getBuffer().toString();
		assertCsvOutputCorrect(output);
	}
	
	public void testRenderAsCsvWithCollection() throws Exception {
		StringWriter writer = new StringWriter();
		JasperReportsUtils.renderAsCsv(getReportByPath(), getModel(), writer, getData());
		
		String output = writer.getBuffer().toString();
		assertCsvOutputCorrect(output);
	}
	
	public void testRenderAsHtmlWithDataSource() throws Exception {
		StringWriter writer = new StringWriter();
		JasperReportsUtils.renderAsHtml(getReportByPath(), getModel(), writer, getDataSource());
		
		String output = writer.getBuffer().toString();
		assertHtmlOutputCorrect(output);
	}
	
	public void testRenderAsHtmlWithCollection() throws Exception {
		StringWriter writer = new StringWriter();
		JasperReportsUtils.renderAsHtml(getReportByPath(), getModel(), writer, getData());
		
		String output = writer.getBuffer().toString();
		assertHtmlOutputCorrect(output);
	}
	
	public void testRenderAsPdfWithDataSource() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JasperReportsUtils.renderAsPdf(getReportByPath(), getModel(), os, getDataSource());
		
		byte[] output = os.toByteArray();
		assertPdfOutputCorrect(output);
	}
	
	public void testRenderAsPdfWithCollection() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JasperReportsUtils.renderAsPdf(getReportByPath(), getModel(), os, getData());
		
		byte[] output = os.toByteArray();
		assertPdfOutputCorrect(output);
	}
	
	public void testRenderAsXlsWithDataSource() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JasperReportsUtils.renderAsXls(getReportByPath(), getModel(), os, getDataSource());
		
		byte[] output = os.toByteArray();
		assertXlsOutputCorrect(output);
	}
	
	public void testRenderAsXlsWithCollection() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JasperReportsUtils.renderAsXls(getReportByPath(), getModel(), os, getData());
		
		byte[] output = os.toByteArray();
		assertXlsOutputCorrect(output);
	}
	
	public void testFillReport() throws Exception {
		JasperPrint print = JasperReportsUtils.fillReport(getReportByPath(), getModel(), getDataSource());
		assertNotNull("JasperPrint should not be null", print);
	}
	
	public void testRenderWithWriter() throws Exception {
		StringWriter writer = new StringWriter();
		JasperPrint print = JasperReportsUtils.fillReport(getReportByPath(), getModel(), getDataSource());
		JasperReportsUtils.render(print, writer, new JRHtmlExporter());
		
		String output = writer.getBuffer().toString();
		assertHtmlOutputCorrect(output);
	}
	
	public void testRenderWithOutputStream() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JasperPrint print = JasperReportsUtils.fillReport(getReportByPath(), getModel(), getDataSource());
		JasperReportsUtils.render(print, os, new JRPdfExporter());
		
		byte[] output = os.toByteArray();
		assertPdfOutputCorrect(output);
	}
	
	private void assertCsvOutputCorrect(String output) {
		assertTrue("Output length should be greater than 0", (output.length()  > 0));
		assertTrue("Output should start with Dear Lord!", output.startsWith("Dear Lord!"));
	}
	
	private void assertHtmlOutputCorrect(String output) {
		assertTrue("Output length should be greater than 0", (output.length()  > 0));
		assertTrue("Output should contain <html>", output.indexOf("<html>") > -1);
	}
	
	private void assertPdfOutputCorrect(byte[] output) throws Exception {
		assertTrue("Output length should be greater than 0", (output.length  > 0));
		
		String translated = new String(output, "US-ASCII");
		assertTrue("Output should start with %PDF", translated.startsWith("%PDF"));
	}
	
	private void assertXlsOutputCorrect(byte[] output) throws Exception {
		HSSFWorkbook workbook = new HSSFWorkbook(new ByteArrayInputStream(output));
		HSSFSheet sheet = workbook.getSheetAt(0);

		assertNotNull("Sheet should not be null", sheet);
		
		HSSFRow row = sheet.getRow(3);
		HSSFCell cell = row.getCell((short)1);
		assertNotNull("Cell should not be null", cell);
		assertEquals("Cell content should be Dear Lord!", "Dear Lord!", cell.getStringCellValue());
	}
	
	private JasperReport getReportByPath() throws Exception {
		return JasperReportsUtils.loadReport(COMPILED_REPORT);
	}
	
	private JasperReport getReportByInputStream() throws Exception {
		InputStream is = new FileInputStream(COMPILED_REPORT);
		JasperReport rpt = JasperReportsUtils.loadReport(is);
		return rpt;
	}
	
	private Map getModel() {
		Map model = new HashMap();
		model.put("ReportTitle", "Dear Lord!");
		model.put("dataSource", new JRBeanCollectionDataSource(getData()));

		return model;
	}
	
	private List getData() {
		List list = new ArrayList();

		for (int x = 0; x < 10; x++) {
			MyBean bean = new MyBean();
			bean.setId(x);
			bean.setName("Rob Harrop");
			bean.setStreet("foo");

			list.add(bean);
		}

		return list;
	}
	
	private JRDataSource getDataSource() {
		return  new JRBeanCollectionDataSource(getData());
	}
	
}
