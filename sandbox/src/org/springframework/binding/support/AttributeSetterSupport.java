/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.support;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.binding.AttributeSetter;
import org.springframework.util.Assert;
import org.springframework.util.Styler;
import org.springframework.util.closure.ProcessTemplate;
import org.springframework.util.closure.support.IteratorProcessTemplate;

/**
 * Support class for attribute setters.
 * TODO - should this implement map?
 * @author Keith Donald
 */
public abstract class AttributeSetterSupport implements AttributeSetter, Map {

	/**
	 * @param attributeName
	 * @param requiredType
	 * @return
	 * @throws IllegalStateException
	 */
	public Object getAttribute(String attributeName, Class requiredType) throws IllegalStateException {
		Object value = getAttribute(attributeName);
		if (requiredType != null && value != null) {
			Assert.isInstanceOf(requiredType, value);
		}
		return value;
	}

	/**
	 * @param attributeName
	 * @return
	 * @throws IllegalStateException
	 */
	public Object getRequiredAttribute(String attributeName) throws IllegalStateException {
		Object value = getAttribute(attributeName);
		if (value == null) {
			throw new IllegalStateException("Required attribute '" + attributeName
					+ "' is not present in this '" + getMapName() + "'; attributes present are = " + Styler.call(getAttributeMap()));
		}
		return value;
	}
	
	protected String getMapName() {
		return "map";
	}

	/**
	 * @param attributeName
	 * @param clazz
	 * @return
	 * @throws IllegalStateException
	 */
	public Object getRequiredAttribute(String attributeName, Class clazz) throws IllegalStateException {
		Object value = getRequiredAttribute(attributeName);
		if (clazz != null) {
			Assert.isInstanceOf(clazz, value);
		}
		return value;
	}

	/**
	 * @param attributes
	 */
	public void setAttributes(Map attributes) {
		Iterator it = attributes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			Assert.isInstanceOf(String.class, entry.getKey());
			setAttribute((String)entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * @param attributeName
	 * @param requiredType
	 * @throws IllegalStateException
	 */
	public void assertAttributePresent(String attributeName, Class requiredType) throws IllegalStateException {
		getRequiredAttribute(attributeName, requiredType);
	}

	/**
	 * @param attributeName
	 * @throws IllegalStateException
	 */
	public void assertAttributePresent(String attributeName) throws IllegalStateException {
		getRequiredAttribute(attributeName);
	}

	/**
	 * @return
	 */
	public Set attributeNames() {
		return Collections.unmodifiableSet(getAttributeMap().keySet());
	}

	/**
	 * @return
	 */
	public Collection attributeValues() {
		return Collections.unmodifiableCollection(getAttributeMap().values());
	}

	/**
	 * @return
	 */
	public Set attributeEntries() {
		return Collections.unmodifiableSet(getAttributeMap().entrySet());
	}

	/**
	 * @return
	 */
	public ProcessTemplate iteratorTemplate() {
		return new IteratorProcessTemplate(attributeEntries().iterator());
	}

	/**
	 * @return
	 */
	public abstract Map getAttributeMap();

	// map operations
	public boolean containsKey(Object key) {
		return containsAttribute(String.valueOf(key));
	}

	public Set entrySet() {
		return attributeEntries();
	}

	public Object get(Object key) {
		return getAttribute(String.valueOf(key));
	}

	public Set keySet() {
		return attributeNames();
	}

	public Object put(Object key, Object value) {
		return setAttribute(String.valueOf(key), value);
	}

	public void putAll(Map attributes) {
		setAttributes(attributes);
	}

	public Collection values() {
		return attributeValues();
	}
}