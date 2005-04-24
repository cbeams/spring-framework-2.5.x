package org.springframework.binding;

import org.springframework.core.NestedRuntimeException;

public class NoSuchAttributeValueException extends NestedRuntimeException {
	public NoSuchAttributeValueException(String placeholder) {
		super("No such attribute value to fill placeholder '" + placeholder + "'");
	}
}
