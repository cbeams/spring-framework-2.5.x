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
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.LocaleResolver;

/**
 * Implementation of LocaleResolver that always returns a fixed default Locale.
 *
 * <p>Note: Does not support <code>setLocale</code>, as the fixed Locale
 * cannot be changed.
 *
 * @author Juergen Hoeller
 * @since 1.1
 */
public class FixedLocaleResolver implements LocaleResolver {

	private Locale defaultLocale = Locale.getDefault();


	/**
	 * Set the fixed Locale that this resolver will return.
	 * Default is the current JVM's default Locale.
	 * @see java.util.Locale#getDefault()
	 */
	public void setDefaultLocale(Locale defaultLocale) {
		this.defaultLocale = (defaultLocale != null ? defaultLocale : Locale.getDefault());
	}


	public Locale resolveLocale(HttpServletRequest request) {
		return this.defaultLocale;
	}

	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		throw new UnsupportedOperationException(
				"Cannot change fixed locale - use a different locale resolution strategy");
	}

}
