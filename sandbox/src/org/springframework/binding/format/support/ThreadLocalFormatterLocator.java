/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.binding.format.support;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.format.Formatter;
import org.springframework.binding.format.Style;
import org.springframework.binding.thread.ThreadCleanupBroadcaster;
import org.springframework.binding.thread.support.DefaultThreadLocalContext;

/**
 * FormatterLocator that caches Formatters in thread-local storage.
 * @author Keith Donald
 */
public class ThreadLocalFormatterLocator extends AbstractFormatterLocator {

	private static final String DEFAULT_DATE_FORMATTER_KEY = "dateFormatter";

	private static final String DEFAULT_DATE_TIME_FORMATTER_KEY = "dateTimeFormatter";

	private static final String DEFAULT_TIME_FORMATTER_KEY = "timeFormatter";

	private DefaultThreadLocalContext formatterStorage = new DefaultThreadLocalContext();

	public ThreadLocalFormatterLocator() {
		
	}
	
	public ThreadLocalFormatterLocator(ThreadCleanupBroadcaster broadcaster) {
		setCleanupBroadcaster(broadcaster);
	}
	
	protected void setCleanupBroadcaster(ThreadCleanupBroadcaster broadcaster) {
		formatterStorage.setCleanupBroadcaster(broadcaster);
	}

	protected Map getLocaleMap() {
		Map map = (Map)formatterStorage.get(getLocale());
		if (map == null) {
			map = new HashMap();
			formatterStorage.put(getLocale(), map);
		}
		return map;
	}

	public Formatter getDateFormatter(Style style) {
		String key = DEFAULT_DATE_FORMATTER_KEY + style.getCode();
		DateFormatter formatter = (DateFormatter)getLocaleMap().get(key);
		if (formatter == null) {
			formatter = new DateFormatter(SimpleDateFormat.getDateInstance(style.getShortCode(), getLocale()));
			getLocaleMap().put(key, formatter);
		}
		return formatter;
	}

	public Formatter getDateTimeFormatter(Style dateStyle, Style timeStyle) {
		String key = DEFAULT_DATE_TIME_FORMATTER_KEY + dateStyle.getCode() + timeStyle.getCode();
		DateFormatter formatter = (DateFormatter)getLocaleMap().get(key);
		if (formatter == null) {
			formatter = new DateFormatter(SimpleDateFormat.getDateTimeInstance(dateStyle.getShortCode(), timeStyle
					.getShortCode(), getLocale()));
			getLocaleMap().put(key, formatter);
		}
		return formatter;
	}

	public Formatter getTimeFormatter(Style style) {
		String key = DEFAULT_TIME_FORMATTER_KEY + style.getCode();
		DateFormatter formatter = (DateFormatter)getLocaleMap().get(key);
		if (formatter == null) {
			formatter = new DateFormatter(SimpleDateFormat.getTimeInstance(style.getShortCode(), getLocale()));
			getLocaleMap().put(key, formatter);
		}
		return formatter;
	}

	public Formatter getNumberFormatter(Class numberClass) {
		NumberFormatter formatter = (NumberFormatter)getLocaleMap().get(numberClass);
		if (formatter == null) {
			formatter = new NumberFormatter(numberClass, NumberFormat.getNumberInstance(getLocale()));
			getLocaleMap().put(numberClass, formatter);
		}
		return formatter;
	}
}