/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.convert.support;

import org.springframework.binding.convert.ConversionException;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public class TextToClassConverter extends AbstractConverter {

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { Class.class };
	}

	protected Object doConvert(Object source, Class targetClass) throws Exception {
		String text = (String)source;
		if (StringUtils.hasText(text)) {
			try {
				return ClassUtils.forName(text.trim());
			}
			catch (ClassNotFoundException ex) {
				throw new ConversionException(source, Class.class, ex);
			}
		}
		else {
			return null;
		}
	}
}