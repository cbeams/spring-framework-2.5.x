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

package org.springframework.context.i18n;

import java.util.Locale;

/**
 * Simple holder class that associates a LocaleContext instance
 * with the current thread. The LocaleContext will be inherited
 * by any child threads spawned by the current thread.
 *
 * <p>Used as a central holder for the current Locale in Spring,
 * wherever necessary: for example, in MessageSourceAccessor.
 * DispatcherServlet automatically exposes its current Locale here.
 * Other applications can expose theirs too, to make classes like
 * MessageSourceAccessor automatically use that Locale.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see LocaleContext
 * @see org.springframework.context.support.MessageSourceAccessor
 * @see org.springframework.web.servlet.DispatcherServlet
 */
public abstract class LocaleContextHolder {

	private static ThreadLocal localeContextHolder = new InheritableThreadLocal();

	/**
	 * Associate the given LocaleContext with the current thread.
	 * @param localeContext the current LocaleContext, or <code>null</code> to reset
	 * the thread-bound context
	 */
	public static void setLocaleContext(LocaleContext localeContext) {
		localeContextHolder.set(localeContext);
	}

	/**
	 * Return the LocaleContext associated with the current thread,
	 * if any.
	 * @return the current LocaleContext, or <code>null</code> if none
	 */
	public static LocaleContext getLocaleContext() {
		return (LocaleContext) localeContextHolder.get();
	}

	/**
	 * Associate the given Locale with the current thread.
	 * Will implicitly create a LocaleContext for the given Locale.
	 * @param locale the current Locale, or <code>null</code> to reset
	 * the thread-bound context
	 * @see SimpleLocaleContext#SimpleLocaleContext(java.util.Locale)
	 */
	public static void setLocale(Locale locale) {
		localeContextHolder.set(locale != null ? new SimpleLocaleContext(locale) : null);
	}

	/**
	 * Return the Locale associated with the current thread, if any,
	 * or the system default Locale else.
	 * @return the current Locale, or the system default Locale if no
	 * specific Locale has been associated with the current thread
	 * @see LocaleContext#getLocale()
	 * @see java.util.Locale#getDefault()
	 */
	public static Locale getLocale() {
		LocaleContext localeContext = (LocaleContext) localeContextHolder.get();
		return (localeContext != null ? localeContext.getLocale() : Locale.getDefault());
	}

}
