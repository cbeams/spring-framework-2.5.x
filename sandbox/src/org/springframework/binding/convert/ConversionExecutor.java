/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.convert;

import java.io.Serializable;

import org.springframework.util.closure.Closure;

/**
 * A command object that is parameterized with the information neccessary to
 * perform a conversion of a source input to a target output.
 * @author Keith Donald
 */
public class ConversionExecutor implements Closure, Serializable {

	private Converter converter;

	private Class targetClass;

	/**
	 * Creates a conversion executor.
	 * @param converter The converter that will perform the conversion.
	 * @param targetClass The target type that the converter will convert to.
	 */
	public ConversionExecutor(Converter converter, Class targetClass) {
		this.converter = converter;
		this.targetClass = targetClass;
	}

	/*
	 * Execute the conversion (implements Closure for use as a function object.)
	 */
	public Object call(Object source) {
		return this.converter.convert(source, this.targetClass);
	}

}
