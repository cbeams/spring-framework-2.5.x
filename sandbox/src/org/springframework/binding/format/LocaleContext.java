/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.format;

import java.util.Locale;

public interface LocaleContext {
	public Locale getLocale();

	public void setLocale(Locale locale);
}
