/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.convert.support;

import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.convert.Converter;
import org.springframework.binding.convert.ConverterLocator;

/**
 * Specialized registry for type converters.
 * @author Keith Donald
 */
public class DefaultConverterLocator implements ConverterLocator {

	private Map sourceClassConverters;

	public DefaultConverterLocator() {

	}

	public void setConverters(Converter[] converters) {
		this.sourceClassConverters = new HashMap(converters.length);
		for (int i = 0; i < converters.length; i++) {
			Class[] sourceClasses = converters[i].getSourceClasses();
			Class[] targetClasses = converters[i].getTargetClasses();
			for (int j = 0; j < sourceClasses.length; j++) {
				Class sourceClass = sourceClasses[j];
				Map sourceMap = (Map)this.sourceClassConverters.get(sourceClass);
				if (sourceMap == null) {
					sourceMap = new HashMap();
					this.sourceClassConverters.put(sourceClass, sourceMap);
				}
				for (int k = 0; k < targetClasses.length; j++) {
					Class targetClass = targetClasses[k];
					sourceMap.put(targetClass, converters[i]);
				}
			}
		}
	}

	public Converter getConverter(Class sourceClass, Class targetClass) {
		if (sourceClassConverters == null) {
			return null;
		}
		Map sourceTargetConverters = (Map)sourceClassConverters.get(sourceClass);
		return (Converter)sourceTargetConverters.get(targetClass);
	}
}