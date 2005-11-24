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

package org.springframework.web.servlet.view.xslt;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXResult;

import org.apache.fop.apps.Driver;

/**
 * Convenient superclass for views rendered to PDF (or other FOP output format)
 * using XSLT-FO stylesheet.
 *
 * Subclasses must provide the XML W3C document to transform.
 * They do not need to concern themselves with XSLT or FOP.
 *
 * <p>In addition to properties specified in {@link AbstractXsltView}, the
 * following can be set:
 * <ul>
 * <li>renderer (int) - sets the output rendering used by the FOP engine. Note that some
 * values for this property (notably Driver.RENDER_AWT and Driver.RENDER_PRINT) may be
 * incompatible with this usage of FOP and will throw runtime exceptions</li>
 * </ul>
 *
 * @author Darren Davison
 * @see AbstractXsltView
 */
public abstract class AbstractXslFoView extends AbstractXsltView {

	/**
	 * Default renderer will be PDF unless overridden.
	 */
	private static final int DEFAULT_RENDERER = Driver.RENDER_PDF;

	private int renderer = DEFAULT_RENDERER;


	/**
	 * Sets the renderer to use for this FOP transformation.  See the available
	 * types in org.apache.fop.apps.Driver.  Defaults to Driver.RENDER_PDF
	 * @param renderer the type of renderer
	 */
	public void setRenderer(int renderer) {
		this.renderer = renderer;
	}


	/**
	 * Perform the actual transformation, writing to the HTTP response via the
	 * FOP Driver.
	 */
	protected void doTransform(
			Map model, Source source, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		Driver driver = new Driver();
		driver.setRenderer(this.renderer);
		driver.setOutputStream(response.getOutputStream());
		Result result = new SAXResult(driver.getContentHandler());

		// Delegate to the superclass for the actual output having constructed the result.
		doTransform(source, getParameters(request), result, response.getCharacterEncoding());
	}

}
