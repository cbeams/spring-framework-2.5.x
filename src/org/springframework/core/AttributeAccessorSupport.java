/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.core;

import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Support class for {@link AttributeAccessor AttributeAccessors}, providing a
 * base implementation of all methods.
 * 
 * <p>{@link Serializable} if sub-classes and all attribute values are {@link Serializable}.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public abstract class AttributeAccessorSupport implements AttributeAccessor, Serializable {

	/**
	 * Attributes {@link Map}. Attributes are {@link String}/{@link Object} pairs.
	 */
	private final Map attributes = new HashMap();


	public void setAttribute(String name, Object value) {
		Assert.notNull(name, "'name' cannot be null.");
		if (value != null) {
			this.attributes.put(name, value);
		}
		else {
			removeAttribute(name);
		}
	}

	public Object getAttribute(String name) {
		Assert.notNull(name, "'name' cannot be null.");
		return this.attributes.get(name);
	}

	public Object removeAttribute(String name) {
		Assert.notNull(name, "'name' cannot be null.");
		return this.attributes.remove(name);
	}

	public boolean hasAttribute(String name) {
		Assert.notNull(name, "'name' cannot be null.");
		return this.attributes.containsKey(name);
	}

	public String[] attributeNames() {
		Set attributeNames = this.attributes.keySet();
		return (String[]) attributeNames.toArray(new String[attributeNames.size()]);
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final AttributeAccessorSupport that = (AttributeAccessorSupport) o;

		if (!this.attributes.equals(that.attributes)) return false;

		return true;
	}

	public int hashCode() {
		return this.attributes.hashCode();
	}
}
