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
 * Generic, lightweight interface for setting attribute values in a backing
 * collection. Useful for hiding the structure or source of the underlying
 * collection.
 * <p>
 * For example, the backing collection might be a javabean, a http request, a
 * http session, a map, a mbean, or some other mutable attribute holder.
 * @author Keith Donald
 */
public interface AttributeSetter extends AttributeAccessor {

	/**
	 * Set the attribute with the provided name to the value provided.
	 * @param attributeName The attribute name
	 * @param attributeValue The attribute value
	 * @return the previous value, or null if no previous value was held
	 */
	public Object setAttribute(String attributeName, Object attributeValue);
}