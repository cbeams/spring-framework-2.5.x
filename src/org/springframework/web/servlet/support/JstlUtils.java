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

package org.springframework.web.servlet.support;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceResourceBundle;

/**
 * Helper class for preparing JSTL views.
 * @author Juergen Hoeller
 * @since 20.08.2003
 */
public abstract class JstlUtils {

	public static final String REQUEST_SCOPE_SUFFIX = ".request";

	/**
	 * Exposes JSTL-specific request attributes specifying locale
	 * and resource bundle for JSTL's formatting and message tags,
	 * using Spring's locale and message source.
	 * @param request current HTTP request
	 * @param messageSource the MessageSource to expose,
	 * typically the current application context
	 */
	public static void exposeLocalizationContext(HttpServletRequest request, MessageSource messageSource) {

		// add JSTL locale and LocalizationContext request attributes
		Locale jstlLocale = RequestContextUtils.getLocale(request);
		ResourceBundle bundle = new MessageSourceResourceBundle(messageSource, jstlLocale);
		LocalizationContext jstlContext = new LocalizationContext(bundle, jstlLocale);

		// for JSTL implementations that stick to the config names (e.g. Resin's)
		request.setAttribute(Config.FMT_LOCALIZATION_CONTEXT, jstlContext);
		request.setAttribute(Config.FMT_LOCALE, jstlLocale);

		// for JSTL implementations that append the scope to the config names (e.g. Jakarta's)
		request.setAttribute(Config.FMT_LOCALIZATION_CONTEXT + REQUEST_SCOPE_SUFFIX, jstlContext);
		request.setAttribute(Config.FMT_LOCALE + REQUEST_SCOPE_SUFFIX, jstlLocale);
	}

}
