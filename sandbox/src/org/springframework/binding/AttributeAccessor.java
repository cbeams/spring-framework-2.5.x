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
package org.springframework.binding;

/**
 * Generic, lightweight interface for accessing a collection of attributes.
 * Useful for hiding the structure or source of the underlying attribute
 * collection.
 * <p>
 * For example, the backing collection might be a javabean, an http request,
 * an http session, a map, an mbean, or some other attribute holder.
 * 
 * @author Keith Donald
 */
public interface AttributeAccessor {

	/**
	 * Does the attribute by the provided name exist in this model?
	 * @param attributeName the attribute name
	 * @return true if so, false otherwise.
	 */
	public boolean containsAttribute(String attributeName);

	/**
	 * Get the attribute value associated with the provided name, returning
	 * <code>null</code> if not found.
	 * @param attributeName The attribute name
	 * @return the attribute value, or null if not found.
	 */
	public Object getAttribute(String attributeName);
}