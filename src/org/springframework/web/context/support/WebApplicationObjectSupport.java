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

package org.springframework.web.context.support;

import java.io.File;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.WebUtils;

/**
 * Convenient superclass for application objects running in a WebApplicationContext.
 * Provides getWebApplicationContext, getServletContext, and getTempDir methods.
 * @author Juergen Hoeller
 * @since 28.08.2003
 */
public abstract class WebApplicationObjectSupport extends ApplicationObjectSupport {

	/**
	 * Return the current application context as WebApplicationContext.
	 */
	protected final WebApplicationContext getWebApplicationContext() {
		ApplicationContext ctx = getApplicationContext();
		if (!(ctx instanceof WebApplicationContext)) {
			throw new IllegalStateException("WebApplicationObjectSupport instance [" + this +
																			"] does not run in a WebApplicationContext but in: " + ctx);
		}
		return (WebApplicationContext) getApplicationContext();
	}

	/**
	 * Return the current ServletContext.
	 */
	protected final ServletContext getServletContext() {
		return getWebApplicationContext().getServletContext();
	}

	/**
	 * Return the temporary directory for the current web application,
	 * as provided by the servlet container.
	 * @return the File representing the temporary directory
	 */
	protected final File getTempDir() {
		return WebUtils.getTempDir(getServletContext());
	}

}
