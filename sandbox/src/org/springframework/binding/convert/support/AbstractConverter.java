/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.convert.support;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.Converter;
import org.springframework.util.closure.Closure;

public abstract class AbstractConverter implements Converter, Closure {

	public Object convert(Object o) throws ConversionException {
		try {
			return doConvert(o);
		}
		catch (ConversionException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ConversionException(o, null, e);
		}
	}

	protected abstract Object doConvert(Object o) throws Exception;

	public Object call(Object o) {
		return call(o);
	}
	
}