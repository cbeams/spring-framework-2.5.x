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
package org.springframework.web.flow.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter class to set data in a map using the <code>AttributeSetter</code>
 * interface.
 * @author Keith Donald
 */
public class MapAttributeSetterAdapter extends AttributeSetterSupport {

	private Map map;

	public MapAttributeSetterAdapter() {

	}

	/**
	 * Create a new map attribute setter adapter.
	 * @param map the map to wrap
	 */
	public MapAttributeSetterAdapter(Map map) {
		this.map = map;
	}

	public Map getAttributeMap() {
		if (map != null) {
			return map;
		}
		else {
			return Collections.EMPTY_MAP;
		}
	}

	public Object setAttribute(String attributeName, Object attributeValue) {
		if (map == null) {
			map = new HashMap();
		}
		return map.put(attributeName, attributeValue);
	}

	public boolean containsAttribute(String attributeName) {
		if (map == null) {
			return false;
		}
		return map.containsKey(attributeName);
	}

	public Object getAttribute(String attributeName) {
		if (map == null) {
			return null;
		}
		return map.get(attributeName);
	}

	public Object removeAttribute(String attributeName) {
		if (map == null) {
			return null;
		}
		return map.remove(attributeName);
	}
}