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

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;


/**
 * <code>ServletContextListener</code> that loads an <code>XMLCContext</code> and
 * binds it the <code>ServletContext</code> on initialization.
 * @author Rob Harrop
 * @see org.enhydra.xml.xmlc.servlet.XMLCContext
 * @see XMLCContextLoader
 */
public class XMLCContextLoaderListener implements ServletContextListener{

	public void contextInitialized(ServletContextEvent event) {
		XMLCContextLoader.loadContext(event.getServletContext());
	}

	public void contextDestroyed(ServletContextEvent event) {
		// no-op
	}
}
