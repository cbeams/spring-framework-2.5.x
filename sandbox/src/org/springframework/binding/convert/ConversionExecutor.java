/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.convert;

import java.io.Serializable;

import org.springframework.util.closure.Closure;

public class ConversionExecutor implements Closure, Serializable {

	private Converter converter;

	private Class targetClass;

	public ConversionExecutor(Converter converter, Class targetClass) {
		this.converter = converter;
		this.targetClass = targetClass;
	}

	public Object call(Object source) {
		return this.converter.convert(source, this.targetClass);
	}

}
