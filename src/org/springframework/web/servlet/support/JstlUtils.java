/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.web.servlet.support;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceResourceBundle;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Helper class for preparing JSTL views,
 * in particular for exposing a JSTL localization context.
 *
 * @author Juergen Hoeller
 * @since 20.08.2003
 */
public abstract class JstlUtils {

	public static final String REQUEST_SCOPE_SUFFIX = ".request";


	/**
	 * Checks JSTL's "javax.servlet.jsp.jstl.fmt.localizationContext"
	 * context-param and creates a corresponding child message source,
	 * with the provided Spring-defined MessageSource as parent.
	 * @param servletContext the ServletContext we're running in
	 * (to check JSTL-related context-params in web.xml)
	 * @param messageSource the MessageSource to expose, typically
	 * the ApplicationContext of the current DispatcherServlet
	 * @return the MessageSource to expose to JSTL; first checking the
	 * JSTL-defined bundle, then the Spring-defined MessageSource
	 * @see org.springframework.context.ApplicationContext
	 */
	public static MessageSource getJstlAwareMessageSource(
			ServletContext servletContext, MessageSource messageSource) {

		if (servletContext != null) {
			String jstlInitParam = servletContext.getInitParameter(Config.FMT_LOCALIZATION_CONTEXT);
			if (jstlInitParam != null) {
				// Create a ResourceBundleMessageSource for the specified resource bundle
				// basename in the JSTL context-param in web.xml, wiring it with the given
				// Spring-defined MessageSource as parent.
				ResourceBundleMessageSource jstlBundleWrapper = new ResourceBundleMessageSource();
				jstlBundleWrapper.setBasename(jstlInitParam);
				jstlBundleWrapper.setParentMessageSource(messageSource);
				return jstlBundleWrapper;
			}
		}
		return messageSource;
	}

	/**
	 * Exposes JSTL-specific request attributes specifying locale
	 * and resource bundle for JSTL's formatting and message tags,
	 * using Spring's locale and MessageSource.
	 * @param request the current HTTP request
	 * @param messageSource the MessageSource to expose,
	 * typically the current ApplicationContext (may be <code>null</code>)
	 */
	public static void exposeLocalizationContext(
			HttpServletRequest request, MessageSource messageSource) {

		// Add JSTL locale and LocalizationContext request attributes.
		Locale jstlLocale = RequestContextUtils.getLocale(request);

		// for JSTL implementations that stick to the config names (e.g. Resin's)
		request.setAttribute(Config.FMT_LOCALE, jstlLocale);
		// for JSTL implementations that append the scope to the config names (e.g. Jakarta's)
		request.setAttribute(Config.FMT_LOCALE + REQUEST_SCOPE_SUFFIX, jstlLocale);

		if (messageSource != null) {
			// We've got a Spring MessageSource - expose it as ResourceBundle.
			ResourceBundle bundle = new MessageSourceResourceBundle(messageSource, jstlLocale);
			LocalizationContext jstlContext = new LocalizationContext(bundle, jstlLocale);

			// for JSTL implementations that stick to the config names (e.g. Resin's)
			request.setAttribute(Config.FMT_LOCALIZATION_CONTEXT, jstlContext);
			// for JSTL implementations that append the scope to the config names (e.g. Jakarta's)
			request.setAttribute(Config.FMT_LOCALIZATION_CONTEXT + REQUEST_SCOPE_SUFFIX, jstlContext);
		}
	}

}
