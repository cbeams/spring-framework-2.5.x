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

import javax.servlet.ServletContext;

import org.enhydra.xml.xmlc.servlet.XMLCContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Loads an <code>XMLCContext</code> and binds into a <code>ServletContext</code> instance.
 *
 * @author Rod Johnson
 * @author Rob Harrop
 */
public class XMLCContextLoader {

	/**
	 * <code>Log</code> implementation for this code.
	 */
	private static final Log logger = LogFactory.getLog(XMLCContextLoader.class);

	/**
	 * Loads an <code>XMLCContext</code> and binds it to the supplied
	 * <code>ServletContext</code>.
	 * @param servletContext
	 * @return
	 */
	public static XMLCContext loadContext(ServletContext servletContext) {
		XMLCContext ctx = XMLCContext.getContext(servletContext);

		if (logger.isInfoEnabled()) {
			logger.info("Loaded XMLCContext [" + ctx + "]");
		}

		return ctx;
	}
}
