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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.support.AttributeSetterSupport;

/**
 * Holder for data placed in a specific scope, for example "request scope" or
 * "flow scope".
 * @author Keith Donald
 */
public class Scope extends AttributeSetterSupport implements Serializable {

	/**
	 * The scope type; e.g FLOW or REQUEST. 
	 */
	private ScopeType scopeType;

	/**
	 * Create a scope attribute container for the specified scope type
	 * @param scopeType the scope type.
	 */
	public Scope(ScopeType scopeType) {
		this.scopeType = scopeType;
	}

	/**
	 * The data holder map.
	 */
	private Map attributes = new HashMap();

	protected String getMapName() {
		return scopeType.getLabel() + " scope";
	}
	
	public boolean containsAttribute(String attributeName) {
		return this.attributes.containsKey(attributeName);
	}

	public Object getAttribute(String attributeName) {
		return this.attributes.get(attributeName);
	}

	public Map getAttributeMap() {
		return Collections.unmodifiableMap(this.attributes);
	}

	public Object setAttribute(String attributeName, Object attributeValue) {
		return this.attributes.put(attributeName, attributeValue);
	}

	public Object removeAttribute(String attributeName) {
		return this.attributes.remove(attributeName);
	}

	// map operations
	public int size() {
		return this.attributes.size();
	}

	public void clear() {
		this.attributes.clear();
	}

	public boolean containsValue(Object value) {
		return this.attributes.containsValue(value);
	}

	public boolean isEmpty() {
		return this.attributes.isEmpty();
	}

	public Object remove(Object key) {
		return removeAttribute(String.valueOf(key));
	}
}