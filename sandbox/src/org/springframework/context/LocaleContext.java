/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.context;

import java.util.Locale;

public interface LocaleContext {
	public Locale getLocale();

	public void setLocale(Locale locale);
}
