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

package org.springframework.web.servlet.view.xmlc;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.enhydra.xml.io.OutputOptions;
import org.enhydra.xml.xmlc.XMLObject;
import org.enhydra.xml.xmlc.servlet.XMLCContext;

import org.springframework.web.servlet.view.AbstractView;

/**
 * @author Rod Johnson
 * @author Rob Harrop
 */
public abstract class AbstractXmlcView extends AbstractView {

	public static final OutputOptions DEFAULT_OUTPUT_OPTIONS = new OutputOptions();

	protected final Log logger = LogFactory.getLog(getClass());

	private XMLCContext xmlcContext;

	protected void initApplicationContext() {
		this.xmlcContext = XMLCContext.getContext(getServletContext());
	}

	protected final XMLCContext getXmlcContext() {
		return xmlcContext;
	}

	protected void renderMergedOutputModel(
			Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {

		XMLObject xo = createXMLObject(model, request);

		OutputOptions oo = getOutputOptions();
		if (logger.isDebugEnabled()) {
			logger.debug("Using OutputOptions [" + oo + "]");
		}

		// Write the response back to the client.
		response.setContentType(getContentType());
		this.xmlcContext.writeDOM(request, response, oo, xo);
	}

	/**
	 * Subclasses can override this method to change the output options
	 * used by XMLC when writing view data to the client.
	 * @return the <code>OutputOptions</code> to use when writing view
	 * data to the client
	 */
	protected OutputOptions getOutputOptions() {
		return DEFAULT_OUTPUT_OPTIONS;
	}

	protected abstract XMLObject createXMLObject(Map model, HttpServletRequest request) throws Exception;

}
