/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.web.servlet.i18n;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.web.servlet.LocaleResolver;

/**
 * LocaleContext adapter that delegates to a LocaleResolver,
 * calling its <code>resolveLocale</code> method with a given request.
 *
 * <p>Used by DispatcherServlet, to expose the current LocaleResolver's
 * behavior through the global LocaleContextHolder: for example,
 * for MessageSourceAccessor.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see org.springframework.web.servlet.DispatcherServlet
 * @see org.springframework.context.i18n.LocaleContextHolder
 * @see org.springframework.context.support.MessageSourceAccessor
 */
public class LocaleResolverLocaleContext implements LocaleContext {

	private final LocaleResolver localeResolver;

	private final HttpServletRequest request;

	/**
	 * Create a LocaleResolverLocaleContext for the given LocaleResolver
	 * and request.
	 * @param localeResolver the current LocaleResolver
	 * @param request the current HTTP request
	 */
	public LocaleResolverLocaleContext(LocaleResolver localeResolver, HttpServletRequest request) {
		this.localeResolver = localeResolver;
		this.request = request;
	}

	/**
	 * Delegate to the LocaleResolver to resolve the Locale
	 * for the current request.
	 */
	public Locale getLocale() {
		return this.localeResolver.resolveLocale(this.request);
	}

}
