/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.support;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.springframework.binding.AttributeSetter;
import org.springframework.util.Assert;
import org.springframework.util.Styler;
import org.springframework.util.closure.ProcessTemplate;
import org.springframework.util.closure.support.IteratorProcessTemplate;

public abstract class AttributeSetterSupport implements AttributeSetter {

	public Object getAttribute(String attributeName, Class requiredType) throws IllegalStateException {
		Object value = getAttribute(attributeName);
		if (requiredType != null && value != null) {
			Assert.isInstanceOf(requiredType, value);
		}
		return value;
	}

	public Object getRequiredAttribute(String attributeName) throws IllegalStateException {
		Object value = getAttribute(attributeName);
		if (value == null) {
			throw new IllegalStateException("Required attribute '" + attributeName
					+ "' is not present; attributes present are = " + Styler.call(getAttributeMap()));
		}
		return value;
	}

	public Object getRequiredAttribute(String attributeName, Class clazz) throws IllegalStateException {
		Object value = getRequiredAttribute(attributeName);
		if (clazz != null) {
			Assert.isInstanceOf(clazz, value);
		}
		return value;
	}

	public void setAttributes(Map attributes) {
		Iterator it = attributes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			Assert.isInstanceOf(String.class, entry.getKey());
			setAttribute((String)entry.getKey(), entry.getValue());
		}
	}
	public void assertAttributePresent(String attributeName, Class requiredType) throws IllegalStateException {
		getRequiredAttribute(attributeName, requiredType);
	}

	public void assertAttributePresent(String attributeName) throws IllegalStateException {
		getRequiredAttribute(attributeName);
	}

	public Collection attributeNames() {
		return Collections.unmodifiableCollection(getAttributeMap().keySet());
	}

	public Collection attributeValues() {
		return Collections.unmodifiableCollection(getAttributeMap().values());
	}

	public Collection attributeEntries() {
		return Collections.unmodifiableCollection(getAttributeMap().entrySet());
	}

	public ProcessTemplate iteratorTemplate() {
		return new IteratorProcessTemplate(attributeEntries().iterator());
	}

	public abstract Map getAttributeMap();
}
