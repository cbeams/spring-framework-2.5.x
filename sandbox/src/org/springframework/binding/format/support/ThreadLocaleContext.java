/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.format.support;

import java.util.Locale;

import org.springframework.binding.format.LocaleContext;

public class ThreadLocaleContext implements LocaleContext {
	private static ThreadLocal localeHolder = new ThreadLocal();

	public Locale getLocale() {
		return (Locale)localeHolder.get();
	}

	public void setLocale(Locale locale) {
		localeHolder.set(locale);
	}

}
