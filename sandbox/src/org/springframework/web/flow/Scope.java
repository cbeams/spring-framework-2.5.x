/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.web.flow;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.binding.MutableAttributeSource;
import org.springframework.core.Styler;
import org.springframework.util.Assert;

/**
 * Holder for data placed in a specific scope, for example "request scope"
 * or "flow scope".
 * 
 * @see org.springframework.web.flow.ScopeType
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class Scope implements MutableAttributeSource, Map, Serializable {

	/**
	 * The scope type; e.g FLOW or REQUEST.
	 */
	private ScopeType scopeType;

	/**
	 * The data holder map.
	 */
	private Map attributes = new HashMap();

	/**
	 * Create a scope attribute container for the specified scope type.
	 * @param scopeType the scope type
	 */
	public Scope(ScopeType scopeType) {
		this.scopeType = scopeType;
	}

	// implementing AttributeAccessor

	public boolean containsAttribute(String attributeName) {
		return this.attributes.containsKey(attributeName);
	}

	public Object getAttribute(String attributeName) {
		return this.attributes.get(attributeName);
	}

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
	 * Get the value of a required attribute.
	 * @param attributeName name of the attribute to get
	 * @return the attribute value
	 * @throws IllegalStateException when the attribute is not found
	 */
	public Object getRequiredAttribute(String attributeName) throws IllegalStateException {
		Object value = getAttribute(attributeName);
		if (value == null) {
			throw new IllegalStateException("Required attribute '" + attributeName + "' is not present in " + this
					+ "; attributes present are = " + Styler.call(getAttributeMap()));
		}
		return value;
	}

	/**
	 * Get the value of a required attribute and make sure it is of the required type.
	 * @param attributeName name of the attribute to get
	 * @param requiredType the required type of the attribute value
	 * @return the attribute value
	 * @throws IllegalStateException when the attribute is not found or not of the
	 *         required type
	 */
	public Object getRequiredAttribute(String attributeName, Class requiredType) throws IllegalStateException {
		Object value = getRequiredAttribute(attributeName);
		if (requiredType != null) {
			Assert.isInstanceOf(requiredType, value);
		}
		return value;
	}

	/**
	 * Gets the value of the specified <code>attributeName</code>, if such an
	 * attribute exists in this scope. If the attribute does not exist, a new
	 * instance will be created of the type <code>attributeClass</code>, which
	 * will be set in this scope and returned.
	 * @param attributeName the attribute name
	 * @param attributeClass the attribute class
	 * @return the value
	 * @throws IllegalStateException when the attribute is not of the required type
	 * @throws BeansException if the attribute could not be created
	 */
	public Object getOrCreateAttribute(String attributeName, Class attributeClass) throws IllegalStateException,
			BeansException {
		if (!containsAttribute(attributeName)) {
			setAttribute(attributeName, BeanUtils.instantiateClass(attributeClass));
		}
		return getAttribute(attributeName, attributeClass);
	}

	/**
	 * Returns the contents of this scope as a map.
	 */
	public Map getAttributeMap() {
		return Collections.unmodifiableMap(this.attributes);
	}

	// implementing AttributeSetter

	public Object setAttribute(String attributeName, Object attributeValue) {
		return this.attributes.put(attributeName, attributeValue);
	}

	/**
	 * Set all given attributes in this scope.
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
	 * Remove an attribute from this scope.
	 * @param attributeName the name of the attribute to remove
	 * @return previous value associated with specified attribute name,
	 *         or <tt>null</tt> if there was no mapping for the name
	 */
	public Object removeAttribute(String attributeName) {
		return this.attributes.remove(attributeName);
	}

	// implementing Map

	public int size() {
		return this.attributes.size();
	}

	public boolean isEmpty() {
		return this.attributes.isEmpty();
	}

	public boolean containsKey(Object key) {
		return this.attributes.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return this.attributes.containsValue(value);
	}

	public Object get(Object key) {
		return this.attributes.get(key);
	}

	public Object put(Object key, Object value) {
		return this.attributes.put(key, value);
	}

	public Object remove(Object key) {
		return removeAttribute(String.valueOf(key));
	}

	public void putAll(Map t) {
		this.attributes.putAll(t);
	}

	public void clear() {
		this.attributes.clear();
	}

	public Set keySet() {
		return this.attributes.keySet();
	}

	public Collection values() {
		return this.attributes.values();
	}

	public Set entrySet() {
		return this.attributes.entrySet();
	}

	public String toString() {
		return scopeType.getLabel() + " scope";
	}
}