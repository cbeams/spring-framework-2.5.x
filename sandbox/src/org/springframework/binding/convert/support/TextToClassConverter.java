/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.convert.support;

import org.springframework.binding.convert.ConversionException;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public class TextToClassConverter extends AbstractFormattingConverter {

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { Class.class };
	}

	protected Object doConvert(Object o) throws Exception {
		String text = (String)o;
		if (StringUtils.hasText(text)) {
			try {
				return ClassUtils.forName(text.trim());
			}
			catch (ClassNotFoundException ex) {
				throw new ConversionException(o, Class.class, ex);
			}
		} else {
			return null;
		}
	}
}
