/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding;

import org.springframework.binding.format.Formatter;

/**
 * A service locator that process access to a registry storing shared type
 * converters and value formatters.
 * @author Keith Donald
 */
public interface TypeConverterRegistry {
	public Formatter getFormatter(String name);

	public Formatter getFormatter(Class clazz);

	public TypeConverter getTypeConverter(String name);

	public TypeConverter getTypeConverter(Class clazz);
}