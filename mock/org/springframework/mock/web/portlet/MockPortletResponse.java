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

package org.springframework.mock.web.portlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.portlet.PortletResponse;

import org.springframework.util.Assert;

/**
 * Mock implementation of the PortletResponse interface.
 *
 * @author John A. Lewis
 * @since 2.0
 */
public class MockPortletResponse implements PortletResponse {

	private final Hashtable properties = new Hashtable();

	
	//---------------------------------------------------------------------
	// PortletResponse methods
	//---------------------------------------------------------------------

	public void addProperty(String key, String value) {
		Assert.notNull(key, "key must not be null");
		Object oldValue = this.properties.get(key);
		if (oldValue instanceof String) {
			ArrayList list = new ArrayList();
			list.add(oldValue);
			list.add(value);
			this.properties.put(key, list);
		}
		else if (oldValue instanceof List) {
			((List) oldValue).add(value);
		}
		else {
			this.properties.put(key, value);
		}
	}

	public void setProperty(String key, String value) {
		Assert.notNull(key, "key must not be null");
		this.properties.put(key, value);
	}

	public String encodeURL(String path) {
		return path;
	}

	
	//---------------------------------------------------------------------
	// MockPortletResponse methods
	//---------------------------------------------------------------------

	public String getProperty(String key) {
		Assert.notNull(key, "key must not be null");
		Object value = this.properties.get(key);
		if (value instanceof List) {
			List list = (List) value;
			if (list.size() < 1) {
				return null;
			}
			Object element = list.get(0);
			return (element != null ? element.toString() : null);
		}
		return (value != null ? value.toString() : null);
	}

	public Enumeration getProperties(String key) {
		Assert.notNull(key, "key must not be null");
		Object value = this.properties.get(key);
		if (value instanceof List) {
			return Collections.enumeration((List) value);
		}
		else if (value != null) {
			Vector vector = new Vector(1);
			vector.add(value.toString());
			return vector.elements();
		}
		else {
			return Collections.enumeration(Collections.EMPTY_SET);
		}
	}

	public Enumeration getPropertyNames() {
		return this.properties.keys();
	}

}
