/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding;

import java.util.Map;

import org.springframework.beans.PropertyAccessException;

/**
 * EXPERIMENTAL - not yet fit for general use
 */
public interface PropertyAccessor {
	public Map getPropertyValues();

	public Object getPropertyValue(String propertyPath) throws PropertyAccessException;
}