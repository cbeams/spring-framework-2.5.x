/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.web.servlet.view;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.MessageSource;
import org.springframework.web.servlet.support.JstlUtils;

/**
 * Specialization of InternalResourceView for JSTL pages,
 * i.e. JSP pages that use the JSP Standard Tag Library.
 *
 * <p>Exposes JSTL-specific request attributes specifying locale
 * and resource bundle for JSTL's formatting and message tags,
 * using Spring's locale and message source.
 *
 * <p>Typical usage with InternalResourceViewResolver would look as follows,
 * from the perspective of the DispatcherServlet context definition:
 *
 * <pre>
 * &lt;bean id="viewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver"&gt;
 *   &lt;property name="viewClass" value="org.springframework.web.servlet.view.JstlView"/&gt;
 *   &lt;property name="prefix" value="/WEB-INF/jsp/"/&gt;
 *   &lt;property name="suffix" value=".jsp"/&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id="messageSource" class="org.springframework.context.support.ResourceBundleMessageSource"&gt;
 *   &lt;property name="basename" value="messages"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * Every view name returned from a handler will be translated to a JSP
 * resource (for example: "myView" -> "/WEB-INF/jsp/myView.jsp"), using
 * this view class to enable explicit JSTL support.
 *
 * <p>The specified MessageSource loads messages from "messages.properties" etc
 * files in the class path. This will automatically be exposed to views as
 * JSTL localization context, which the JSTL fmt tags (message etc) will use.
 * Consider using Spring's ReloadableResourceBundleMessageSource instead of
 * the standard ResourceBundleMessageSource for more sophistication.
 * Of course, any other Spring components can share the same MessageSource.
 *
 * <p>This is a separate class mainly to avoid JSTL dependencies in
 * InternalResourceView itself. JSTL has not been part of standard
 * J2EE up until J2EE 1.4, so we can't assume the JSTL API jar to be
 * available on the class path.
 *
 * @author Juergen Hoeller
 * @since 27.02.2003
 * @see org.springframework.web.servlet.support.JstlUtils#exposeLocalizationContext
 * @see InternalResourceViewResolver
 * @see org.springframework.context.support.ResourceBundleMessageSource
 * @see org.springframework.context.support.ReloadableResourceBundleMessageSource
 */
public class JstlView extends InternalResourceView {

	private MessageSource jstlAwareMessageSource;


	protected void initApplicationContext() {
		super.initApplicationContext();
		this.jstlAwareMessageSource =
				JstlUtils.getJstlAwareMessageSource(getServletContext(), getApplicationContext());
	}


	protected void exposeHelpers(HttpServletRequest request) throws Exception {
		JstlUtils.exposeLocalizationContext(request, this.jstlAwareMessageSource);
	}

}
