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

package org.springframework.web.servlet.view.document;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import org.springframework.core.io.Resource;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.AbstractView;

/**
 * Convenient superclass for Excel document views.
 *
 * <p>Properties:
 * <ul>
 * <li>url (optional): The url of an existing Excel document to pick as a starting point.
 * It is done without localization part nor the .xls extension.
 * </ul>
 *
 * <p>The file will be searched with names in the following order:
 * <ul>
 * <li>[url]_[language]_[country].xls
 * <li>[url]_[language].xls
 * <li>[url].xls
 * </ul>
 *
 * <p>For working with the workbook in the subclass, see
 * <a href="http://jakarta.apache.org/poi/index.html">Jakarta's POI site</a>
 *
 * <p>As an example, you can try this snippet:
 *
 * <pre>
 * protected void buildExcelDocument(
 *     Map model,
 *     HSSFWorkbook workbook,
 *     HttpServletRequest request,
 *     HttpServletResponse response )
 * {
 *     // AModel aModel = ( AModel ) model.get( "amodel" );
 *
 *     HSSFSheet sheet;
 *     HSSFRow   sheetRow;
 *     HSSFCell  cell;
 *
 *     // Go to the first sheet
 *     // getSheetAt: only if workbook is created from an existing document
 * 	   //sheet = workbook.getSheetAt( 0 );
 * 	   sheet = workbook.createSheet("Spring");
 * 	   sheet.setDefaultColumnWidth((short)12);
 *
 *     // write a text at A1
 *     cell = getCell( sheet, 0, 0 );
 *     setText(cell,"Spring POI test");
 *
 *     // Write the current date at A2
 *     HSSFCellStyle dateStyle = workbook.createCellStyle(  );
 *     dateStyle.setDataFormat( HSSFDataFormat.getBuiltinFormat( "m/d/yy" ) );
 *     cell = getCell( sheet, 1, 0 );
 *     cell.setCellValue( new Date() );
 *     cell.setCellStyle( dateStyle );
 *
 *     // Write a number at A3
 *     getCell( sheet, 2, 0 ).setCellValue( 458 );
 *
 *     // Write a range of numbers
 *     sheetRow = sheet.createRow( 3 );
 *     for (short i = 0; i<10; i++) {
 *         sheetRow.createCell(i).setCellValue( i*10 );
 *     }
 * }</pre>
 *
 * <p>The use of this view is close to the AbstractPdfView class.
 *
 * @author Jean-Pierre Pawlak
 * @see AbstractPdfView
 */
public abstract class AbstractExcelView extends AbstractView {

	private static final String EXTENSION = ".xls";

	private static final String SEPARATOR = "_";


	private String url;

	private HSSFWorkbook workbook;


	public AbstractExcelView() {
		setContentType("application/vnd.ms-excel");
	}

	/**
	 * Sets the url of the Excel workbook source without localization part nor extension.
	 */
	public void setUrl(String url) {
		this.url = url;
	}


	/**
	 * Renders the view given the specified model.
	 */
	protected final void renderMergedOutputModel(
			Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (this.url != null) {
			this.workbook = getTemplateSource(this.url, request);
		}
		else {
			this.workbook = new HSSFWorkbook();
			logger.debug("Created Excel Workbook from scratch");
		}

		buildExcelDocument(model, this.workbook, request, response);

		// response.setContentLength(workbook.getBytes().length);
		response.setContentType(getContentType());
		ServletOutputStream out = response.getOutputStream();
		this.workbook.write(out);
		out.flush();
	}

	/**
	 * Creates the workBook from an existing .xls document.
	 * @param url url of the Excle template without localization part nor extension
	 * @param request
	 * @return HSSFWorkbook
	 */
	protected HSSFWorkbook getTemplateSource(String url, HttpServletRequest request)
			throws ServletException, IOException {

		String source = null;
		Resource inputFile = null;

		Locale userLocale = RequestContextUtils.getLocale(request);
		String lang = userLocale.getLanguage();
		String country = userLocale.getCountry();

		// check for document with language and country localisation
		if (country.length() > 1) {
			source = url + SEPARATOR + lang + SEPARATOR + country + EXTENSION;
			inputFile = getApplicationContext().getResource(source);
		}

		// check for document with language localisation
		if ((inputFile == null || !inputFile.exists()) && lang.length() > 1) {
			source = url + SEPARATOR + lang + EXTENSION;
			inputFile = getApplicationContext().getResource(source);
		}

		// check for document without localisation
		if (inputFile == null || !inputFile.exists()) {
			source = url + EXTENSION;
			inputFile = getApplicationContext().getResource(source);
		}

		// create the Excel document from source
		POIFSFileSystem fs = new POIFSFileSystem(inputFile.getInputStream());
		HSSFWorkbook workBook = new HSSFWorkbook(fs);
		if (logger.isDebugEnabled()) {
			logger.debug("Loaded Excel workbook '" + source + "'");
		}
		return workBook;
	}

	/**
	 * Subclasses must implement this method to create an Excel HSSFWorkbook document,
	 * given the model.
	 * @param model
	 * @param wb The Excel workBook to complete
	 * @param request in case we need locale etc. Shouldn't look at attributes
	 * @param response in case we need to set cookies. Shouldn't write to it.
	 */
	protected abstract void buildExcelDocument(
			Map model,	HSSFWorkbook wb, HttpServletRequest request, HttpServletResponse response)
			throws Exception;

	/**
	 * Convenient method to obtain the cell in the given sheet, row and column.
	 * <p>Creates the row and the cell if they still doesn't already exist.
	 * Thus, the column can be passed as an int, the method making the needed downcasts.
	 * @param sheet a sheet object. The first sheet is usually obtained by workbook.getSheetAt(0)
	 * @param row thr row number
	 * @param col the column number
	 * @return the HSSFCell
	 */
	protected HSSFCell getCell(HSSFSheet sheet, int row, int col) {
		HSSFRow sheetRow = sheet.getRow(row);
		if (null == sheetRow) {
			sheetRow = sheet.createRow(row);
		}
		HSSFCell cell = sheetRow.getCell((short) col);
		if (null == cell) {
			cell = sheetRow.createCell((short) col);
		}
		return cell;
	}

	/**
	 * Convenient method to set a String as text content in a cell.
	 * @param cell The cell in which the text must be put
	 * @param text The text to put in the cell
	 */
	protected void setText(HSSFCell cell, String text) {
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(text);
	}

}
