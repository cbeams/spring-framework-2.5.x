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

import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.core.io.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletException;
import java.util.Map;
import java.util.Locale;
import java.io.IOException;

import jxl.Workbook;
import jxl.write.WritableWorkbook;
import jxl.read.biff.BiffException;

/**
 * Convenient superclass for Excel document views.
 * 
 * <p>
 * This class uses the <i>JExcelAPI</i> instead of <i>POI</i>. More
 * information on <i>JExcelAPI</i> can be found on their <a
 * href="http://www.andykhan.com/jexcelapi/" target="_blank">website</a>.
 * </p>
 * 
 * <p>
 * Properties:
 * <ul>
 * <li>url (optional): The url of an existing Excel document to pick as a
 * starting point. It is done without localization part nor the .xls extension.
 * </ul>
 * 
 * <p>
 * The file will be searched with names in the following order:
 * <ul>
 * <li>[url]_[language]_[country].xls
 * <li>[url]_[language].xls
 * <li>[url].xls
 * </ul>
 * 
 * <p>
 * For working with the workbook in the subclass, see <a
 * href="http://www.andykhan.com/jexcelapi/">Java Excel API site</a>
 * 
 * <p>
 * As an example, you can try this snippet:
 * 
 * <pre>
 * protected void buildExcelDocument(Map model, WritableWorkbook workbook, HttpServletRequest request,
 * 		HttpServletResponse response) {
 * 	if (workbook.getNumberOfSheets() == 0) {
 * 		workbook.createSheet(&quot;Spring&quot;, 0);
 * 	}
 * 
 * 	WritableSheet sheet = workbook.getSheet(&quot;Spring&quot;);
 * 	Label label = new Label(0, 0, &quot;This is a nice label&quot;);
 * 	sheet.addCell(label);
 * }
 * </pre>
 * 
 * <p>
 * The use of this view is close to the AbstractExcelView class.
 * 
 * @see AbstractPdfView
 * @see AbstractExcelView
 * 
 * @author Bram Smeets
 * @author Alef Arendsen
 */
public abstract class AbstractJExcelView extends AbstractView {
	/** The extension to look for existing templates. */
	private static final String EXTENSION = ".xls";

	/** The separator to use to search for locale specific templates. */
	private static final String SEPARATOR = "_";

	/** The url at which the template to use is located. */
	private String url;

	/**
	 * Default Constructor.
	 * <p>
	 * It sets the content type of the view to
	 * <code>application/vnd.ms-excel</code>.
	 * </p>
	 */
	public AbstractJExcelView() {
		setContentType("application/vnd.ms-excel");
	}

	/**
	 * Sets the url of the Excel workbook source without localization part nor
	 * extension.
	 * 
	 * @param url the url of the template workbook
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Renders the excel view, given the specified model.
	 * 
	 * @param model combined output Map (never null), with dynamic values taking
	 *        precedence over static attributes
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws Exception if rendering failed
	 */
	protected final void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		// set the content type and get the output stream
		response.setContentType(getContentType());
		ServletOutputStream out = response.getOutputStream();

		WritableWorkbook workbook;
		if (this.url != null) {
			Workbook template = getTemplateSource(this.url, request);
			workbook = Workbook.createWorkbook(out, template);
		} 
		else {
			workbook = Workbook.createWorkbook(out);
			logger.debug("Created Excel Workbook from scratch");
		}

		buildExcelDocument(model, workbook, request, response);

		// response.setContentLength(workbook.getBytes().length);

		workbook.write();
		out.flush();
		workbook.close();
	}

	/**
	 * Creates the workbook from an existing XLS document.
	 * 
	 * @param location URL of the Excel template without localization part nor
	 *        extension
	 * @param request current HTTP request
	 * @return the template workbook
	 */
	protected Workbook getTemplateSource(String location, HttpServletRequest request) throws ServletException,
			IOException, BiffException {

		String source = null;
		Resource inputFile = null;

		Locale userLocale = RequestContextUtils.getLocale(request);
		String lang = userLocale.getLanguage();
		String country = userLocale.getCountry();

		// check for document with language and country localisation
		if (country.length() > 1) {
			source = location + SEPARATOR + lang + SEPARATOR + country + EXTENSION;
			inputFile = getApplicationContext().getResource(source);
		}

		// check for document with language localisation
		if ((inputFile == null || !inputFile.exists()) && lang.length() > 1) {
			source = location + SEPARATOR + lang + EXTENSION;
			inputFile = getApplicationContext().getResource(source);
		}

		// check for document without localisation
		if (inputFile == null || !inputFile.exists()) {
			source = location + EXTENSION;
			inputFile = getApplicationContext().getResource(source);
		}

		// create the Excel document from source
		Workbook workBook = Workbook.getWorkbook(inputFile.getInputStream());
		if (logger.isDebugEnabled()) {
			logger.debug("Loaded Excel workbook '" + source + '\'');
		}
		return workBook;
	}

	/**
	 * Subclasses must implement this method to create an Excel Workbook
	 * document, given the model.
	 * 
	 * @param model the model Map
	 * @param workbook the Excel workbook to complete
	 * @param request in case we need locale etc. Shouldn't look at attributes.
	 * @param response in case we need to set cookies. Shouldn't write to it.
	 */
	protected abstract void buildExcelDocument(Map model, WritableWorkbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception;
}