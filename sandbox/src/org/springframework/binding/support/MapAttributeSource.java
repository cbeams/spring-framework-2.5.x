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

import java.util.HashMap;
import java.util.Map;

/**
 * Adapter class to set data in a map using the <code>AttributeSetter</code>
 * interface.
 * @author Keith Donald
 */
public class MapAttributeSource extends AttributeSourceSupport {

	private Map map;

	/**
	 * Create a new map attribute setter adapter.
	 */
	public MapAttributeSource() {
		this.map = new HashMap();
	}

	/**
	 * Create a new map attribute setter adapter.
	 * @param map the map to wrap
	 */
	public MapAttributeSource(Map map) {
		this.map = map;
	}

	public Map getAttributeMap() {
		return map;
	}

	public Object setAttribute(String attributeName, Object attributeValue) {
		return map.put(attributeName, attributeValue);
	}

	public boolean containsAttribute(String attributeName) {
		return map.containsKey(attributeName);
	}

	public Object getAttribute(String attributeName) {
		return map.get(attributeName);
	}

	public Object removeAttribute(String attributeName) {
		return map.remove(attributeName);
	}

	public void clear() {
		map.clear();
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public Object remove(Object key) {
		return map.remove(key);
	}

	public int size() {
		return map.size();
	}
}