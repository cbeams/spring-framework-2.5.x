/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding;

public interface TypeConverterRegistry {
	public Formatter getFormatter(String name);

	public Formatter getFormatter(Class clazz);

	public TypeConverter getTypeConverter(String name);

	public TypeConverter getTypeConverter(Class clazz);
}