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

package org.springframework.web.portlet.context.support;

import java.io.File;

import javax.portlet.PortletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.web.portlet.context.PortletApplicationContext;
import org.springframework.web.portlet.util.PortletUtils;

/**
 * Convenient superclass for application objects running in a PortletApplicationContext.
 * Provides getPortletApplicationContext, getPortletContext, and getTempDir methods.
 * @author Juergen Hoeller
 * @since 28.08.2003
 */
public abstract class PortletApplicationObjectSupport extends ApplicationObjectSupport {

	/**
	 * Return the current application context as PortletApplicationContext.
	 */
	protected final PortletApplicationContext getPortletApplicationContext() {
		ApplicationContext ctx = getApplicationContext();
		if (!(ctx instanceof PortletApplicationContext)) {
			throw new IllegalStateException("PortletApplicationObjectSupport instance [" + this +
																			"] does not run in a PortletApplicationContext but in: " + ctx);
		}
		return (PortletApplicationContext) getApplicationContext();
	}

	/**
	 * Return the current PortletContext.
	 */
	protected final PortletContext getPortletContext() {
		return getPortletApplicationContext().getPortletContext();
	}

	/**
	 * Return the temporary directory for the current web application,
	 * as provided by the servlet container.
	 * @return the File representing the temporary directory
	 */
	protected final File getTempDir() {
		return PortletUtils.getTempDir(getPortletContext());
	}

}
