/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.convert;

/**
 * A service interface for retrieving type conversion executors. The returned
 * command object is thread-safe.
 * @author Keith Donald
 */
public interface ConversionService {
	public ConversionExecutor getConversionExecutor(Class sourceClass, Class targetClass) throws ConversionException;
}