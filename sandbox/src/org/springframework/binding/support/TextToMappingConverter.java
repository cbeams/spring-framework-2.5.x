/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.support;

import org.springframework.binding.convert.ConverterLocator;
import org.springframework.binding.convert.support.AbstractConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class TextToMappingConverter extends AbstractConverter {

	private ConverterLocator converterLocator;

	public TextToMappingConverter(ConverterLocator locator) {
		setConverterLocator(locator);
	}

	/**
	 * Set the type converter registry
	 * @param registry the registry
	 */
	public void setConverterLocator(ConverterLocator locator) {
		this.converterLocator = locator;
	}

	protected ConverterLocator getConverterLocator() {
		Assert.notNull(this.converterLocator, "The converterLocator property was request but is not set");
		return this.converterLocator;
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { Mapping.class };
	}

	protected Object doConvert(Object o) throws Exception {
		String[] mappingInfo = StringUtils.commaDelimitedListToStringArray((String)o);
		String[] sourceTarget = StringUtils.delimitedListToStringArray(mappingInfo[0], "->");
		if (mappingInfo.length == 2) {
			Class clazz = (Class)getConverterLocator().getConverter(String.class, Class.class).convert(mappingInfo[1]);
			if (sourceTarget.length == 2) {
				return new Mapping(sourceTarget[0], sourceTarget[1], getConverterLocator().getConverter(String.class,
						clazz));
			}
			else {
				return new Mapping(sourceTarget[0], getConverterLocator().getConverter(String.class, clazz));
			}
		}
		else {
			if (sourceTarget.length == 2) {
				return new Mapping(sourceTarget[0], sourceTarget[1]);
			}
			else {
				return new Mapping(sourceTarget[0]);
			}
		}
	}
}