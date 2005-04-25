/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.binding.support;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.ToStringCreator;
import org.springframework.util.Assert;

/**
 * Adapter class to set data in a map using the <code>AttributeSetter</code>
 * interface.
 * @author Keith Donald
 */
public class MapAttributeSource extends AttributeSourceSupport implements Serializable {

	private Map map;

	/**
	 * Create a new map attribute setter adapter.
	 */
	public MapAttributeSource() {
		this.map = new HashMap();
	}

	/**
	 * Create a new map attribute setter adapter.
	 */
	public MapAttributeSource(int size) {
		this.map = new HashMap(size);
	}

	/**
	 * Create a new map attribute setter adapter.
	 * @param map the map to wrap
	 */
	public MapAttributeSource(Map map) {
		Assert.notNull(map, "The map is required");
		this.map = map;
	}

	public boolean containsAttribute(String attributeName) {
		return map.containsKey(attributeName);
	}

	public Object getAttribute(String attributeName) {
		return map.get(attributeName);
	}

	public Object setAttribute(String attributeName, Object attributeValue) {
		return map.put(attributeName, attributeValue);
	}

	public Object removeAttribute(String attributeName) {
		return map.remove(attributeName);
	}

	public Map getAttributeMap() {
		return map;
	}

	public String toString() {
		return new ToStringCreator(this).append("map", map).toString();
	}
}