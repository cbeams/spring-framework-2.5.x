/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.format;

import org.springframework.enums.ShortCodedEnum;

public class Style extends ShortCodedEnum {
	private Style(int code, String label) {
		super(code, label);
	}
}
