/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.convert.support;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.Converter;

public abstract class AbstractConverter implements Converter {

	public Object convert(Object source, Class targetClass) throws ConversionException {
		try {
			return doConvert(source, targetClass);
		}
		catch (ConversionException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ConversionException(source, null, e);
		}
	}

	protected abstract Object doConvert(Object source, Class targetClass) throws Exception;
	
}