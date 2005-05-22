/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.support;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.binding.MutableAttributeSource;
import org.springframework.core.Styler;
import org.springframework.core.closure.ElementClosureTemplate;
import org.springframework.core.closure.support.IteratorElementTemplate;
import org.springframework.util.Assert;

/**
 * Support class for attribute setters. TODO - should this implement map?
 * @author Keith Donald
 */
public abstract class AttributeSourceSupport implements MutableAttributeSource {

	/**
	 * Get an attribute value and make sure it is of the required type.
	 * @param attributeName name of the attribute to get
	 * @param requiredType the required type of the attribute value
	 * @return the attribute value, or null if not found
	 * @throws IllegalStateException when the value is not of the required type
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
	 * @return the attribute
	 * @throws IllegalStateException
	 */
	public Object getRequiredAttribute(String attributeName) throws IllegalStateException {
		Object value = getAttribute(attributeName);
		if (value == null) {
			throw new IllegalStateException("Required attribute '" + attributeName + "' is not present in this "
					+ getSourceName() + "; attributes present are = " + Styler.call(getAttributeMap()));
		}
		return value;
	}

	protected String getSourceName() {
		return "map";
	}

	/**
	 * @param attributeName
	 * @param clazz
	 * @return the attribute
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
	 * Set the set of attributes.
	 * @param attributes the attributes
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
	 * Assert the the attribute is present in this source.
	 * @param attributeName the attribute name
	 * @param requiredType the expected type
	 * @throws IllegalStateException if not present
	 */
	public void assertAttributePresent(String attributeName, Class requiredType) throws IllegalStateException {
		getRequiredAttribute(attributeName, requiredType);
	}

	/**
	 * Assert the the attribute is present in this source.
	 * @param attributeName the attribute name
	 * @throws IllegalStateException if not present
	 */
	public void assertAttributePresent(String attributeName) throws IllegalStateException {
		getRequiredAttribute(attributeName);
	}

	/**
	 * Returns the names of attributes in this source
	 * @return a collection of attribute names
	 */
	public Set attributeNames() {
		return Collections.unmodifiableSet(getAttributeMap().keySet());
	}

	/**
	 * Returns the collection of attribute values
	 * @return a collection of attribute values
	 */
	public Collection attributeValues() {
		return Collections.unmodifiableCollection(getAttributeMap().values());
	}

	/**
	 * Returns a collection of attribute name=value pairs
	 * @return the attribute entries
	 */
	public Set attributeEntries() {
		return Collections.unmodifiableSet(getAttributeMap().entrySet());
	}

	/**
	 * Returns a template for iterating over elements in this source.
	 * @return the template
	 */
	public ElementClosureTemplate iteratorTemplate() {
		return new IteratorElementTemplate(attributeEntries().iterator());
	}

	/**
	 * Returns the underlying attribute map
	 * @return the attribute map
	 */
	protected abstract Map getAttributeMap();

}