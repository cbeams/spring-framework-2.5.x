/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.convert;

/**
 * A service locator that provides access to a registry of type converters.
 * @author Keith Donald
 */
public interface ConversionService {
	public ConversionExecutor getConversionExecutor(Class sourceClass, Class targetClass) throws ConversionException;
}