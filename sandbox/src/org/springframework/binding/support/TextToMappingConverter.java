/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.support;

import org.springframework.beans.propertyeditors.ClassEditor;
import org.springframework.binding.convert.ConverterLocator;
import org.springframework.binding.convert.support.AbstractConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class TextToMappingConverter extends AbstractConverter {

	private ConverterLocator converterLocator;

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
		ClassEditor classEditor = new ClassEditor();
		String[] encodedMapping = StringUtils.commaDelimitedListToStringArray((String)o);
		if (encodedMapping.length == 2) {
			classEditor.setAsText(encodedMapping[1]);
			Class clazz = (Class)classEditor.getValue();
			return new Mapping(encodedMapping[0], getConverterLocator().getConverter(String.class, clazz));
		}
		else {
			return new Mapping(encodedMapping[0]);
		}
	}
}