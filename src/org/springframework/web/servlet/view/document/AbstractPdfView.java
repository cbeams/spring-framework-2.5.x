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

import java.io.ByteArrayOutputStream;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.web.servlet.view.AbstractView;
 
/**
 * Abstract superclass for PDF views, using Bruno Lowagie's
 * iText package. Application-specific view classes will extend this class.
 * The view will be held in such a subclass, not a template such as a JSP.
 * <br>See <a href="http://www.amazon.com/exec/obidos/tg/detail/-/0764543857/">Expert One-On-One J2EE Design and Development</a> 
 * by Rod Johnson, pp 571-575 for an example of use of this class.
 * <br>NB: Internet Explorer requires a .pdf extension, as
 * it doesn't always respect the declared content type.
 * <br>Exposes page width and height as bean properties.
 * @version $Id: AbstractPdfView.java,v 1.8 2004-03-18 02:46:18 trisberg Exp $
 * @author Rod Johnson
 * @author Jean-Pierre Pawlak
 */
public abstract class AbstractPdfView extends AbstractView {
	
	/**
	 * Sets the appropriate content type.
	 * Note that IE won't take much notice of this, but there's not a lot we
	 * can do about this. Generated documents should have a ".pdf" extension.
	*/
	public AbstractPdfView() {
		setContentType("application/pdf");
	}
	
	protected final void renderMergedOutputModel(Map model, HttpServletRequest request,
	                                             HttpServletResponse response) throws Exception {
		Document document = getDocument();

		// The following simple method doesn't work in IE, which
		// needs to know the content length.
		// PdfWriter.getInstance(document, response.getOutputStream());
		//document.open();
		//doPdfDocument(model, document);
		//document.close();

		// See	http://www.lowagie.com/iText/faq.html#msie
		// for an explanation of why we can't use the obvious form above.

		// IE workaround
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfWriter writer = PdfWriter.getInstance(document, baos);

		writer.setViewerPreferences(getViewerPreferences());
		document.open();
		buildPdfDocument(model, document, writer, request, response);
		document.close();

		response.setContentLength(baos.size());
		response.setContentType(getContentType());
		ServletOutputStream out = response.getOutputStream();
		baos.writeTo(out);
		out.flush();
	}

	/**
	 * Return a new com.lowagie.text.Document.
	 * <br>By default return an A4 document, but the subclass can set anything else or retrieve from properties.
	 * @return the new created Document
	 */
	protected Document getDocument() {
		return new Document(PageSize.A4);
	}

	/**
	 * Return the ViewerPreferences.
	 * <br>By default return AllowPrinting and PageLayoutSinglePage, but can be subclassed.
	 * The subclass can either fix the preferences or retrieve them from the bean properties.
	 * @return an int containing the bits information against PdfWriter definitions.
	 */
	protected int getViewerPreferences() {
		return PdfWriter.AllowPrinting | PdfWriter.PageLayoutSinglePage;
	}

	/**
	 * Subclasses must implement this method to create an iText PDF document,
	 * given the model.
	 * @param request in case we need locale etc. Shouldn't look at attributes
	 * @param response in case we need to set cookies. Shouldn't write to it.
	 */
	protected abstract void buildPdfDocument(Map model, Document pdfDoc, PdfWriter writer, HttpServletRequest request,
																					 HttpServletResponse response) throws Exception;

}

