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

package org.springframework.autobuilds.integration.util;

/**
 * Subclasses Spring class of same name to use our own {@link ContextLoader}.<br />
 * 
 * <p>
 * This listener should be registered after Log4jConfigListener in web.xml, if
 * the latter is used.
 * 
 * <p>
 * For Servlet 2.2 containers and Servlet 2.3 ones that do not initalize
 * listeners before servlets, use ContextLoaderServlet. See the latter's
 * javadoc for details.
 * 
 * @author colin samapaleanu
 * @see ContextLoader
 */
public class ContextLoaderListener
		extends
			org.springframework.web.context.ContextLoaderListener {

	/**
	 * Create the ContextLoader to use. Can be overridden in subclasses.
	 * 
	 * @return the new ContextLoader
	 */
	protected org.springframework.web.context.ContextLoader createContextLoader() {
		return new ContextLoader();
	}

}