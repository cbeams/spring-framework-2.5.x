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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import junit.framework.TestCase;
import org.pdfbox.cos.COSDocument;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.util.PDFTextStripper;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Alef Arendsen
 */
public class PdfTestSuite extends TestCase {

	public void testPdf() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		AbstractPdfView pdfView = new AbstractPdfView() {
			protected void buildPdfDocument(Map model, Document document, PdfWriter writer,
					HttpServletRequest request, HttpServletResponse response) throws Exception {
				document.add(new Paragraph("this should be in the PDF"));
			}
		};

		pdfView.render(new HashMap(), request, response);

		// get the content
		byte[] pdf = response.getContentAsByteArray();
		String text = parsePdf(pdf);
		if (text.indexOf("this should be in the PDF") == -1) {
			fail("The text we put in the PDF wasn't in there when we looked at it!");
		}
	}

	private String parsePdf(byte[] pdf) throws Exception {
		PDFTextStripper stripper = new PDFTextStripper();
		// parse all of it!
		int startPage = 1;
		int endPage = Integer.MAX_VALUE;

		InputStream input = null;
		Writer output = null;
		COSDocument document = null;
		try {
			input = new ByteArrayInputStream(pdf);
			document = parseDocument(input);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			output = new OutputStreamWriter(baos);

			stripper.setStartPage(startPage);
			stripper.setEndPage(endPage);

			stripper.writeText(document, output);
			return new String(baos.toByteArray());
		}
		finally {
			input.close();
			output.close();
			document.close();
		}
	}

	/** parses a PDF document resulting in a COSDocument */
	private static COSDocument parseDocument(InputStream input) throws IOException {
		PDFParser parser = new PDFParser(input);
		parser.parse();
		return parser.getDocument();
	}


}
