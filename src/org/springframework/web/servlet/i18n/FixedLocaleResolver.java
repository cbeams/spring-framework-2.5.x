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

package org.springframework.web.servlet.i18n;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.LocaleResolver;

/**
 * Implementation of LocaleResolver that always returns a fixed default Locale.
 *
 * <p>Note: Does not support setLocale, because the fixed Locale cannot be changed.
 *
 * @author Juergen Hoeller
 * @since 16.06.2004
 */
public class FixedLocaleResolver implements LocaleResolver {

	private Locale defaultLocale;

	/**
	 * Set the fixed Locale that this resolver will return.
	 */
	public void setDefaultLocale(Locale defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

	public Locale resolveLocale(HttpServletRequest request) {
		return this.defaultLocale;
	}

	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		throw new IllegalArgumentException("Cannot change fixed locale - use a different locale resolution strategy");
	}

}
