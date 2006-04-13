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

/**
 * Interface defining a generic contract for attaching and accessing metadata
 * to/from arbitrary objects.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public interface AttributeAccessor {

	/**
	 * Sets the attribute defined by <code>name</code> to the supplied	<code>value</code>.
	 * If <code>value</code> is <code>null</code>, the attribute is {@link #removeAttribute removed}.
	 */

	void setAttribute(String name, Object value);

	/**
	 * Gets the value of the attribute identified by <code>name</code>. Returns <code>null</code> if
	 * the attribute doesn't exist.
	 */

	Object getAttribute(String name);

	/**
	 * Removes the attribute identified by <code>name</code> and returns its value. Returns <code>null</code>
	 * if no attribute under <code>name</code> is found.
	 */

	Object removeAttribute(String name);

	/**
	 * Returns <code>true</code> if the attribute identified by <code>name</code> exists, otherwise
	 * returns <code>false</code>.
	 */
	boolean hasAttribute(String name);

	/**
	 * Returns the names of all attributes.
	 */
	String[] attributeNames();
}
